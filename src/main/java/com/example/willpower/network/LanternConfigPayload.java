package com.example.willpower.network;

import com.example.willpower.WillpowerMod;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public record LanternConfigPayload(
        boolean suitActive,
        boolean maskActive,
        boolean auraActive,
        boolean insigniaActive,
        int suitStyle,
        int maskHeight,
        float brightness,
        float saturation,
        float insigniaDistance,
        long glowingEyesMask,
        boolean eyesCorpsColor,
        float cruiseSpeed,
        int activePose
) implements CustomPayload {

    public static final Id<LanternConfigPayload> ID = new Id<>(WillpowerMod.id("lantern_config"));

    public static final PacketCodec<RegistryByteBuf, LanternConfigPayload> CODEC = PacketCodec.of(
            (v, buf) -> {
                buf.writeBoolean(v.suitActive);
                buf.writeBoolean(v.maskActive);
                buf.writeBoolean(v.auraActive);
                buf.writeBoolean(v.insigniaActive);
                buf.writeInt(v.suitStyle);
                buf.writeInt(v.maskHeight);
                buf.writeFloat(v.brightness);
                buf.writeFloat(v.saturation);
                buf.writeFloat(v.insigniaDistance);
                buf.writeLong(v.glowingEyesMask);
                buf.writeBoolean(v.eyesCorpsColor);
                buf.writeFloat(v.cruiseSpeed);
                buf.writeInt(v.activePose);
            },
            buf -> new LanternConfigPayload(
                    buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                    buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readLong(), buf.readBoolean(), buf.readFloat(), buf.readInt()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static LanternConfigPayload fromComponent(PlayerLanternComponent d) {
        return new LanternConfigPayload(
                d.isSuitActive(), d.isMaskActive(), d.isAuraActive(), d.isInsigniaActive(),
                d.getSuitStyle(), d.getMaskHeight(), d.getBrightness(), d.getSaturation(),
                d.getInsigniaDistance(), d.getGlowingEyesMask(), d.isEyesCorpsColor(),
                d.getCruiseSpeed(), d.getActivePose()
        );
    }

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> apply(context.player(), payload))
        );
    }

    private static void apply(ServerPlayerEntity player, LanternConfigPayload p) {
        PlayerLanternComponent d = ModComponents.LANTERN.get(player);
        if (d.getActiveCorps() == null) return;

        d.setSuitActive(p.suitActive());
        d.setMaskActive(p.maskActive());
        d.setAuraActive(p.auraActive());
        d.setInsigniaActive(p.insigniaActive());
        d.setSuitStyle(p.suitStyle());
        d.setMaskHeight(p.maskHeight());
        d.setBrightness(p.brightness());
        d.setSaturation(p.saturation());
        d.setInsigniaDistance(p.insigniaDistance());
        d.setGlowingEyesMask(p.glowingEyesMask());
        d.setEyesCorpsColor(p.eyesCorpsColor());
        d.setCruiseSpeed(p.cruiseSpeed());
        d.setActivePose(p.activePose());

        if (!player.isCreative() && !player.isSpectator()) {
            boolean canFly = p.suitActive() && d.getCharge() > 0;
            player.getAbilities().allowFlying = canFly;
            if (!canFly) player.getAbilities().flying = false;
        }
        player.getAbilities().setFlySpeed(Math.clamp(p.cruiseSpeed() / 480.0f, 0.02f, 0.25f));
        player.sendAbilitiesUpdate();
    }
}
