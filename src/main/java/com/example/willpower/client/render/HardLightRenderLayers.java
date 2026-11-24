package com.example.willpower.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class HardLightRenderLayers extends RenderPhase {
    private HardLightRenderLayers() {
        super("willpower_render_phase", () -> {}, () -> {});
    }

    /** Светящийся голографический слой с текстурой (для эмблем, конструктов, щитов, глаз) */
    public static RenderLayer getHardLightGlow(Identifier texture) {
        return RenderLayer.of(
                "willpower_hard_light_glow",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(ENERGY_SWIRL_PROGRAM)
                        .texture(new Texture(texture, false, false))
                        .transparency(ADDITIVE_TRANSPARENCY)
                        .cull(DISABLE_CULLING)
                        .lightmap(ENABLE_LIGHTMAP)
                        .overlay(ENABLE_OVERLAY_COLOR)
                        .writeMaskState(COLOR_MASK)
                        .build(false)
        );
    }

    /** Аддитивный цветной слой без текстуры (для воксельного шлейфа, ауры и конуса Sonic Boom) */
    public static RenderLayer getVoxelGlow() {
        return RenderLayer.of(
                "willpower_voxel_glow",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(COLOR_PROGRAM)
                        .transparency(ADDITIVE_TRANSPARENCY)
                        .cull(DISABLE_CULLING)
                        .writeMaskState(COLOR_MASK)
                        .build(false)
        );
    }
}
