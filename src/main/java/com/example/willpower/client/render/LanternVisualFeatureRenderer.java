package com.example.willpower.client.render;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public class LanternVisualFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier WHITE_TEX = Identifier.of("minecraft", "textures/misc/white.png");

    public LanternVisualFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() == null) return;

        int argb = data.getDisplayArgb();
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // 1. Светящееся кольцо на руке
        matrices.push();
        if (player.getMainArm() == Arm.LEFT) {
            this.getContextModel().leftArm.rotate(matrices);
            matrices.translate(0.0625F, 0.54F, 0.0F);
        } else {
            this.getContextModel().rightArm.rotate(matrices);
            matrices.translate(-0.0625F, 0.54F, 0.0F);
        }
        VertexConsumer ringVc = vertexConsumers.getBuffer(HardLightRenderLayers.getVoxelGlow());
        drawGlowBox(matrices.peek(), ringVc, 0.065F, 0.065F, 0.065F, r, g, b, data.isDesynced() && (player.age % 6 < 3) ? 45 : 195);
        matrices.pop();

        // 2. Парящая 3D-эмблема перед грудью
        if (data.isSuitActive() && data.isInsigniaActive()) {
            matrices.push();
            this.getContextModel().body.rotate(matrices);
            float dist = data.getInsigniaDistance();
            matrices.translate(0.0F, 0.23F, -0.15F - dist);

            Identifier emblemTex = data.getActiveCorps().getInsigniaTexture();
            if (MinecraftClient.getInstance().getResourceManager().getResource(emblemTex).isPresent()) {
                matrices.scale(0.30F, 0.30F, 0.30F);
                VertexConsumer emblemVc = vertexConsumers.getBuffer(HardLightRenderLayers.getHardLightGlow(emblemTex));
                drawTexturedQuad(matrices, emblemVc, r, g, b, 235);
            } else {
                VertexConsumer emblemVc = vertexConsumers.getBuffer(HardLightRenderLayers.getVoxelGlow());
                drawProceduralEmblem(matrices, emblemVc, r, g, b, 220);
                matrices.translate(0.0F, 0.0F, dist * 0.45F);
                drawProceduralEmblem(matrices, emblemVc, r, g, b, 105);
            }
            matrices.pop();
        }

        // 3. Светящиеся глаза
        long eyesMask = data.getGlowingEyesMask();
        if (eyesMask != 0L) {
            matrices.push();
            this.getContextModel().head.rotate(matrices);
            VertexConsumer eyeVc = vertexConsumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
            int er = data.isEyesCorpsColor() ? r : 255;
            int eg = data.isEyesCorpsColor() ? g : 255;
            int eb = data.isEyesCorpsColor() ? b : 255;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (((eyesMask >>> (row * 8 + col)) & 1L) != 0) {
                        drawFacePixel(matrices, eyeVc, -0.25F + col * 0.0625F, -0.50F + row * 0.0625F, -0.253F, 0.0625F, er, eg, eb, 255);
                    }
                }
            }
            matrices.pop();
        }

        // 4. Энергетическая аура
        if (data.isAuraActive() && data.getCharge() > 0) {
            matrices.push();
            this.getContextModel().body.rotate(matrices);
            matrices.translate(0.0F, 0.375F, 0.0F);
            float pulse = 0.025F * (float) Math.sin((player.age + tickDelta) * 0.25F);
            VertexConsumer auraVc = vertexConsumers.getBuffer(HardLightRenderLayers.getVoxelGlow());
            drawGlowBox(matrices.peek(), auraVc, 0.30F + pulse, 0.43F + pulse, 0.18F + pulse, r, g, b, 55);
            matrices.pop();
        }
    }

    private static void drawProceduralEmblem(MatrixStack m, VertexConsumer vc, int r, int g, int b, int a) {
        drawBar(m, vc, 0.0f, -0.095f, 0.095f, 0.017f, r, g, b, a);
        drawBar(m, vc, 0.0f,  0.095f, 0.095f, 0.017f, r, g, b, a);
        drawBar(m, vc, 0.0f, -0.055f, 0.060f, 0.015f, r, g, b, a);
        drawBar(m, vc, 0.0f,  0.055f, 0.060f, 0.015f, r, g, b, a);
        drawBar(m, vc, -0.052f, 0.0f, 0.015f, 0.055f, r, g, b, a);
        drawBar(m, vc,  0.052f, 0.0f, 0.015f, 0.055f, r, g, b, a);
    }

    private static void drawBar(MatrixStack m, VertexConsumer vc, float ox, float oy, float sx, float sy, int r, int g, int b, int a) {
        m.push();
        m.translate(ox, oy, 0.0f);
        drawGlowBox(m.peek(), vc, sx, sy, 0.012f, r, g, b, a);
        m.pop();
    }

    private static void drawTexturedQuad(MatrixStack m, VertexConsumer vc, int r, int g, int b, int a) {
        var e = m.peek();
        vc.vertex(e, -0.5F, -0.5F, 0).color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
        vc.vertex(e,  0.5F, -0.5F, 0).color(r, g, b, a).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
        vc.vertex(e,  0.5F,  0.5F, 0).color(r, g, b, a).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
        vc.vertex(e, -0.5F,  0.5F, 0).color(r, g, b, a).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
    }

    private static void drawFacePixel(MatrixStack m, VertexConsumer vc, float x, float y, float z, float s, int r, int g, int b, int a) {
        var e = m.peek();
        vc.vertex(e, x,     y,     z).color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
        vc.vertex(e, x + s, y,     z).color(r, g, b, a).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
        vc.vertex(e, x + s, y + s, z).color(r, g, b, a).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
        vc.vertex(e, x,     y + s, z).color(r, g, b, a).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, -1);
    }

    private static void drawGlowBox(MatrixStack.Entry e, VertexConsumer vc, float sx, float sy, float sz, int r, int g, int b, int a) {
        quad(e, vc, -sx, -sy,  sz,  sx, -sy,  sz,  sx,  sy,  sz, -sx,  sy,  sz, r, g, b, a);
        quad(e, vc,  sx, -sy, -sz, -sx, -sy, -sz, -sx,  sy, -sz,  sx,  sy, -sz, r, g, b, a);
        quad(e, vc, -sx, -sy, -sz, -sx, -sy,  sz, -sx,  sy,  sz, -sx,  sy, -sz, r, g, b, a);
        quad(e, vc,  sx, -sy,  sz,  sx, -sy, -sz,  sx,  sy, -sz,  sx,  sy,  sz, r, g, b, a);
        quad(e, vc, -sx,  sy,  sz,  sx,  sy,  sz,  sx,  sy, -sz, -sx,  sy, -sz, r, g, b, a);
        quad(e, vc, -sx, -sy, -sz,  sx, -sy, -sz,  sx, -sy,  sz, -sx, -sy,  sz, r, g, b, a);
    }

    private static void quad(MatrixStack.Entry e, VertexConsumer vc, float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4, int r, int g, int b, int a) {
        vc.vertex(e, x1, y1, z1).color(r, g, b, a);
        vc.vertex(e, x2, y2, z2).color(r, g, b, a);
        vc.vertex(e, x3, y3, z3).color(r, g, b, a);
        vc.vertex(e, x4, y4, z4).color(r, g, b, a);
    }
}
