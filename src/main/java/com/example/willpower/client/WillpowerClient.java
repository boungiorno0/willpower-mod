package com.example.willpower.client;

import com.example.willpower.ModBlocks;
import com.example.willpower.ModCorpsRegistry;
import com.example.willpower.client.render.*;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
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

        for (LanternCorps corps : LanternCorps.values()) {
            int tint = 0xFF000000 | corps.getBaseRgb();
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? tint : -1,
                    ModCorpsRegistry.RINGS.get(corps),
                    ModCorpsRegistry.LANTERNS.get(corps));
        }

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                registrationHelper.register(new LanternSuitFeatureRenderer(playerRenderer));
                registrationHelper.register(new LanternVisualFeatureRenderer(playerRenderer));
            }
        });

        LanternKeybinds.init();
        HudRenderCallback.EVENT.register(new LanternHudRenderer());
        ClientTickEvents.END_CLIENT_TICK.register(VoxelTrailRenderer::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(VoxelTrailRenderer::render);
    }
}
