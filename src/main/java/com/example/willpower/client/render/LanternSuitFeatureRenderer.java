package com.example.willpower.client.render;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class LanternSuitFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier WHITE_TEX = Identifier.of("minecraft", "textures/misc/white.png");

    private final ModelPart maskPart;
    private final ModelPart torsoBase;
    private final ModelPart chestAccent;
    private final ModelPart rightSleeve;
    private final ModelPart leftSleeve;
    private final ModelPart rightGlove;
    private final ModelPart leftGlove;
    private final ModelPart legBase;
    private final ModelPart bootPart;

    public LanternSuitFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> ctx) {
        super(ctx);
        this.maskPart = box(-4.0f, -5.2f, -4.35f, 8.0f, 2.2f, 0.5f);
        this.torsoBase = box(-4.2f, 0.0f, -2.2f, 8.4f, 12.0f, 4.4f);
        this.chestAccent = box(-4.35f, 0.0f, -2.35f, 8.7f, 7.2f, 4.7f);
        this.rightSleeve = box(-3.15f, -2.0f, -2.15f, 4.3f, 6.5f, 4.3f);
        this.leftSleeve = box(-1.15f, -2.0f, -2.15f, 4.3f, 6.5f, 4.3f);
        this.rightGlove = box(-3.25f, 4.2f, -2.25f, 4.5f, 5.9f, 4.5f);
        this.leftGlove = box(-1.25f, 4.2f, -2.25f, 4.5f, 5.9f, 4.5f);
        this.legBase = box(-2.15f, 0.0f, -2.15f, 4.3f, 6.8f, 4.3f);
        this.bootPart = box(-2.30f, 6.5f, -2.30f, 4.6f, 5.6f, 4.6f);
    }

    private static ModelPart box(float x, float y, float z, float dx, float dy, float dz) {
        ModelData data = new ModelData();
        data.getRoot().addChild("p", ModelPartBuilder.create().uv(0, 0).cuboid(x, y, z, dx, dy, dz), ModelTransform.NONE);
        return TexturedModelData.of(data, 16, 16).createModel();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() == null || !data.isSuitActive()) return;
        if (data.isDesynced() && (player.age % 12 < 3)) return; // эффект сбоя при десинхронизации

        int rgb = data.getDisplayArgb() & 0x00FFFFFF;
        int darkBase = data.getSuitStyle() == 0 ? 0xFF14171D : 0xFF202733;
        int accentSolid = 0xFF000000 | rgb;
        int accentGlow = 0x66000000 | rgb;

        VertexConsumer solidVc = consumers.getBuffer(RenderLayer.getEntityTranslucent(WHITE_TEX));
        VertexConsumer glowVc = consumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
        var model = getContextModel();

        // Торс
        renderPart(matrices, model.body, torsoBase, solidVc, light, darkBase);
        renderPart(matrices, model.body, chestAccent, solidVc, light, accentSolid);
        if (data.getSuitStyle() == 1) {
            renderPart(matrices, model.body, chestAccent, glowVc, LightmapTextureManager.MAX_LIGHT_COORDINATE, accentGlow);
        }

        // Руки и перчатки
        renderPart(matrices, model.rightArm, rightSleeve, solidVc, light, darkBase);
        renderPart(matrices, model.leftArm, leftSleeve, solidVc, light, darkBase);
        renderPart(matrices, model.rightArm, rightGlove, solidVc, light, accentSolid);
        renderPart(matrices, model.leftArm, leftGlove, solidVc, light, accentSolid);

        // Ноги и сапоги
        renderPart(matrices, model.rightLeg, legBase, solidVc, light, darkBase);
        renderPart(matrices, model.leftLeg, legBase, solidVc, light, darkBase);
        renderPart(matrices, model.rightLeg, bootPart, solidVc, light, accentSolid);
        renderPart(matrices, model.leftLeg, bootPart, solidVc, light, accentSolid);

        // Маска с подгонкой высоты под скин
        if (data.isMaskActive()) {
            matrices.push();
            model.head.rotate(matrices);
            matrices.translate(0.0f, -data.getMaskHeight() * 0.0625f, 0.0f);
            maskPart.render(matrices, solidVc, light, OverlayTexture.DEFAULT_UV, accentSolid);
            matrices.pop();
        }
    }

    private static void renderPart(MatrixStack matrices, ModelPart anchor, ModelPart part,
                                   VertexConsumer vc, int light, int color) {
        matrices.push();
        anchor.rotate(matrices);
        part.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, color);
        matrices.pop();
    }
}
