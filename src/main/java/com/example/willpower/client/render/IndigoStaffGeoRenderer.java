package com.example.willpower.client.render;

import com.example.willpower.WillpowerMod;
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

public class IndigoStaffGeoRenderer extends GeoItemRenderer<CorpsRingItem> {
    private static final Identifier MODEL_ID = WillpowerMod.id("indigo_staff");
    private static final Identifier TEX_ID = WillpowerMod.id("textures/dynamic/indigo_staff_geo.png");
    private static boolean texReady = false;

    public IndigoStaffGeoRenderer() {
        super(new DefaultedItemGeoModel<>(MODEL_ID));
    }

    @Override
    public Identifier getTextureLocation(CorpsRingItem animatable) {
        if (!texReady) buildTexture();
        return TEX_ID;
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        poseStack.push();
        if (mode == ModelTransformationMode.GUI) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.45f, 0.45f, 0.45f);
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45f));
            poseStack.translate(-0.5, -1.0, -0.5);
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.85f, 0.85f, 0.85f);
            poseStack.translate(-0.5, -0.85, -0.5);
        }
        poseStack.translate(-0.5, 0.0, -0.5);
        super.render(stack, mode, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.pop();
    }

    private static void buildTexture() {
        NativeImage img = new NativeImage(128, 128, false);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 16; x++) {
                int abgr;
                if (y < 16) {
                    int w = ((x + y) % 3 == 0) ? 58 : 74;
                    abgr = (255 << 24) | ((w - 8) << 16) | ((w - 14) << 8) | w; // тёмное древко
                } else if (y < 32) {
                    abgr = (255 << 24) | (68 << 16) | (106 << 8) | 128; // бронзовые кольца
                } else if (y < 48) {
                    abgr = (255 << 24) | (235 << 16) | (45 << 8) | 130; // индиго кольца
                } else {
                    abgr = (255 << 24) | (255 << 16) | (120 << 8) | 195; // сияющий фиолетовый кристалл
                }
                img.setColor(x, y, abgr);
            }
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(TEX_ID, new NativeImageBackedTexture(img));
        texReady = true;
    }
}
