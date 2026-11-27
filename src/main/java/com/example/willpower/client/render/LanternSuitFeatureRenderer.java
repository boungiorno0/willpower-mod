package com.example.willpower.client.render;

import com.example.willpower.WillpowerMod;
import com.example.willpower.client.SuitAnimState;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class LanternSuitFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier WHITE_TEX = Identifier.of("minecraft", "textures/misc/white.png");
    private static final Identifier TACTICAL_TEX = WillpowerMod.id("textures/dynamic/exo_armor.png");
    private static boolean textureRegistered = false;

    private final ModelPart dominoMask, scanWave;
    private final ModelPart torsoBase, collar, chestPlate, absBelt;
    private final ModelPart rSleeve, lSleeve, rPauldron, lPauldron, rGauntlet, lGauntlet;
    private final ModelPart legBase, thighPlate, kneeGuard, bootShell;

    public LanternSuitFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> ctx) {
        super(ctx);
        // Аккуратная домино-маска поверх глаз, двигается настройкой высоты маски
        this.dominoMask = box(-4.05f, -5.0f, -4.18f, 8.1f, 2.0f, 0.38f);
        this.scanWave   = box(-5.2f, -0.45f, -3.4f, 10.4f, 0.9f, 6.8f);

        this.torsoBase  = box(-4.15f, 0.0f, -2.15f, 8.3f, 12.0f, 4.3f);
        this.collar     = box(-4.45f, -0.8f, -2.45f, 8.9f, 1.3f, 4.9f);
        this.chestPlate = box(-4.40f, 0.5f, -2.50f, 8.8f, 5.8f, 5.0f);
        this.absBelt    = box(-4.28f, 6.5f, -2.32f, 8.56f, 4.8f, 4.64f);

        this.rSleeve    = box(-3.15f, -2.0f, -2.15f, 4.3f, 12.0f, 4.3f);
        this.lSleeve    = box(-1.15f, -2.0f, -2.15f, 4.3f, 12.0f, 4.3f);
        this.rPauldron  = box(-3.60f, -2.4f, -2.45f, 4.9f, 3.9f, 4.9f);
        this.lPauldron  = box(-1.30f, -2.4f, -2.45f, 4.9f, 3.9f, 4.9f);
        this.rGauntlet  = box(-3.38f, 4.1f, -2.38f, 4.58f, 5.0f, 4.76f);
        this.lGauntlet  = box(-1.20f, 4.1f, -2.38f, 4.58f, 5.0f, 4.76f);

        this.legBase    = box(-2.15f, 0.0f, -2.15f, 4.3f, 12.0f, 4.3f);
        this.thighPlate = box(-2.35f, 0.6f, -2.35f, 4.7f, 3.6f, 4.7f);
        this.kneeGuard  = box(-2.22f, 4.6f, -2.50f, 4.44f, 2.5f, 4.7f);
        this.bootShell  = box(-2.38f, 7.6f, -2.44f, 4.76f, 4.4f, 4.88f);
    }

    private static ModelPart box(float x, float y, float z, float dx, float dy, float dz) {
        ModelData data = new ModelData();
        data.getRoot().addChild("p", ModelPartBuilder.create().uv(0, 0).cuboid(x, y, z, dx, dy, dz), ModelTransform.NONE);
        return TexturedModelData.of(data, 16, 16).createModel();
    }

    private static Identifier getExoTexture() {
        if (!textureRegistered) {
            NativeImage img = new NativeImage(16, 16, false);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int shade = 215;
                    if (x == 0 || y == 0 || x == 15 || y == 15) shade = 110;
                    else if (x == 1 || y == 1) shade = 252;
                    else if (x == 14 || y == 14 || y == 7) shade = 155;
                    else if (((x + y) & 1) == 0) shade = 200;
                    img.setColor(x, y, 0xFF000000 | (shade << 16) | (shade << 8) | shade);
                }
            }
            MinecraftClient.getInstance().getTextureManager().registerTexture(TACTICAL_TEX, new NativeImageBackedTexture(img));
            textureRegistered = true;
        }
        return TACTICAL_TEX;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        LanternCorps corps = data.getActiveCorps();
        if (corps == null) return;

        float anim = SuitAnimState.getProgress(player, data.isSuitActive());
        if (anim <= 0.01f) return;
        if (data.isDesynced() && (player.age % 12 < 3)) return;

        int rgb = data.getDisplayArgb() & 0x00FFFFFF;
        boolean comic = data.getSuitStyle() == 0;
        int underColor = 0xFF161A20;
        int plateColor = switch (corps) {
            case BLACK -> scaleRgb(0x464858, 1.0f + data.getBrightness());
            case WHITE -> scaleRgb(0xEEF6FA, 1.0f + data.getBrightness() * 0.4f);
            default -> comic ? (0xFF000000 | rgb) : scaleRgb(rgb, 0.85f);
        };

        float legA = SuitAnimState.sectionAlpha(anim, 0.02f);
        float torsoA = SuitAnimState.sectionAlpha(anim, 0.25f);
        float headA = SuitAnimState.sectionAlpha(anim, 0.55f);

        var model = getContextModel();
        VertexConsumer solidVc = consumers.getBuffer(RenderLayer.getEntityTranslucent(getExoTexture()));

        if (legA > 0.02f) {
            int lUnder = withAlpha(underColor, legA), lPlate = withAlpha(plateColor, legA);
            for (ModelPart leg : new ModelPart[]{model.rightLeg, model.leftLeg}) {
                part(matrices, leg, legBase, solidVc, light, lUnder);
                part(matrices, leg, thighPlate, solidVc, light, lPlate);
                part(matrices, leg, kneeGuard, solidVc, light, lPlate);
                part(matrices, leg, bootShell, solidVc, light, lPlate);
            }
        }

        if (torsoA > 0.02f) {
            int tUnder = withAlpha(underColor, torsoA), tPlate = withAlpha(plateColor, torsoA);
            part(matrices, model.body, torsoBase, solidVc, light, tUnder);
            part(matrices, model.body, absBelt, solidVc, light, withAlpha(0xFF1D2028, torsoA));
            part(matrices, model.body, collar, solidVc, light, tPlate);
            part(matrices, model.body, chestPlate, solidVc, light, tPlate);
            part(matrices, model.rightArm, rSleeve, solidVc, light, tUnder);
            part(matrices, model.leftArm, lSleeve, solidVc, light, tUnder);
            part(matrices, model.rightArm, rPauldron, solidVc, light, tPlate);
            part(matrices, model.leftArm, lPauldron, solidVc, light, tPlate);
            part(matrices, model.rightArm, rGauntlet, solidVc, light, tPlate);
            part(matrices, model.leftArm, lGauntlet, solidVc, light, tPlate);
        }

        if (data.isMaskActive() && headA > 0.02f) {
            matrices.push();
            model.head.rotate(matrices);
            matrices.translate(0.0f, -data.getMaskHeight() * 0.0625f, 0.0f);
            dominoMask.render(matrices, solidVc, light, OverlayTexture.DEFAULT_UV, withAlpha(plateColor, headA));
            matrices.pop();
        }

        // Уникальное светящееся лого корпуса на груди
        if (headA > 0.05f) {
            matrices.push();
            model.body.rotate(matrices);
            matrices.translate(0.0f, 0.21f, -0.165f);
            matrices.scale(0.24f, 0.24f, 0.24f);
            VertexConsumer emblemVc = consumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(CorpsEmblemTextures.get(corps)));
            int er = (corps == LanternCorps.BLACK || corps == LanternCorps.WHITE) ? 255 : Math.min(255, ((rgb >> 16) & 0xFF) + 70);
            int eg = (corps == LanternCorps.BLACK || corps == LanternCorps.WHITE) ? 255 : Math.min(255, ((rgb >> 8) & 0xFF) + 70);
            int eb = (corps == LanternCorps.BLACK || corps == LanternCorps.WHITE) ? 255 : Math.min(255, (rgb & 0xFF) + 70);
            int ea = Math.round(255 * headA);
            var entry = matrices.peek();
            emblemVc.vertex(entry, -0.5f, -0.5f, 0).color(er, eg, eb, ea).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry, 0, 0, -1);
            emblemVc.vertex(entry,  0.5f, -0.5f, 0).color(er, eg, eb, ea).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry, 0, 0, -1);
            emblemVc.vertex(entry,  0.5f,  0.5f, 0).color(er, eg, eb, ea).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry, 0, 0, -1);
            emblemVc.vertex(entry, -0.5f,  0.5f, 0).color(er, eg, eb, ea).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry, 0, 0, -1);
            matrices.pop();
        }

        // Светящаяся волна-сканер материализации снизу вверх (пока anim между 0.02 и 0.98)
        if (anim > 0.02f && anim < 0.98f) {
            VertexConsumer waveVc = consumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
            matrices.push();
            model.body.rotate(matrices);
            float waveY = 1.5f - anim * 2.0f;
            matrices.translate(0.0f, waveY, 0.0f);
            scanWave.render(matrices, waveVc, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0xCC000000 | rgb);
            matrices.pop();
        }
    }

    private static int withAlpha(int argb, float factor) {
        int a = Math.clamp(Math.round(((argb >>> 24) & 0xFF) * factor), 0, 255);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private static int scaleRgb(int rgb, float factor) {
        int r = Math.clamp(Math.round(((rgb >> 16) & 0xFF) * factor), 0, 255);
        int g = Math.clamp(Math.round(((rgb >> 8) & 0xFF) * factor), 0, 255);
        int b = Math.clamp(Math.round((rgb & 0xFF) * factor), 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static void part(MatrixStack matrices, ModelPart anchor, ModelPart piece, VertexConsumer vc, int light, int color) {
        matrices.push();
        anchor.rotate(matrices);
        piece.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, color);
        matrices.pop();
    }
}
