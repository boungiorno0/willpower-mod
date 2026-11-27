package com.example.willpower.core;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.entity.ConstructEntity;
import com.example.willpower.entity.ModEntities;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public final class CorpsAbilities {
    private CorpsAbilities() {}

    public static void triggerSignature(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        LanternCorps corps = data.getActiveCorps();
        if (corps == null || !data.consumeCharge(180)) return;

        int argb = data.getDisplayArgb();
        LivingEntity aimed = raycastLiving(player, 22.0);
        switch (corps) {
            case GREEN -> {
                spawnAt(world, player, player.getPos().add(0, 1.0, 0), ConstructEntity.Kind.DOME, argb);
                if (aimed != null) spawnAt(world, player, aimed.getPos(), ConstructEntity.Kind.CAGE, argb);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 1));
            }
            case YELLOW -> {
                if (aimed != null) {
                    spawnAt(world, player, aimed.getPos(), ConstructEntity.Kind.CAGE, argb);
                    LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                    if (bolt != null) {
                        bolt.refreshPositionAfterTeleport(aimed.getPos());
                        world.spawnEntity(bolt);
                    }
                }
                for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10), x -> x != player)) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 140, 0));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));
                    Vec3d away = e.getPos().subtract(player.getPos()).normalize().multiply(1.3);
                    e.addVelocity(away.x, 0.55, away.z);
                    e.velocityModified = true;
                }
            }
            case BLUE -> {
                spawnAt(world, player, player.getPos().add(0, 1.0, 0), ConstructEntity.Kind.DOME, argb);
                for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10), x -> x == player || x instanceof PlayerEntity)) {
                    e.heal(8.0f);
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 1));
                }
            }
            case RED -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 120, 1));
                world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getBodyY(0.5), player.getZ(), 45, 2.5, 1.0, 2.5, 0.08);
                for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(7), x -> x != player)) {
                    e.setOnFireFor(6);
                    e.damage(world.getDamageSources().inFire(), 9.0f);
                    player.heal(2.0f);
                }
            }
            case ORANGE -> {
                if (aimed != null && !aimed.getMainHandStack().isEmpty()) {
                    ItemStack stolen = aimed.getMainHandStack().copy();
                    aimed.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    player.getInventory().offerOrDrop(stolen);
                }
                for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(18), e -> true)) {
                    item.setPosition(player.getX(), player.getY() + 0.5, player.getZ());
                }
            }
            case INDIGO -> {
                if (aimed != null) {
                    Vec3d myPos = player.getPos();
                    player.requestTeleport(aimed.getX(), aimed.getY(), aimed.getZ());
                    aimed.requestTeleport(myPos.x, myPos.y, myPos.z);
                } else {
                    HitResult hit = player.raycast(22.0, 1.0f, false);
                    Vec3d p = hit.getPos();
                    player.requestTeleport(p.x, p.y + 0.5, p.z);
                }
                world.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getBodyY(0.5), player.getZ(), 30, 0.5, 1.0, 0.5, 0.2);
            }
            case STAR_SAPPHIRE -> {
                if (aimed != null) {
                    aimed.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 250));
                    aimed.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 120, 0));
                    spawnAt(world, player, aimed.getPos(), ConstructEntity.Kind.CAGE, argb);
                }
            }
            case WHITE -> {
                spawnAt(world, player, player.getPos().add(0, 1.0, 0), ConstructEntity.Kind.DOME, argb);
                player.heal(20.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1));
                for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(11), x -> x != player)) {
                    if (e.getType().isIn(EntityTypeTags.UNDEAD)) {
                        e.setOnFireFor(8);
                        e.damage(world.getDamageSources().magic(), 18.0f);
                    }
                }
                growCropsAround(world, player.getBlockPos(), 4);
            }
            case BLACK -> {
                world.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getBodyY(0.5), player.getZ(), 50, 2.5, 1.0, 2.5, 0.05);
                List<LivingEntity> foes = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10),
                        x -> x != player && !(x.hasCustomName() && "Black Thrall".equals(x.getCustomName().getString())));
                for (LivingEntity e : foes) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
                    e.damage(world.getDamageSources().wither(), 8.0f);
                    player.heal(3.0f);
                }
                LivingEntity target = foes.isEmpty() ? null : foes.getFirst();
                for (int i = 0; i < 2; i++) {
                    ZombieEntity z = EntityType.ZOMBIE.create(world);
                    if (z != null) {
                        z.refreshPositionAndAngles(player.getX() + (i == 0 ? 1.8 : -1.8), player.getY(), player.getZ(), 0, 0);
                        z.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                        z.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 800, 1));
                        z.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 800, 1));
                        z.setCustomName(Text.literal("Black Thrall"));
                        if (target != null) z.setTarget(target);
                        world.spawnEntity(z);
                    }
                }
            }
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.15f);
    }

    private static void spawnAt(ServerWorld world, PlayerEntity owner, Vec3d pos, ConstructEntity.Kind kind, int argb) {
        ConstructEntity c = ModEntities.CONSTRUCT.create(world);
        if (c != null) {
            c.configureAt(owner, pos, kind, argb, SpectrumSynergy.getPowerMultiplier(owner));
            world.spawnEntity(c);
        }
    }

    private static LivingEntity raycastLiving(PlayerEntity player, double range) {
        Vec3d eye = player.getEyePos();
        Vec3d end = eye.add(player.getRotationVec(1.0f).multiply(range));
        Box box = player.getBoundingBox().stretch(player.getRotationVec(1.0f).multiply(range)).expand(1.5);
        EntityHitResult res = ProjectileUtil.raycast(player, eye, end, box, e -> e instanceof LivingEntity && e != player, range * range);
        return res != null ? (LivingEntity) res.getEntity() : null;
    }

    private static void growCropsAround(ServerWorld world, BlockPos c, int r) {
        for (BlockPos p : BlockPos.iterate(c.add(-r, -1, -r), c.add(r, 1, r))) {
            var state = world.getBlockState(p);
            if (state.getBlock() instanceof Fertilizable f && f.isFertilizable(world, p, state)) {
                f.grow(world, world.random, p, state);
            }
        }
    }
}
