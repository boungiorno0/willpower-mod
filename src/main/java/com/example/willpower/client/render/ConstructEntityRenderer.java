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
        ModelData modelData = new ModelData();
        modelData.getRoot().addChild(
                "box",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -4f, -4f, 8f, 8f, 8f),
                ModelTransform.NONE
        );
        this.coreBox = TexturedModelData.of(modelData, 16, 16).createModel();
    }

    @Override
    public void render(ConstructEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        float scale = entity.getScale();
        int rgb = entity.getColorArgb() & 0x00FFFFFF;
        int coreColor = 0xCC000000 | rgb;
        int auraColor = 0x55000000 | rgb;
        float spin = (entity.age + tickDelta) * 14f;

        VertexConsumer vc = vertexConsumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));

        matrices.push();
        matrices.translate(0.0, 0.35, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));

        switch (entity.getKind()) {
            case FIST -> {
                matrices.scale(1.3f * scale, 1.1f * scale, 1.9f * scale);
                renderLayered(matrices, vc, coreColor, auraColor);
            }
            case BOOMERANG -> {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin * 2.2f));
                matrices.scale(2.2f * scale, 0.28f * scale, 2.2f * scale);
                renderLayered(matrices, vc, coreColor, auraColor);
            }
            case CHAIN_BOLT -> {
                matrices.scale(0.45f * scale, 0.45f * scale, 2.6f * scale);
                renderLayered(matrices, vc, coreColor, auraColor);
            }
            case LIGHT_ORB -> {
                float pulse = 1.0f + 0.15f * (float) Math.sin((entity.age + tickDelta) * 0.25f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin * 0.6f));
                matrices.scale(0.95f * scale * pulse, 0.95f * scale * pulse, 0.95f * scale * pulse);
                renderLayered(matrices, vc, 0xEEFFFFFF, auraColor);
            }
            case BLAST -> {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(spin * 0.7f));
                matrices.scale(scale, scale, scale);
                renderLayered(matrices, vc, coreColor, auraColor);
            }
        }
        matrices.pop();
    }

    private void renderLayered(MatrixStack matrices, VertexConsumer vc, int coreColor, int auraColor) {
        coreBox.render(matrices, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, coreColor);
        matrices.push();
        matrices.scale(1.28f, 1.28f, 1.28f);
        coreBox.render(matrices, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, auraColor);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(ConstructEntity entity) {
        return WHITE_TEX;
    }
}
