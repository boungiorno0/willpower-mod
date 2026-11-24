package com.example.willpower.client;

import com.example.willpower.ModBlocks;
import com.example.willpower.client.render.LanternVisualFeatureRenderer;
import com.example.willpower.client.render.VoxelTrailRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class WillpowerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WILL_CONSTRUCT, RenderLayer.getTranslucent());

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                registrationHelper.register(new LanternVisualFeatureRenderer(playerRenderer));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(VoxelTrailRenderer::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(VoxelTrailRenderer::render);
    }
}
