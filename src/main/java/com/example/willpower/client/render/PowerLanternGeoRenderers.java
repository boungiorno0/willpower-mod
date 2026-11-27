package com.example.willpower.client.render;

import com.example.willpower.WillpowerMod;
import com.example.willpower.block.PowerLanternBlockEntity;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.item.PowerLanternItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.EnumMap;
import java.util.Map;

public final class PowerLanternGeoRenderers {
    private static final Identifier MODEL_ID = WillpowerMod.id("power_lantern");
    private static final Map<LanternCorps, Identifier> TEX_CACHE = new EnumMap<>(LanternCorps.class);

    private PowerLanternGeoRenderers() {}

    public static Identifier getTexture(LanternCorps corps) {
        return TEX_CACHE.computeIfAbsent(corps, PowerLanternGeoRenderers::buildTexture);
    }

    private static Identifier buildTexture(LanternCorps corps) {
        NativeImage img = new NativeImage(128, 128, false);
        int rgb = corps.getBaseRgb();
        int cr = (rgb >> 16) & 0xFF, cg = (rgb >> 8) & 0xFF, cb = rgb & 0xFF;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 128; x++) {
                int abgr;
                if (x < 16) {
                    // [0,0] Состаренная бронза (оправа, обручи, крышка, ручка)
                    int edge = (x == 0 || y == 0 || x == 15 || y == 15) ? -25 : ((x + y) % 3 == 0 ? 12 : 0);
                    abgr = abgr(255, 138 + edge, 114 + edge, 72 + edge / 2);
                } else if (x < 32) {
                    // [16,0] Тёмная сталь / переходные кольца
                    int v = ((x + y) % 2 == 0) ? 88 : 102;
                    abgr = abgr(255, v, v + 4, v + 8);
                } else if (x < 48) {
                    // [32,0] Яркое светящееся ядро в цвет корпуса
                    int boost = (y % 4 == 0) ? 55 : 25;
                    abgr = abgr(255, Math.min(255, cr + boost), Math.min(255, cg + boost), Math.min(255, cb + boost));
                } else if (x < 64) {
                    // [48,0] Стойки и тёмный фон медальона
                    abgr = abgr(255, 52, 54, 62);
                } else if (x < 80) {
                    // [64,0] Объёмное лого спереди (яркое свечение)
                    abgr = abgr(255, Math.min(255, cr + 85), Math.min(255, cg + 85), Math.min(255, cb + 85));
                } else {
                    // [96,0] Нижнее тёмное основание и зажим ручки
                    abgr = abgr(255, 42, 40, 38);
                }
                img.setColor(x, y, abgr);
            }
        }
        Identifier id = WillpowerMod.id("textures/dynamic/lantern_" + corps.getId() + ".png");
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(img));
        return id;
    }

    private static int abgr(int a, int r, int g, int b) {
        return (a << 24) | (Math.clamp(b, 0, 255) << 16) | (Math.clamp(g, 0, 255) << 8) | Math.clamp(r, 0, 255);
    }

    public static class BlockGeoRenderer extends GeoBlockRenderer<PowerLanternBlockEntity> {
        public BlockGeoRenderer() {
            super(new DefaultedBlockGeoModel<>(MODEL_ID));
        }

        @Override
        public Identifier getTextureLocation(PowerLanternBlockEntity animatable) {
            return getTexture(animatable.getCorps());
        }

        @Override
        public void defaultRender(MatrixStack poseStack, PowerLanternBlockEntity animatable,
                                  VertexConsumerProvider bufferSource, net.minecraft.client.render.RenderLayer renderType,
                                  net.minecraft.client.render.VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
            poseStack.push();
            poseStack.translate(-0.5, 0.0, -0.5);
            super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
            poseStack.pop();
        }
    }

    public static class ItemGeoRenderer extends GeoItemRenderer<PowerLanternItem> {
        public ItemGeoRenderer() {
            super(new DefaultedBlockGeoModel<>(MODEL_ID));
        }

        @Override
        public Identifier getTextureLocation(PowerLanternItem animatable) {
            return getTexture(animatable.getCorps());
        }

        @Override
        public void renderByItem(ItemStack stack, ModelTransformationMode mode, MatrixStack poseStack,
                                 VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
            poseStack.push();
            poseStack.translate(-0.5, 0.0, -0.5);

            // В руке держим за верхнюю рукоять (опускаем корпус ниже кисти) и качаем при ходьбе!
            if (mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND
                    || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND) {
                var player = MinecraftClient.getInstance().player;
                float swing = player != null
                        ? (float) Math.sin(player.limbAnimator.getPos() * 0.66f) * player.limbAnimator.getSpeed() * 18.0f
                        : 0.0f;
                poseStack.translate(0.5, 0.85, 0.5);
                poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swing));
                poseStack.translate(-0.5, -1.65, -0.5);
            }

            super.renderByItem(stack, mode, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.pop();
        }
    }
}
