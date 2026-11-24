package com.example.willpower.core;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public enum LanternCorps {
    GREEN(
            "green",
            "Green Lantern Corps",
            "Willpower",
            0x00FF44,
            Blocks.OXIDIZED_COPPER,
            "In brightest day, in blackest night, no evil shall escape my sight. Let those who worship evil's might, beware my power, Green Lantern's light!"
    ),
    YELLOW(
            "yellow",
            "Sinestro Corps",
            "Fear",
            0xFFD000,
            Blocks.GOLD_BLOCK,
            "In blackest day, in brightest night, beware your fears made into light. Let those who try to stop what's right, burn like his power, Sinestro's might!"
    ),
    BLUE(
            "blue",
            "Blue Lantern Corps",
            "Hope",
            0x1E90FF,
            Blocks.LAPIS_BLOCK,
            "In fearful day, in raging night, with strong hearts full, our souls ignite. When all seems lost in the War of Light, look to the stars, for hope burns bright!"
    ),
    RED(
            "red",
            "Red Lantern Corps",
            "Rage",
            0xFF1A1A,
            Blocks.REDSTONE_BLOCK,
            "With blood and rage of crimson red, ripped from a corpse so freshly dead, together with our hellish hate, we'll burn you all, that is your fate!"
    ),
    ORANGE(
            "orange",
            "Orange Lantern Corps",
            "Avarice",
            0xFF7700,
            Blocks.COPPER_BLOCK,
            "What's mine is mine and mine and mine, and mine and mine and mine! Not yours!"
    ),
    INDIGO(
            "indigo",
            "Indigo Tribe",
            "Compassion",
            0x7B00FF,
            Blocks.AMETHYST_BLOCK,
            "Tor lorek san, bor nakka mur, natromo faan tornek wot ur. Ter lantern ker lo Abin Sur, taan lek lek nok — formorrow sur!"
    ),
    STAR_SAPPHIRE(
            "violet",
            "Star Sapphire Corps",
            "Love",
            0xFF3EB5,
            Blocks.PURPUR_BLOCK,
            "For hearts long lost and full of fright, for those alone in blackest night, accept our ring and join our fight, love conquers all with violet light!"
    );

    private final String id;
    private final String displayName;
    private final String emotionName;
    private final int baseRgb;
    private final Block batteryBlock;
    private final String oath;

    LanternCorps(String id, String displayName, String emotionName, int baseRgb, Block batteryBlock, String oath) {
        this.id = id;
        this.displayName = displayName;
        this.emotionName = emotionName;
        this.baseRgb = baseRgb;
        this.batteryBlock = batteryBlock;
        this.oath = oath;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmotionName() {
        return emotionName;
    }

    public Text getFormattedName() {
        return Text.literal(displayName).styled(s -> s.withColor(baseRgb));
    }

    public int getBaseRgb() {
        return baseRgb;
    }

    public Block getBatteryBlock() {
        return batteryBlock;
    }

    public String getOath() {
        return oath;
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

    /** Применяет слайдеры Brightness (-0.5..0.5) и Saturation (-0.5..0.5) без AWT */
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
        v = Math.clamp(v + brightnessDelta, 0.20f, 1.0f);

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
