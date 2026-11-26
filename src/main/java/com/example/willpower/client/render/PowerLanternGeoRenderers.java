package com.example.willpower.client.render;

import com.example.willpower.WillpowerMod;
import com.example.willpower.block.PowerLanternBlockEntity;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.item.PowerLanternItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.Color;

public final class PowerLanternGeoRenderers {
    private static final Identifier MODEL_ID = WillpowerMod.id("power_lantern");
    private static final Identifier TEX_ID = WillpowerMod.id("textures/block/power_lantern.png");
    private static boolean texReady = false;

    private PowerLanternGeoRenderers() {}

    public static void ensureTexture() {
        if (texReady) return;
        NativeImage img = new NativeImage(64, 64, false);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                // Нижняя часть (y >= 34) — светящееся ядро, верх — металлический каркас
                if (y >= 34 && x < 24) {
                    int pulse = (x % 6 == 0 || y % 6 == 0) ? 235 : 255;
                    img.setColor(x, y, 0xFF000000 | (pulse << 16) | (pulse << 8) | pulse);
                } else {
                    int m = ((x + y) % 4 == 0) ? 145 : 175;
                    if (x == 0 || y == 0 || x == 63 || y == 63) m = 110;
                    img.setColor(x, y, 0xFF000000 | (m << 16) | (m << 8) | m);
                }
            }
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(TEX_ID, new NativeImageBackedTexture(img));
        texReady = true;
    }

    public static class BlockGeoRenderer extends GeoBlockRenderer<PowerLanternBlockEntity> {
        public BlockGeoRenderer() {
            super(new DefaultedBlockGeoModel<>(MODEL_ID));
            ensureTexture();
        }

        @Override
        public Color getRenderColor(PowerLanternBlockEntity animatable, float partialTick, int packedLight) {
            ensureTexture();
            LanternCorps c = animatable.getCorps();
            return Color.ofOpaque(c == LanternCorps.BLACK ? 0x626678 : c.getBaseRgb());
        }
    }

    public static class ItemGeoRenderer extends GeoItemRenderer<PowerLanternItem> {
        public ItemGeoRenderer() {
            super(new DefaultedBlockGeoModel<>(MODEL_ID));
            ensureTexture();
        }

        @Override
        public Color getRenderColor(PowerLanternItem animatable, float partialTick, int packedLight) {
            ensureTexture();
            LanternCorps c = animatable.getCorps();
            return Color.ofOpaque(c == LanternCorps.BLACK ? 0x626678 : c.getBaseRgb());
        }
    }
}
