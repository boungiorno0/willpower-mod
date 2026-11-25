package com.example.willpower.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

public final class LanternModelPlugin {
    private static final String RING_JSON =
            "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"minecraft:item/firework_star_overlay\"}}";
    private static final String LANTERN_JSON =
            "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"minecraft:item/lantern\"}}";

    private LanternModelPlugin() {}

    public static void register() {
        ModelLoadingPlugin.register(pluginCtx -> pluginCtx.resolveModel().register(ctx -> {
            Identifier id = ctx.id();
            if (!"willpower".equals(id.getNamespace())) return null;
            String path = id.getPath();
            if (path.startsWith("item/") && path.endsWith("_ring") && !path.equals("item/will_ring")) {
                return JsonUnbakedModel.deserialize(RING_JSON);
            }
            if (path.startsWith("item/") && path.endsWith("_lantern")) {
                return JsonUnbakedModel.deserialize(LANTERN_JSON);
            }
            return null;
        }));
    }
}
