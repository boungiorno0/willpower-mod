package com.example.willpower.client.render;

import com.example.willpower.client.LanternKeybinds;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public final class LanternHudRenderer implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        PlayerLanternComponent data = ModComponents.LANTERN.get(mc.player);
        LanternCorps corps = data.getActiveCorps();
        if (corps == null) return;

        int argb = data.getDisplayArgb();
        int rgb = argb & 0x00FFFFFF;
        int accent = 0xFF000000 | rgb;
        TextRenderer font = mc.textRenderer;

        int panelW = 144;
        int x = ctx.getScaledWindowWidth() - panelW - 8;
        int y = Math.max(12, (ctx.getScaledWindowHeight() - 150) / 2);

        // 1. Верхняя карточка: Эмблема, Корпус, Ранг, Заряд %
        ctx.fill(x, y, x + panelW, y + 36, 0xAA0B0E14);
        ctx.drawBorder(x, y, panelW, 36, 0xCC000000 | rgb);

        float r = ((rgb >> 16) & 0xFF) / 255f, g = ((rgb >> 8) & 0xFF) / 255f, b = (rgb & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, g, b, 1.0f);
        ctx.drawTexture(CorpsEmblemTextures.get(corps), x + 5, y + 5, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        ctx.drawText(font, Text.literal(corps.getEmotionName() + " [R" + data.getRank() + "]"), x + 25, y + 5, accent, true);
        int pctNum = Math.round(Math.clamp(data.getCharge() / (float) Math.max(1, data.getMaxCharge()), 0f, 1f) * 100);
        String stat = data.isDesynced() && (mc.player.age % 20 < 10) ? "DESYNC!" : (pctNum + "% | " + Math.round(data.getCruiseSpeed()) + "b/s");
        ctx.drawText(font, Text.literal(stat), x + 25, y + 15, data.isDesynced() ? 0xFFFF5555 : 0xFFD8DCE6, true);

        ctx.fill(x + 5, y + 27, x + panelW - 5, y + 32, 0xCC1B1F2A);
        ctx.fill(x + 5, y + 27, x + 5 + Math.round((panelW - 10) * (pctNum / 100f)), y + 32, accent);

        // 2. Вертикальные слоты способностей справа (Palladium-стиль)
        float cd = LanternKeybinds.getSignatureCooldownLeftSec();
        String vLabel = cd > 0.05f ? String.format("%.1fs", cd) : "ГОТОВО";
        drawSlot(ctx, font, x, y + 40, panelW, "V", signatureName(corps), vLabel, cd <= 0.05f ? accent : 0xFFFF6666, cd > 0 ? (1f - cd / 3f) : 1f, accent);
        drawSlot(ctx, font, x, y + 60, panelW, "G", "Тракторный луч", "ЛУЧ", accent, 1f, accent);
        drawSlot(ctx, font, x, y + 80, panelW, "R", "Костюм и Полёт", data.isSuitActive() ? "ВКЛ" : "ВЫКЛ", data.isSuitActive() ? 0xFF55FF88 : 0xFFAAAAAA, 1f, accent);
        drawSlot(ctx, font, x, y + 100, panelW, "H", "Скорость полёта", Math.round(data.getCruiseSpeed()) + "", 0xFFE6E6E6, 1f, accent);
        drawSlot(ctx, font, x, y + 120, panelW, "K", "Меню стиля", "GUI", 0xFF99CCFF, 1f, accent);
    }

    private static void drawSlot(DrawContext ctx, TextRenderer font, int x, int y, int w,
                                 String key, String name, String status, int statusCol, float fillPct, int accent) {
        ctx.fill(x, y, x + w, y + 18, 0x990B0E14);
        ctx.fill(x, y, x + 2, y + 18, accent);
        if (fillPct < 1.0f) {
            ctx.fill(x + 2, y + 16, x + 2 + Math.round((w - 2) * fillPct), y + 18, accent);
        }
        ctx.fill(x + 5, y + 3, x + 19, y + 15, 0xCC1E2430);
        ctx.drawCenteredTextWithShadow(font, Text.literal(key), x + 12, y + 5, 0xFFFFFFFF);
        ctx.drawText(font, Text.literal(name), x + 23, y + 5, 0xFFE8ECF2, true);
        int sw = font.getWidth(status);
        ctx.drawText(font, Text.literal(status), x + w - sw - 5, y + 5, statusCol, true);
    }

    private static String signatureName(LanternCorps corps) {
        return switch (corps) {
            case GREEN -> "Щит и Клетка";
            case YELLOW -> "Ужас и Молния";
            case BLUE -> "Купол Надежды";
            case RED -> "Плазма Крови";
            case ORANGE -> "Грабеж и Магнит";
            case INDIGO -> "Телепорт/Обмен";
            case STAR_SAPPHIRE -> "Кристалл-Плен";
            case WHITE -> "Светлейший День";
            case BLACK -> "Некро-Призыв";
        };
    }
}
