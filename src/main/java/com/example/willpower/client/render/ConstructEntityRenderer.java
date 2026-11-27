package com.example.willpower.client.render;

import com.example.willpower.entity.ConstructEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class ConstructEntityRenderer extends EntityRenderer<ConstructEntity> {
    private static final Identifier WHITE_TEX = Identifier.of("minecraft", "textures/misc/white.png");
    private final ModelPart coreBox;

    public ConstructEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        ModelData md = new ModelData();
        md.getRoot().addChild("b", ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -4f, -4f, 8f, 8f, 8f), ModelTransform.NONE);
        this.coreBox = TexturedModelData.of(md, 16, 16).createModel();
    }

    @Override
    public void render(ConstructEntity entity, float yaw, float tickDelta, MatrixStack m, VertexConsumerProvider consumers, int light) {
        float scale = entity.getScale();
        int rgb = entity.getColorArgb() & 0x00FFFFFF;
        int coreColor = 0xCC000000 | rgb;
        int auraColor = 0x55000000 | rgb;
        float spin = (entity.age + tickDelta) * 14f;

        VertexConsumer vc = consumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
        m.push();

        switch (entity.getKind()) {
            case CAGE -> renderCage(m, vc, spin, coreColor, auraColor);
            case DOME -> renderDome(m, vc, spin, coreColor, auraColor);
            case FIST -> renderFist(m, vc, entity, scale, coreColor, auraColor);
            case BOOMERANG -> {
                m.translate(0.0, 0.35, 0.0);
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin * 2.4f));
                drawScaled(m, vc, 2.3f * scale, 0.22f * scale, 0.45f * scale, coreColor);
                drawScaled(m, vc, 0.45f * scale, 0.22f * scale, 2.3f * scale, coreColor);
            }
            case CHAIN_BOLT -> {
                m.translate(0.0, 0.35, 0.0);
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw()));
                m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));
                drawScaled(m, vc, 0.35f * scale, 0.35f * scale, 2.8f * scale, coreColor);
            }
            case LIGHT_ORB -> {
                m.translate(0.0, 0.35, 0.0);
                float p = 1.0f + 0.14f * (float) Math.sin((entity.age + tickDelta) * 0.25f);
                drawScaled(m, vc, 0.85f * p, 0.85f * p, 0.85f * p, 0xEEFFFFFF);
                drawScaled(m, vc, 1.35f * p, 1.35f * p, 1.35f * p, auraColor);
            }
            case BLAST -> {
                m.translate(0.0, 0.35, 0.0);
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
                m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(spin * 0.7f));
                drawScaled(m, vc, scale, scale, scale, coreColor);
                drawScaled(m, vc, 1.35f * scale, 1.35f * scale, 1.35f * scale, auraColor);
            }
        }
        m.pop();
    }

    private void renderFist(MatrixStack m, VertexConsumer vc, ConstructEntity e, float s, int core, int aura) {
        m.translate(0.0, 0.35, 0.0);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-e.getYaw()));
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(e.getPitch()));
        m.scale(s, s, s);
        m.push(); m.translate(0, 0, -0.65); drawScaled(m, vc, 0.85f, 0.85f, 1.25f, aura); m.pop();
        drawScaled(m, vc, 1.35f, 1.15f, 1.1f, core);
        for (int i = 0; i < 4; i++) {
            m.push();
            m.translate(-0.42f + i * 0.28f, -0.05f, 0.55f);
            drawScaled(m, vc, 0.22f, 0.75f, 0.38f, core);
            m.pop();
        }
        m.push(); m.translate(0.68f, 0.25f, 0.25f); drawScaled(m, vc, 0.32f, 0.45f, 0.65f, core); m.pop();
    }

    private void renderCage(MatrixStack m, VertexConsumer vc, float spin, int core, int aura) {
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin * 0.15f));
        for (int i = 0; i < 10; i++) {
            double ang = (Math.PI * 2.0 * i) / 10.0;
            m.push();
            m.translate(Math.cos(ang) * 1.45, 1.25, Math.sin(ang) * 1.45);
            m.multiply(RotationAxis.POSITIVE_Y.rotation((float) -ang));
            drawScaled(m, vc, 0.12f, 2.7f, 0.12f, core);
            m.pop();
        }
        drawRing(m, vc, 0.05f, 1.45f, 0.12f, aura);
        drawRing(m, vc, 2.60f, 1.45f, 0.12f, aura);
    }

    private void renderDome(MatrixStack m, VertexConsumer vc, float spin, int core, int aura) {
        for (int i = 0; i < 4; i++) {
            m.push();
            m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin * 0.45f + i * 45f));
            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i == 0 ? 0f : 55f));
            drawRing(m, vc, 0.0f, 2.55f, 0.10f, i % 2 == 0 ? core : aura);
            m.pop();
        }
        drawRing(m, vc, -0.7f, 2.35f, 0.08f, aura);
        drawRing(m, vc,  0.7f, 2.35f, 0.08f, aura);
    }

    private void drawRing(MatrixStack m, VertexConsumer vc, float y, float radius, float thickness, int color) {
        int segs = 16;
        float segLen = (float) (2.0 * Math.PI * radius / segs) * 1.05f;
        for (int k = 0; k < segs; k++) {
            double a = (Math.PI * 2.0 * k) / segs;
            m.push();
            m.translate(Math.cos(a) * radius, y, Math.sin(a) * radius);
            m.multiply(RotationAxis.POSITIVE_Y.rotation((float) -a));
            drawScaled(m, vc, thickness, thickness, segLen, color);
            m.pop();
        }
    }

    private void drawScaled(MatrixStack m, VertexConsumer vc, float sx, float sy, float sz, int color) {
        m.push();
        m.scale(sx, sy, sz);
        coreBox.render(m, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, color);
        m.pop();
    }

    @Override
    public Identifier getTexture(ConstructEntity entity) {
        return WHITE_TEX;
    }
}
