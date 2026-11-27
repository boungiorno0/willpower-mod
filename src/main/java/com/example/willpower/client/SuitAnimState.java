package com.example.willpower.client;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SuitAnimState {
    private static final Map<UUID, Anim> STATES = new HashMap<>();

    private static class Anim {
        float progress = 0.0f;
        long lastNs = System.nanoTime();
    }

    private SuitAnimState() {}

    public static float getProgress(PlayerEntity player, boolean suitActive) {
        Anim a = STATES.computeIfAbsent(player.getUuid(), id -> {
            Anim init = new Anim();
            init.progress = suitActive ? 1.0f : 0.0f;
            return init;
        });
        long now = System.nanoTime();
        float dt = Math.clamp((now - a.lastNs) / 1_000_000_000.0f, 0.0f, 0.05f);
        a.lastNs = now;

        if (suitActive && a.progress < 1.0f) {
            a.progress = Math.min(1.0f, a.progress + dt * 1.45f);
        } else if (!suitActive && a.progress > 0.0f) {
            a.progress = Math.max(0.0f, a.progress - dt * 2.1f);
        }
        return a.progress;
    }

    public static float sectionAlpha(float anim, float startAt) {
        if (anim <= startAt) return 0.0f;
        return Math.clamp((anim - startAt) / 0.35f, 0.0f, 1.0f);
    }
}
