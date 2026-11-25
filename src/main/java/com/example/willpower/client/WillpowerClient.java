package com.example.willpower.client;

import com.example.willpower.ModBlocks;
import com.example.willpower.client.render.*;
import com.example.willpower.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class WillpowerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WILL_CONSTRUCT, RenderLayer.getTranslucent());
        EntityRendererRegistry.register(ModEntities.CONSTRUCT, ConstructEntityRenderer::new);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                registrationHelper.register(new LanternSuitFeatureRenderer(playerRenderer));
                registrationHelper.register(new LanternVisualFeatureRenderer(playerRenderer));
            }
        });

        HudRenderCallback.EVENT.register(new LanternHudRenderer());
        ClientTickEvents.END_CLIENT_TICK.register(VoxelTrailRenderer::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(VoxelTrailRenderer::render);
    }
}
