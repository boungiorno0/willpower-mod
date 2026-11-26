package com.example.willpower.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

public final class LanternModelPlugin {
    private static final String RING_3D = buildRingJson();
    private static final String LANTERN_BUILTIN =
            "{\"parent\":\"minecraft:builtin/entity\",\"gui_light\":\"side\",\"display\":{"
            + "\"gui\":{\"rotation\":[25,45,0],\"translation\":[0,-1,0],\"scale\":[0.75,0.75,0.75]},"
            + "\"firstperson_righthand\":{\"rotation\":[0,45,0],\"translation\":[0,1.5,0],\"scale\":[0.45,0.45,0.45]},"
            + "\"thirdperson_righthand\":{\"rotation\":[15,45,0],\"translation\":[0,1.5,0.5],\"scale\":[0.45,0.45,0.45]}"
            + "}}";

    private LanternModelPlugin() {}

    public static void register() {
        ModelLoadingPlugin.register(pluginCtx -> pluginCtx.resolveModel().register(ctx -> {
            Identifier id = ctx.id();
            if (!"willpower".equals(id.getNamespace())) return null;
            String path = id.getPath();
            if (path.startsWith("item/") && path.endsWith("_ring") && !path.equals("item/will_ring")) {
                return JsonUnbakedModel.deserialize(RING_3D);
            }
            if (path.startsWith("item/") && path.endsWith("_lantern")) {
                return JsonUnbakedModel.deserialize(LANTERN_BUILTIN);
            }
            return null;
        }));
    }

    private static String buildRingJson() {
        return "{"
            + "\"gui_light\":\"side\","
            + "\"textures\":{\"metal\":\"minecraft:block/netherite_block\",\"gem\":\"minecraft:block/white_concrete\",\"particle\":\"minecraft:block/white_concrete\"},"
            + "\"elements\":["
            + box(5, 4.5f, 6, 11, 6, 10, "#metal", false) + ","
            + box(4.5f, 5.5f, 6, 6, 10.5f, 10, "#metal", false) + ","
            + box(10, 5.5f, 6, 11.5f, 10.5f, 10, "#metal", false) + ","
            + box(5, 10, 5.5f, 11, 11.8f, 10.5f, "#metal", false) + ","
            + box(5.8f, 11.3f, 6.2f, 10.2f, 12.8f, 9.8f, "#gem", true)
            + "],"
            + "\"display\":{"
            + "\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,-0.5,0],\"scale\":[1.45,1.45,1.45]},"
            + "\"firstperson_righthand\":{\"rotation\":[0,45,0],\"translation\":[0,2,0],\"scale\":[0.55,0.55,0.55]},"
            + "\"thirdperson_righthand\":{\"rotation\":[75,45,0],\"translation\":[0,2.5,0],\"scale\":[0.45,0.45,0.45]}"
            + "}}";
    }

    private static String box(float x1, float y1, float z1, float x2, float y2, float z2, String tex, boolean tint) {
        String f = tint ? "{\"texture\":\"" + tex + "\",\"tintindex\":0}" : "{\"texture\":\"" + tex + "\"}";
        return "{\"from\":[" + x1 + "," + y1 + "," + z1 + "],\"to\":[" + x2 + "," + y2 + "," + z2 + "],"
                + "\"faces\":{\"north\":" + f + ",\"south\":" + f + ",\"east\":" + f + ",\"west\":" + f + ",\"up\":" + f + ",\"down\":" + f + "}}";
    }
}
