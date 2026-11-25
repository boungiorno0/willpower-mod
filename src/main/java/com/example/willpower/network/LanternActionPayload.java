package com.example.willpower.network;

import com.example.willpower.WillpowerMod;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.CorpsAbilities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record LanternActionPayload(int action) implements CustomPayload {
    public static final int ACTION_SIGNATURE = 0;
    public static final int ACTION_TRACTOR_TOGGLE = 1;
    public static final int ACTION_TRACTOR_RELEASE = 2;

    public static final Id<LanternActionPayload> ID = new Id<>(WillpowerMod.id("lantern_action"));
    public static final PacketCodec<RegistryByteBuf, LanternActionPayload> CODEC = PacketCodec.of(
            (v, buf) -> buf.writeInt(v.action()),
            buf -> new LanternActionPayload(buf.readInt())
    );

    private static final Map<UUID, Integer> HELD_ENTITIES = new HashMap<>();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, ctx) ->
                ctx.server().execute(() -> handleAction(ctx.player(), payload.action()))
        );

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                Integer entityId = HELD_ENTITIES.get(player.getUuid());
                if (entityId == null) continue;
                tickTractorHold(world, player, entityId);
            }
        });
    }

    private static void handleAction(ServerPlayerEntity player, int action) {
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() == null || data.getCharge() <= 0) {
            HELD_ENTITIES.remove(player.getUuid());
            return;
        }

        if (action == ACTION_SIGNATURE) {
            CorpsAbilities.triggerSignature(player);
            return;
        }

        Integer heldId = HELD_ENTITIES.get(player.getUuid());
        if (heldId != null) {
            Entity target = player.getServerWorld().getEntityById(heldId);
            HELD_ENTITIES.remove(player.getUuid());
            if (target != null && action == ACTION_TRACTOR_TOGGLE) {
                Vec3d launch = player.getRotationVec(1.0f).multiply(2.4).add(0, 0.75, 0);
                target.setVelocity(launch);
                target.velocityModified = true;
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.4f);
            }
            return;
        }

        if (action == ACTION_TRACTOR_TOGGLE) {
            Entity hit = raycastEntity(player, 24.0);
            if (hit != null) {
                HELD_ENTITIES.put(player.getUuid(), hit.getId());
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.7f, 1.6f);
            }
        }
    }

    private static void tickTractorHold(ServerWorld world, ServerPlayerEntity player, int entityId) {
        Entity target = world.getEntityById(entityId);
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (target == null || !target.isAlive() || !data.consumeCharge(2)) {
            HELD_ENTITIES.remove(player.getUuid());
            return;
        }

        Vec3d start = player.getEyePos().add(0, -0.2, 0);
        Vec3d goal = player.getEyePos().add(player.getRotationVec(1.0f).multiply(6.5));
        target.setVelocity(goal.subtract(target.getPos()).multiply(0.55));
        target.velocityModified = true;
        target.fallDistance = 0.0f;

        int argb = data.getDisplayArgb();
        Vector3f col = new Vector3f(((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f);
        DustParticleEffect dust = new DustParticleEffect(col, 1.1f);

        Vec3d end = target.getPos().add(0, target.getHeight() * 0.5, 0);
        for (int i = 1; i <= 10; i++) {
            Vec3d p = start.lerp(end, i / 10.0);
            world.spawnParticles(dust, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    private static Entity raycastEntity(ServerPlayerEntity player, double range) {
        Vec3d eye = player.getEyePos();
        Vec3d end = eye.add(player.getRotationVec(1.0f).multiply(range));
        Box box = player.getBoundingBox().stretch(player.getRotationVec(1.0f).multiply(range)).expand(1.5);
        EntityHitResult res = ProjectileUtil.raycast(player, eye, end, box, e -> e instanceof LivingEntity && e != player, range * range);
        return res != null ? res.getEntity() : null;
    }
}
