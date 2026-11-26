package com.example.willpower.client.render;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public final class VoxelTrailRenderer {
    private static final int MAX_POINTS = 24;
    private static final Map<UUID, Deque<Vec3d>> TRAILS = new HashMap<>();

    private VoxelTrailRenderer() {}

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.isPaused()) return;
        Set<UUID> activeIds = new HashSet<>();

        for (PlayerEntity player : client.world.getPlayers()) {
            PlayerLanternComponent data = ModComponents.LANTERN.get(player);
            boolean flying = data.getActiveCorps() != null && data.isSuitActive()
                    && (player.getAbilities().flying || player.isFallFlying() || player.getVelocity().lengthSquared() > 0.35);

            if (flying) {
                activeIds.add(player.getUuid());
                Deque<Vec3d> deque = TRAILS.computeIfAbsent(player.getUuid(), id -> new ArrayDeque<>());
                deque.addFirst(player.getPos().add(0, player.getHeight() * 0.45, 0));
                while (deque.size() > MAX_POINTS) deque.removeLast();
            }
        }
        TRAILS.keySet().removeIf(id -> {
            if (!activeIds.contains(id)) {
                Deque<Vec3d> d = TRAILS.get(id);
                if (d != null && !d.isEmpty()) {
                    d.removeLast();
                    return d.isEmpty();
                }
                return true;
            }
            return false;
        });
    }

    public static void render(WorldRenderContext context) {
        if (context.world() == null || TRAILS.isEmpty()) return;
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d cam = context.camera().getPos();
        VertexConsumer vc = consumers.getBuffer(HardLightRenderLayers.getVoxelGlow());

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        var entry = matrices.peek();

        for (PlayerEntity player : context.world().getPlayers()) {
            Deque<Vec3d> points = TRAILS.get(player.getUuid());
            if (points == null || points.size() < 2) continue;

            PlayerLanternComponent data = ModComponents.LANTERN.get(player);
            int argb = data.getDisplayArgb();
            int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;

            List<Vec3d> list = new ArrayList<>(points);
            for (int i = 0; i < list.size() - 1; i++) {
                Vec3d p0 = list.get(i), p1 = list.get(i + 1);
                Vec3d dir = p1.subtract(p0);
                if (dir.lengthSquared() < 1e-6) continue;

                float t0 = 1.0f - (i / (float) list.size());
                float t1 = 1.0f - ((i + 1) / (float) list.size());
                Vec3d side = dir.crossProduct(cam.subtract(p0));
                if (side.lengthSquared() < 1e-6) side = new Vec3d(0, 1, 0);
                side = side.normalize();

                Vec3d s0 = side.multiply(0.18 * t0);
                Vec3d s1 = side.multiply(0.18 * t1);
                int a0 = (int) (170 * t0), a1 = (int) (170 * t1);

                vc.vertex(entry, (float)(p0.x - s0.x), (float)(p0.y - s0.y), (float)(p0.z - s0.z)).color(r, g, b, a0);
                vc.vertex(entry, (float)(p0.x + s0.x), (float)(p0.y + s0.y), (float)(p0.z + s0.z)).color(r, g, b, a0);
                vc.vertex(entry, (float)(p1.x + s1.x), (float)(p1.y + s1.y), (float)(p1.z + s1.z)).color(r, g, b, a1);
                vc.vertex(entry, (float)(p1.x - s1.x), (float)(p1.y - s1.y), (float)(p1.z - s1.z)).color(r, g, b, a1);
            }
        }
        matrices.pop();
    }
}
