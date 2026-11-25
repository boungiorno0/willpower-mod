package com.example.willpower.core;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SpectrumSynergy {
    private static final double RANGE_SQ = 256.0; // радиус 16 блоков

    private SpectrumSynergy() {}

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % 20 != 0) return;

            for (ServerPlayerEntity source : world.getPlayers()) {
                PlayerLanternComponent srcData = ModComponents.LANTERN.get(source);
                if (srcData.getActiveCorps() == null || !srcData.isSuitActive() || srcData.getCharge() <= 0) continue;

                LanternCorps src = srcData.getActiveCorps();
                for (ServerPlayerEntity target : world.getPlayers()) {
                    if (source == target || source.squaredDistanceTo(target) > RANGE_SQ) continue;
                    applyTickInteraction(src, ModComponents.LANTERN.get(target));
                }
            }
        });
    }

    private static void applyTickInteraction(LanternCorps source, PlayerLanternComponent targetData) {
        LanternCorps target = targetData.getActiveCorps();
        if (target == null || targetData.getCharge() <= 0) return;

        switch (source) {
            case BLUE -> {
                if (target == LanternCorps.GREEN) targetData.recharge(20, false);
                else if (target == LanternCorps.YELLOW || target == LanternCorps.RED) targetData.drainCharge(18);
            }
            case STAR_SAPPHIRE -> {
                if (target == LanternCorps.RED) targetData.drainCharge(15);
            }
            case BLACK -> {
                if (target != LanternCorps.WHITE && target != LanternCorps.BLACK) {
                    targetData.drainCharge(22);
                }
            }
            case WHITE -> {
                if (target == LanternCorps.BLACK) targetData.drainCharge(35);
                else if (target != LanternCorps.WHITE) targetData.recharge(15, false);
            }
            default -> {}
        }
    }

    /** Итоговый множитель силы и размера конструктов (от 0.5 до 1.85) */
    public static float getPowerMultiplier(PlayerEntity owner) {
        PlayerLanternComponent self = ModComponents.LANTERN.get(owner);
        LanternCorps me = self.getActiveCorps();
        if (me == null) return 1.0f;

        float mult = 1.0f;
        for (PlayerEntity other : owner.getWorld().getPlayers()) {
            if (other == owner || owner.squaredDistanceTo(other) > RANGE_SQ) continue;
            PlayerLanternComponent otherData = ModComponents.LANTERN.get(other);
            LanternCorps near = otherData.getActiveCorps();
            if (near == null || !otherData.isSuitActive() || otherData.getCharge() <= 0) continue;

            if (me == LanternCorps.GREEN && near == LanternCorps.BLUE) mult += 0.35f;
            if (me == LanternCorps.GREEN && near == LanternCorps.YELLOW && self.getRank() < 3) mult -= 0.25f;
            if (me == LanternCorps.YELLOW && near == LanternCorps.BLUE) mult -= 0.40f;
            if (me == LanternCorps.RED && (near == LanternCorps.BLUE || near == LanternCorps.STAR_SAPPHIRE)) mult -= 0.30f;
            if (me == LanternCorps.INDIGO) mult += 0.15f;
            if (me != LanternCorps.WHITE && near == LanternCorps.BLACK) mult -= 0.30f;
            if (near == LanternCorps.WHITE && me != LanternCorps.BLACK) mult += 0.20f;
            if (me == LanternCorps.WHITE && near == LanternCorps.BLACK) mult += 0.60f;
        }
        return Math.clamp(mult, 0.50f, 1.85f);
    }
}
