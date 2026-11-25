package com.example.willpower.client.render;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
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
        int border = 0xCC000000 | (argb & 0x00FFFFFF);
        int fill = 0xAA000000 | (argb & 0x00FFFFFF);

        int x = 10;
        int y = ctx.getScaledWindowHeight() - 48;
        int width = 148;
        int height = 38;

        // Полупрозрачная рамка и фон
        ctx.fill(x, y, x + width, y + height, 0x88080A0E);
        ctx.drawBorder(x, y, width, height, border);

        TextRenderer font = mc.textRenderer;
        String title = corps.getEmotionName() + " [R" + data.getRank() + "]";
        ctx.drawText(font, Text.literal(title), x + 6, y + 5, border, true);

        int max = Math.max(1, data.getMaxCharge());
        float pct = Math.clamp(data.getCharge() / (float) max, 0.0f, 1.0f);
        int percentNum = Math.round(pct * 100);

        // Полоска заряда
        int barX = x + 6;
        int barY = y + 17;
        int barW = width - 12;
        ctx.fill(barX, barY, barX + barW, barY + 6, 0xAA1A1D24);
        ctx.fill(barX, barY, barX + Math.round(barW * pct), barY + 6, fill);

        String info = percentNum + "%  |  " + Math.round(data.getCruiseSpeed()) + " b/s";
        if (data.isDesynced() && (mc.player.age % 20 < 10)) {
            info = "DESYNC! Charge Ring!";
        }
        int infoColor = data.isDesynced() ? 0xFFFF5555 : 0xFFE6E6E6;
        ctx.drawText(font, Text.literal(info), x + 6, y + 26, infoColor, true);
    }
}
