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

    private final ModelPart maskPart, torsoBase, chestAccent, collar, belt, buckle;
    private final ModelPart rightSleeve, leftSleeve, rightPauldron, leftPauldron, rightGlove, leftGlove;
    private final ModelPart legBase, bootPart;

    public LanternSuitFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> ctx) {
        super(ctx);
        this.maskPart = box(-4.0f, -5.2f, -4.35f, 8.0f, 2.2f, 0.5f);
        this.torsoBase = box(-4.2f, 0.0f, -2.2f, 8.4f, 12.0f, 4.4f);
        this.chestAccent = box(-4.35f, 0.4f, -2.42f, 8.7f, 6.4f, 4.84f);
        this.collar = box(-4.35f, -0.7f, -2.35f, 8.7f, 1.3f, 4.7f);
        this.belt = box(-4.32f, 9.6f, -2.35f, 8.64f, 1.7f, 4.7f);
        this.buckle = box(-1.1f, 9.5f, -2.52f, 2.2f, 1.9f, 0.25f);

        this.rightSleeve = box(-3.15f, -2.0f, -2.15f, 4.3f, 10.5f, 4.3f);
        this.leftSleeve = box(-1.15f, -2.0f, -2.15f, 4.3f, 10.5f, 4.3f);
        this.rightPauldron = box(-3.55f, -2.35f, -2.4f, 4.8f, 3.6f, 4.8f);
        this.leftPauldron = box(-1.25f, -2.35f, -2.4f, 4.8f, 3.6f, 4.8f);
        this.rightGlove = box(-3.28f, 4.6f, -2.28f, 4.56f, 5.5f, 4.56f);
        this.leftGlove = box(-1.28f, 4.6f, -2.28f, 4.56f, 5.5f, 4.56f);

        this.legBase = box(-2.15f, 0.0f, -2.15f, 4.3f, 11.8f, 4.3f);
        this.bootPart = box(-2.32f, 6.8f, -2.32f, 4.64f, 5.3f, 4.64f);
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
        if (data.isDesynced() && (player.age % 12 < 3)) return;

        int rgb = data.getDisplayArgb() & 0x00FFFFFF;
        boolean tactical = data.getSuitStyle() == 1;
        int darkBase = tactical ? 0xFF171A20 : 0xFF101318;
        int armorTone = tactical ? scaleRgb(rgb, 0.52f) : (0xFF000000 | rgb);
        int trimTone = tactical ? scaleRgb(rgb, 0.78f) : (0xFF000000 | rgb);
        int glowTone = 0x77000000 | rgb;
        var model = getContextModel();

        // 1. Сначала рисуем все обычные детали одним слоем solidVc
        VertexConsumer solidVc = consumers.getBuffer(RenderLayer.getEntityTranslucent(WHITE_TEX));
        renderPart(matrices, model.body, torsoBase, solidVc, light, darkBase);
        renderPart(matrices, model.body, chestAccent, solidVc, light, armorTone);
        renderPart(matrices, model.body, belt, solidVc, light, tactical ? 0xFF111317 : armorTone);
        if (tactical) {
            renderPart(matrices, model.body, collar, solidVc, light, darkBase);
            renderPart(matrices, model.rightArm, rightPauldron, solidVc, light, armorTone);
            renderPart(matrices, model.leftArm, leftPauldron, solidVc, light, armorTone);
        }
        renderPart(matrices, model.rightArm, rightSleeve, solidVc, light, darkBase);
        renderPart(matrices, model.leftArm, leftSleeve, solidVc, light, darkBase);
        renderPart(matrices, model.rightArm, rightGlove, solidVc, light, tactical ? darkBase : trimTone);
        renderPart(matrices, model.leftArm, leftGlove, solidVc, light, tactical ? darkBase : trimTone);
        renderPart(matrices, model.rightLeg, legBase, solidVc, light, darkBase);
        renderPart(matrices, model.leftLeg, legBase, solidVc, light, darkBase);
        renderPart(matrices, model.rightLeg, bootPart, solidVc, light, tactical ? 0xFF121419 : trimTone);
        renderPart(matrices, model.leftLeg, bootPart, solidVc, light, tactical ? 0xFF121419 : trimTone);

        if (data.isMaskActive()) {
            matrices.push();
            model.head.rotate(matrices);
            matrices.translate(0.0f, -data.getMaskHeight() * 0.0625f, 0.0f);
            maskPart.render(matrices, solidVc, light, OverlayTexture.DEFAULT_UV, trimTone);
            matrices.pop();
        }

        // 2. Только после solidVc берём glowVc для светящейся пряжки
        VertexConsumer glowVc = consumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
        renderPart(matrices, model.body, buckle, glowVc, LightmapTextureManager.MAX_LIGHT_COORDINATE, glowTone);
    }

    private static int scaleRgb(int rgb, float factor) {
        int r = Math.clamp(Math.round(((rgb >> 16) & 0xFF) * factor), 0, 255);
        int g = Math.clamp(Math.round(((rgb >> 8) & 0xFF) * factor), 0, 255);
        int b = Math.clamp(Math.round((rgb & 0xFF) * factor), 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static void renderPart(MatrixStack matrices, ModelPart anchor, ModelPart part,
                                   VertexConsumer vc, int light, int color) {
        matrices.push();
        anchor.rotate(matrices);
        part.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, color);
        matrices.pop();
    }
}
