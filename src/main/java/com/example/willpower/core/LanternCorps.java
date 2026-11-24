package com.example.willpower.core;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public enum LanternCorps {
    GREEN("green", "Green Lantern Corps", "Willpower", 0x00FF44, Blocks.OXIDIZED_COPPER),
    YELLOW("yellow", "Sinestro Corps", "Fear", 0xFFD000, Blocks.GOLD_BLOCK),
    BLUE("blue", "Blue Lantern Corps", "Hope", 0x1E90FF, Blocks.LAPIS_BLOCK),
    RED("red", "Red Lantern Corps", "Rage", 0xFF1A1A, Blocks.REDSTONE_BLOCK),
    ORANGE("orange", "Orange Lantern Corps", "Avarice", 0xFF7700, Blocks.COPPER_BLOCK),
    INDIGO("indigo", "Indigo Tribe", "Compassion", 0x7B00FF, Blocks.AMETHYST_BLOCK),
    STAR_SAPPHIRE("violet", "Star Sapphire Corps", "Love", 0xFF3EB5, Blocks.PURPUR_BLOCK),
    WHITE("white", "White Lantern Corps", "Life", 0xF2FFFF, Blocks.SEA_LANTERN),
    BLACK("black", "Black Lantern Corps", "Death", 0x262632, Blocks.CRYING_OBSIDIAN);

    private final String id;
    private final String displayName;
    private final String emotionName;
    private final int baseRgb;
    private final Block batteryBlock;

    LanternCorps(String id, String displayName, String emotionName, int baseRgb, Block batteryBlock) {
        this.id = id;
        this.displayName = displayName;
        this.emotionName = emotionName;
        this.baseRgb = baseRgb;
        this.batteryBlock = batteryBlock;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmotionName() { return emotionName; }
    public int getBaseRgb() { return baseRgb; }
    public Block getBatteryBlock() { return batteryBlock; }

    public Text getFormattedName() {
        return Text.literal(displayName).styled(s -> s.withColor(baseRgb));
    }

    public Text getOathText() {
        return Text.translatable("oath.willpower." + id);
    }

    public Identifier getInsigniaTexture() {
        return Identifier.of("willpower", "textures/insignia/" + id + ".png");
    }

    public static LanternCorps byId(String id) {
        for (LanternCorps corps : values()) {
            if (corps.id.equalsIgnoreCase(id) || corps.name().equalsIgnoreCase(id)) {
                return corps;
            }
        }
        return GREEN;
    }

    public int getTintedArgb(float brightnessDelta, float saturationDelta) {
        float r = ((baseRgb >> 16) & 0xFF) / 255.0f;
        float g = ((baseRgb >> 8) & 0xFF) / 255.0f;
        float b = (baseRgb & 0xFF) / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0f;
        if (delta > 1e-5f) {
            if (max == r) h = ((g - b) / delta) % 6f;
            else if (max == g) h = ((b - r) / delta) + 2f;
            else h = ((r - g) / delta) + 4f;
            h /= 6f;
            if (h < 0f) h += 1f;
        }
        float s = max == 0f ? 0f : delta / max;
        float v = max;

        s = Math.clamp(s + saturationDelta, 0.0f, 1.0f);
        v = Math.clamp(v + brightnessDelta, 0.15f, 1.0f);

        int i = (int) Math.floor(h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);

        float rr, gg, bb;
        switch (i % 6) {
            case 0 -> { rr = v; gg = t; bb = p; }
            case 1 -> { rr = q; gg = v; bb = p; }
            case 2 -> { rr = p; gg = v; bb = t; }
            case 3 -> { rr = p; gg = q; bb = v; }
            case 4 -> { rr = t; gg = p; bb = v; }
            default -> { rr = v; gg = p; bb = q; }
        }
        return ColorHelper.Argb.getArgb(255, (int) (rr * 255), (int) (gg * 255), (int) (bb * 255));
    }
}
