package com.example.willpower.client;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlightAnimState {
    private static final Map<UUID, State> STATES = new HashMap<>();

    private static class State {
        float tilt = 0.0f;
        float roll = 0.0f;
        long lastNs = System.nanoTime();
    }

    private FlightAnimState() {}

    public static float getTilt(AbstractClientPlayerEntity player) {
        return update(player).tilt;
    }

    public static float getRoll(AbstractClientPlayerEntity player) {
        return update(player).roll;
    }

    private static State update(AbstractClientPlayerEntity player) {
        State s = STATES.computeIfAbsent(player.getUuid(), id -> new State());
        long now = System.nanoTime();
        float dt = Math.clamp((now - s.lastNs) / 1_000_000_000.0f, 0.0f, 0.05f);
        if (dt < 0.0005f) return s;
        s.lastNs = now;

        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        boolean activeFlight = data.getActiveCorps() != null && data.isSuitActive()
                && (player.getAbilities().flying || player.isFallFlying());

        double dx = player.getX() - player.prevX;
        double dy = player.getY() - player.prevY;
        double dz = player.getZ() - player.prevZ;
        double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);

        float targetTilt = (activeFlight && speed > 0.14) ? 1.0f : 0.0f;
        s.tilt += (targetTilt - s.tilt) * Math.min(1.0f, dt * 8.0f);

        float yawDiff = MathHelper.wrapDegrees(player.getYaw() - player.bodyYaw);
        float targetRoll = activeFlight ? MathHelper.clamp(yawDiff * 0.75f, -38f, 38f) * s.tilt : 0.0f;
        s.roll += (targetRoll - s.roll) * Math.min(1.0f, dt * 9.0f);
        return s;
    }
}
