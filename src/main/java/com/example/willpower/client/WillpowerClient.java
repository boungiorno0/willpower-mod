package com.example.willpower.client;

import com.example.willpower.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class WillpowerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Конструкты полупрозрачные
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WILL_CONSTRUCT, RenderLayer.getTranslucent());
    }
}
