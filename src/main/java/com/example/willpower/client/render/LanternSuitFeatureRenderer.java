package com.example.willpower.client.render;

import com.example.willpower.WillpowerMod;
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

    private final ModelPart helmetTop, helmetSides, visorStrip;
    private final ModelPart torsoBase, collar, chestPlate, absBelt, chestEmblem;
    private final ModelPart rSleeve, lSleeve, rPauldron, lPauldron, rGauntlet, lGauntlet;
    private final ModelPart legBase, thighPlate, kneeGuard, bootShell;

    public LanternSuitFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> ctx) {
        super(ctx);
        this.helmetTop = box(-4.35f, -8.35f, -4.35f, 8.7f, 2.8f, 8.7f);
        this.helmetSides = box(-4.4f, -6.0f, -2.5f, 8.8f, 6.0f, 6.9f);
        this.visorStrip = box(-4.15f, -5.6f, -4.42f, 8.3f, 2.1f, 0.4f);

        this.torsoBase = box(-4.15f, 0.0f, -2.15f, 8.3f, 12.0f, 4.3f);
        this.collar = box(-4.5f, -0.9f, -2.5f, 9.0f, 1.5f, 5.0f);
        this.chestPlate = box(-4.45f, 0.5f, -2.55f, 8.9f, 5.8f, 5.1f);
        this.absBelt = box(-4.3f, 6.5f, -2.35f, 8.6f, 4.8f, 4.7f);
        this.chestEmblem = box(-1.45f, 1.7f, -2.72f, 2.9f, 2.9f, 0.25f);

        this.rSleeve = box(-3.15f, -2.0f, -2.15f, 4.3f, 12.0f, 4.3f);
        this.lSleeve = box(-1.15f, -2.0f, -2.15f, 4.3f, 12.0f, 4.3f);
        this.rPauldron = box(-3.65f, -2.45f, -2.5f, 5.0f, 4.1f, 5.0f);
        this.lPauldron = box(-1.35f, -2.45f, -2.5f, 5.0f, 4.1f, 5.0f);
        this.rGauntlet = box(-3.42f, 4.1f, -2.42f, 4.65f, 5.1f, 4.84f);
        this.lGauntlet = box(-1.23f, 4.1f, -2.42f, 4.65f, 5.1f, 4.84f);

        this.legBase = box(-2.15f, 0.0f, -2.15f, 4.3f, 12.0f, 4.3f);
        this.thighPlate = box(-2.38f, 0.6f, -2.38f, 4.76f, 3.6f, 4.76f);
        this.kneeGuard = box(-2.25f, 4.6f, -2.55f, 4.5f, 2.5f, 4.8f);
        this.bootShell = box(-2.42f, 7.6f, -2.48f, 4.84f, 4.4f, 4.96f);
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
                    int shade = 205;
                    if (x == 0 || y == 0 || x == 15 || y == 15) shade = 95;
                    else if (x == 1 || y == 1) shade = 248;
                    else if (x == 14 || y == 14 || y == 7) shade = 140;
                    else if (((x + y) & 1) == 0) shade = 190;
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
        if (corps == null || !data.isSuitActive()) return;
        if (data.isDesynced() && (player.age % 12 < 3)) return;

        int rgb = data.getDisplayArgb() & 0x00FFFFFF;
        boolean comic = data.getSuitStyle() == 0;
        int underColor = 0xFF15181E;
        int plateColor = switch (corps) {
            case BLACK -> 0xFF424452;
            case WHITE -> 0xFFE6F0F4;
            default -> comic ? (0xFF000000 | rgb) : scaleRgb(rgb, 0.78f);
        };
        int emblemGlow = switch (corps) {
            case BLACK -> 0xDDE5EBF5;
            case WHITE -> 0xEEFFFFFF;
            default -> 0xCC000000 | rgb;
        };

        var model = getContextModel();
        VertexConsumer solidVc = consumers.getBuffer(RenderLayer.getEntityTranslucent(getExoTexture()));

        // Основа (тёмный экзо-подкостюм)
        part(matrices, model.body, torsoBase, solidVc, light, underColor);
        part(matrices, model.body, absBelt, solidVc, light, 0xFF1D2028);
        part(matrices, model.rightArm, rSleeve, solidVc, light, underColor);
        part(matrices, model.leftArm, lSleeve, solidVc, light, underColor);
        part(matrices, model.rightLeg, legBase, solidVc, light, underColor);
        part(matrices, model.leftLeg, legBase, solidVc, light, underColor);

        // Цветные бронепластины
        part(matrices, model.body, collar, solidVc, light, plateColor);
        part(matrices, model.body, chestPlate, solidVc, light, plateColor);
        part(matrices, model.rightArm, rPauldron, solidVc, light, plateColor);
        part(matrices, model.leftArm, lPauldron, solidVc, light, plateColor);
        part(matrices, model.rightArm, rGauntlet, solidVc, light, plateColor);
        part(matrices, model.leftArm, lGauntlet, solidVc, light, plateColor);

        for (ModelPart leg : new ModelPart[]{model.rightLeg, model.leftLeg}) {
            part(matrices, leg, thighPlate, solidVc, light, plateColor);
            part(matrices, leg, kneeGuard, solidVc, light, plateColor);
            part(matrices, leg, bootShell, solidVc, light, plateColor);
        }

        if (data.isMaskActive()) {
            part(matrices, model.head, helmetTop, solidVc, light, plateColor);
            part(matrices, model.head, helmetSides, solidVc, light, underColor);
        }

        // Светящиеся элементы (визор и нагрудный знак) — строго отдельным слоем после solidVc
        VertexConsumer glowVc = consumers.getBuffer(HardLightRenderLayers.getHardLightGlow(WHITE_TEX));
        part(matrices, model.body, chestEmblem, glowVc, LightmapTextureManager.MAX_LIGHT_COORDINATE, emblemGlow);
        if (data.isMaskActive()) {
            matrices.push();
            model.head.rotate(matrices);
            matrices.translate(0.0f, -data.getMaskHeight() * 0.0625f, 0.0f);
            visorStrip.render(matrices, glowVc, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0x88353942);
            matrices.pop();
        }
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
