package com.example.willpower.client.render;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.item.CorpsRingItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public class LanternVisualFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier WHITE_TEX = Identifier.of("minecraft", "textures/misc/white.png");

    private final ModelPart rRingBand, lRingBand, rRingGem, lRingGem;
    private final ModelPart auraHead, auraBody, auraRArm, auraLArm, auraLeg;

    public LanternVisualFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        this.rRingBand = box(-3.55f, 8.15f, -2.55f, 5.1f, 1.35f, 5.1f);
        this.lRingBand = box(-1.55f, 8.15f, -2.55f, 5.1f, 1.35f, 5.1f);
        this.rRingGem  = box(-2.05f, 8.05f, -2.92f, 2.1f, 1.55f, 0.55f);
        this.lRingGem  = box(-0.05f, 8.05f, -2.92f, 2.1f, 1.55f, 0.55f);

        float inf = 0.65f;
        this.auraHead = box(-4 - inf, -8 - inf, -4 - inf, 8 + inf * 2, 8 + inf * 2, 8 + inf * 2);
        this.auraBody = box(-4 - inf, -inf, -2 - inf, 8 + inf * 2, 12 + inf * 2, 4 + inf * 2);
        this.auraRArm = box(-3 - inf, -2 - inf, -2 - inf, 4 + inf * 2, 12 + inf * 2, 4 + inf * 2);
        this.auraLArm = box(-1 - inf, -2 - inf, -2 - inf, 4 + inf * 2, 12 + inf * 2, 4 + inf * 2);
        this.auraLeg  = box(-2 - inf, -inf, -2 - inf, 4 + inf * 2, 12 + inf * 2, 4 + inf * 2);
    }

    private static ModelPart box(float x, float y, float z, float dx, float dy, float dz) {
        ModelData d = new ModelData();
        d.getRoot().addChild("p", ModelPartBuilder.create().uv(0, 0).cuboid(x, y, z, dx, dy, dz), ModelTransform.NONE);
        return TexturedModelData.of(d, 16, 16).createModel();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        var model = getContextModel();

        // 1. 3D-кольца на руках из слотов Trinkets (или активный корпус на ведущей руке)
        LanternCorps mainRingCorps = findTrinketCorps(player, false);
        LanternCorps offRingCorps = findTrinketCorps(player, true);
        if (mainRingCorps == null && data.getActiveCorps() != null && player.getMainHandStack().getItem() instanceof CorpsRingItem r) {
            mainRingCorps = r.getCorps();
        }
        if (mainRingCorps != null || offRingCorps != null) {
            boolean leftMain = player.getMainArm() == Arm.LEFT;
            if (mainRingCorps != null) renderRingOnArm(matrices, consumers, light, leftMain ? model.leftArm : model.rightArm, leftMain, mainRingCorps);
            if (offRingCorps != null) renderRingOnArm(matrices, consumers, light, leftMain ? model.rightArm : model.leftArm, !leftMain, offRingCorps);
        }

        if (data.getActiveCorps() == null) return;
        int argb = data.getDisplayArgb();
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;

        // 2. Парящая комикс-эмблема (точное лого корпуса)
        if (data.isSuitActive() && data.isInsigniaActive()) {
            matrices.push();
            model.body.rotate(matrices);
            float dist = data.getInsigniaDistance();
            matrices.translate(0.0F, 0.22F, -0.17F - dist);
            matrices.scale(0.28F, 0.28F, 0.28F);
            VertexConsumer embVc = consumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(CorpsEmblemTextures.get(data.getActiveCorps())));
            drawQuad(matrices, embVc, r, g, b, 235);
            matrices.translate(0.0F, 0.0F, dist * 0.45F);
            drawQuad(matrices, embVc, r, g, b, 110);
            matrices.pop();
        }

        // 3. Светящиеся глаза
        long eyesMask = data.getGlowingEyesMask();
        if (eyesMask != 0L) {
            matrices.push();
            model.head.rotate(matrices);
            VertexConsumer eyeVc = consumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
            int er = data.isEyesCorpsColor() ? r : 255, eg = data.isEyesCorpsColor() ? g : 255, eb = data.isEyesCorpsColor() ? b : 255;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (((eyesMask >>> (row * 8 + col)) & 1L) != 0) {
                        drawFacePixel(matrices, eyeVc, -0.25F + col * 0.0625F, -0.50F + row * 0.0625F, -0.253F, 0.0625F, er, eg, eb, 255);
                    }
                }
            }
            matrices.pop();
        }

        // 4. Мягкая облегающая аура по силуэту всего тела (голова, тело, руки, ноги)
        if (data.isAuraActive() && data.getCharge() > 0) {
            int pulseAlpha = 38 + Math.round(14 * (float) Math.sin((player.age + tickDelta) * 0.25f));
            int auraColor = (pulseAlpha << 24) | (argb & 0x00FFFFFF);
            VertexConsumer auraVc = consumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(WHITE_TEX));
            renderPart(matrices, model.head, auraHead, auraVc, auraColor);
            renderPart(matrices, model.body, auraBody, auraVc, auraColor);
            renderPart(matrices, model.rightArm, auraRArm, auraVc, auraColor);
            renderPart(matrices, model.leftArm, auraLArm, auraVc, auraColor);
            renderPart(matrices, model.rightLeg, auraLeg, auraVc, auraColor);
            renderPart(matrices, model.leftLeg, auraLeg, auraVc, auraColor);
        }
    }

    private void renderRingOnArm(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                                 ModelPart arm, boolean isLeft, LanternCorps corps) {
        VertexConsumer metalVc = consumers.getBuffer(RenderLayer.getEntitySolid(WHITE_TEX));
        renderPartLit(matrices, arm, isLeft ? lRingBand : rRingBand, metalVc, light, 0xFF2B303A);
        VertexConsumer gemVc = consumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(WHITE_TEX));
        renderPartLit(matrices, arm, isLeft ? lRingGem : rRingGem, gemVc, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0xFF000000 | corps.getBaseRgb());
    }

    private static LanternCorps findTrinketCorps(AbstractClientPlayerEntity player, boolean offhand) {
        var opt = TrinketsApi.getTrinketComponent(player);
        if (opt.isEmpty()) return null;
        String groupName = offhand ? "offhand" : "hand";
        var group = opt.get().getInventory().get(groupName);
        if (group == null || !group.containsKey("ring")) return null;
        var inv = group.get("ring");
        for (int i = 0; i < inv.size(); i++) {
            ItemStack st = inv.getStack(i);
            if (st.getItem() instanceof CorpsRingItem ring) return ring.getCorps();
        }
        return null;
    }

    private static void renderPart(MatrixStack m, ModelPart anchor, ModelPart part, VertexConsumer vc, int color) {
        renderPartLit(m, anchor, part, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, color);
    }

    private static void renderPartLit(MatrixStack m, ModelPart anchor, ModelPart part, VertexConsumer vc, int light, int color) {
        m.push();
        anchor.rotate(m);
        part.render(m, vc, light, OverlayTexture.DEFAULT_UV, color);
        m.pop();
    }

    private static void drawQuad(MatrixStack m, VertexConsumer vc, int r, int g, int b, int a) {
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
}
