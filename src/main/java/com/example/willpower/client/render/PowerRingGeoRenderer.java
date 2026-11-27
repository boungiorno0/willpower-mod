package com.example.willpower.client.render;

import com.example.willpower.WillpowerMod;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.item.CorpsRingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.EnumMap;
import java.util.Map;

public class PowerRingGeoRenderer extends GeoItemRenderer<CorpsRingItem> {
    private static final Identifier MODEL_ID = WillpowerMod.id("power_ring");
    private static final Map<LanternCorps, Identifier> TEX_CACHE = new EnumMap<>(LanternCorps.class);

    public PowerRingGeoRenderer() {
        super(new DefaultedItemGeoModel<>(MODEL_ID));
    }

    @Override
    public Identifier getTextureLocation(CorpsRingItem animatable) {
        return TEX_CACHE.computeIfAbsent(animatable.getCorps(), PowerRingGeoRenderer::buildTexture);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        poseStack.push();
        if (mode == ModelTransformationMode.GUI) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.88f, 0.88f, 0.88f);
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(25f));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-35f));
            poseStack.translate(-0.5, -0.5, -0.5);
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.45f, 0.45f, 0.45f);
            poseStack.translate(-0.5, -0.5, -0.5);
        }
        poseStack.translate(-0.5, -0.45, -0.5);
        super.render(stack, mode, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.pop();
    }

    private static Identifier buildTexture(LanternCorps corps) {
        NativeImage img = new NativeImage(128, 128, false);
        int rgb = corps == LanternCorps.BLACK ? 0xD8DCE8 : corps.getBaseRgb();
        int cr = (rgb >> 16) & 0xFF, cg = (rgb >> 8) & 0xFF, cb = rgb & 0xFF;

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 16; x++) {
                int abgr;
                if (y < 16) {
                    int g = ((x + y) % 3 == 0) ? 46 : 60;
                    abgr = abgr(255, g, g + 3, g + 8);
                } else if (y < 32) {
                    abgr = abgr(255, cr / 2 + 40, cg / 2 + 40, cb / 2 + 40);
                } else if (y < 48 || y >= 96 && y < 112) {
                    abgr = abgr(255, Math.min(255, cr + 70), Math.min(255, cg + 70), Math.min(255, cb + 70));
                } else if (y < 64 || y >= 112) {
                    abgr = abgr(255, cr, cg, cb);
                } else {
                    abgr = (y % 4 < 2) ? abgr(255, cr, cg, cb) : abgr(255, 72, 76, 84);
                }
                img.setColor(x, y, abgr);
            }
        }
        Identifier id = WillpowerMod.id("textures/dynamic/ring_geo_" + corps.getId() + ".png");
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(img));
        return id;
    }

    private static int abgr(int a, int r, int g, int b) {
        return (a << 24) | (Math.clamp(b, 0, 255) << 16) | (Math.clamp(g, 0, 255) << 8) | Math.clamp(r, 0, 255);
    }
}
