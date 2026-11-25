package com.example.willpower.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

public final class LanternModelPlugin {
    private static final String RING_3D = buildRingJson();
    private static final String LANTERN_3D = buildLanternJson();

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
                return JsonUnbakedModel.deserialize(LANTERN_3D);
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

    private static String buildLanternJson() {
        return "{"
            + "\"gui_light\":\"side\","
            + "\"textures\":{\"metal\":\"minecraft:block/netherite_block\",\"gem\":\"minecraft:block/white_concrete\",\"particle\":\"minecraft:block/white_concrete\"},"
            + "\"elements\":["
            + box(4, 1, 4, 12, 3, 12, "#metal", false) + ","
            + box(4, 11, 4, 12, 13, 12, "#metal", false) + ","
            + box(5, 13, 7, 11, 15.5f, 9, "#metal", false) + ","
            + box(4.2f, 3, 4.2f, 5.6f, 11, 5.6f, "#metal", false) + ","
            + box(10.4f, 3, 4.2f, 11.8f, 11, 5.6f, "#metal", false) + ","
            + box(4.2f, 3, 10.4f, 5.6f, 11, 11.8f, "#metal", false) + ","
            + box(10.4f, 3, 10.4f, 11.8f, 11, 11.8f, "#metal", false) + ","
            + box(5.2f, 3, 5.2f, 10.8f, 11, 10.8f, "#gem", true)
            + "],"
            + "\"display\":{"
            + "\"gui\":{\"rotation\":[30,225,0],\"translation\":[0,-0.5,0],\"scale\":[0.85,0.85,0.85]},"
            + "\"firstperson_righthand\":{\"rotation\":[0,45,0],\"translation\":[0,2,0],\"scale\":[0.5,0.5,0.5]},"
            + "\"thirdperson_righthand\":{\"rotation\":[15,45,0],\"translation\":[0,2,1],\"scale\":[0.5,0.5,0.5]}"
            + "}}";
    }

    private static String box(float x1, float y1, float z1, float x2, float y2, float z2, String tex, boolean tint) {
        String f = tint ? "{\"texture\":\"" + tex + "\",\"tintindex\":0}" : "{\"texture\":\"" + tex + "\"}";
        return "{\"from\":[" + x1 + "," + y1 + "," + z1 + "],\"to\":[" + x2 + "," + y2 + "," + z2 + "],"
                + "\"faces\":{\"north\":" + f + ",\"south\":" + f + ",\"east\":" + f + ",\"west\":" + f + ",\"up\":" + f + ",\"down\":" + f + "}}";
    }
}
