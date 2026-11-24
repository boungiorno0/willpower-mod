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
    private static final int MAX_POINTS = 22;
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
                while (deque.size() > MAX_POINTS) {
                    deque.removeLast();
                }
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
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;

            List<Vec3d> list = new ArrayList<>(points);
            for (int i = 0; i < list.size() - 1; i++) {
                float progress = 1.0f - (i / (float) list.size());
                float halfSize = 0.08f + 0.14f * progress;
                int alpha = (int) (175 * progress);
                Vec3d p = list.get(i);
                drawVoxelBox(entry, vc, p.x, p.y, p.z, halfSize, r, g, b, alpha);
            }

            // Sonic Boom вспышка на высокой скорости (> 1.4 блоков/тик)
            double speed = player.getVelocity().length();
            if (speed > 1.4 && data.isSuitActive()) {
                Vec3d look = player.getRotationVec(1.0f);
                Vec3d center = player.getPos().add(0, player.getHeight() * 0.5, 0).add(look.multiply(0.9));
                float pulse = 0.45f + 0.12f * (float) Math.sin((player.age + context.tickCounter().getTickDelta(false)) * 0.9f);
                drawVoxelBox(entry, vc, center.x, center.y, center.z, pulse, 220, 255, 255, 75);
            }
        }

        matrices.pop();
    }

    private static void drawVoxelBox(MatrixStack.Entry entry, VertexConsumer vc,
                                     double cx, double cy, double cz, float s,
                                     int r, int g, int b, int a) {
        float x0 = (float) (cx - s), x1 = (float) (cx + s);
        float y0 = (float) (cy - s), y1 = (float) (cy + s);
        float z0 = (float) (cz - s), z1 = (float) (cz + s);

        // 6 граней светящегося вокселя
        quad(entry, vc, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(entry, vc, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a);
        quad(entry, vc, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
        quad(entry, vc, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a);
        quad(entry, vc, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a);
        quad(entry, vc, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a);
    }

    private static void quad(MatrixStack.Entry e, VertexConsumer vc,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int r, int g, int b, int a) {
        vc.vertex(e, x1, y1, z1).color(r, g, b, a);
        vc.vertex(e, x2, y2, z2).color(r, g, b, a);
        vc.vertex(e, x3, y3, z3).color(r, g, b, a);
        vc.vertex(e, x4, y4, z4).color(r, g, b, a);
    }
}
