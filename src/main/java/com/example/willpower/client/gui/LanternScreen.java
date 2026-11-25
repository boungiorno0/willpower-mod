package com.example.willpower.client.gui;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.network.LanternConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class LanternScreen extends Screen {
    private static final long DEFAULT_EYES = (1L << 33) | (1L << 34) | (1L << 37) | (1L << 38);

    private boolean suit, mask, aura, insignia, eyesCorps;
    private int style, maskY, pose;
    private float bright, sat, insDist, speed;
    private long eyesMask;

    public LanternScreen() {
        super(Text.literal("Lantern Customization"));
        PlayerLanternComponent d = ModComponents.LANTERN.get(MinecraftClient.getInstance().player);
        this.suit = d.isSuitActive();
        this.mask = d.isMaskActive();
        this.aura = d.isAuraActive();
        this.insignia = d.isInsigniaActive();
        this.style = d.getSuitStyle();
        this.maskY = d.getMaskHeight();
        this.bright = d.getBrightness();
        this.sat = d.getSaturation();
        this.insDist = d.getInsigniaDistance();
        this.eyesMask = d.getGlowingEyesMask();
        this.eyesCorps = d.isEyesCorpsColor();
        this.speed = d.getCruiseSpeed();
        this.pose = d.getActivePose();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 44;

        addBtn(cx - 155, y, 150, "Костюм: " + onOff(suit), () -> suit = !suit);
        addBtn(cx + 5, y, 150, "Стиль: " + (style == 0 ? "Comic" : "Armored"), () -> style = (style + 1) % 2);
        y += 24;

        addBtn(cx - 155, y, 100, "Маска: " + onOff(mask), () -> mask = !mask);
        addBtn(cx - 50, y, 25, "-", () -> maskY = Math.max(-4, maskY - 1));
        addBtn(cx - 22, y, 25, "+", () -> maskY = Math.min(4, maskY + 1));
        addBtn(cx + 5, y, 150, "Аура: " + onOff(aura), () -> aura = !aura);
        y += 24;

        addBtn(cx - 155, y, 100, "Эмблема: " + onOff(insignia), () -> insignia = !insignia);
        addBtn(cx - 50, y, 25, "-", () -> insDist = Math.max(0.05f, insDist - 0.05f));
        addBtn(cx - 22, y, 25, "+", () -> insDist = Math.min(0.55f, insDist + 0.05f));
        String eyeText = eyesMask == 0 ? "Глаза: Выкл" : (eyesCorps ? "Глаза: Корпус" : "Глаза: Белые");
        addBtn(cx + 5, y, 150, eyeText, () -> {
            if (eyesMask == 0) { eyesMask = DEFAULT_EYES; eyesCorps = true; }
            else if (eyesCorps) { eyesCorps = false; }
            else { eyesMask = 0L; }
        });
        y += 24;

        addBtn(cx - 155, y, 72, "Ярк -", () -> bright = Math.max(-0.4f, bright - 0.1f));
        addBtn(cx - 78, y, 73, "Ярк +", () -> bright = Math.min(0.4f, bright + 0.1f));
        addBtn(cx + 5, y, 72, "Насыщ -", () -> sat = Math.max(-0.5f, sat - 0.1f));
        addBtn(cx + 83, y, 72, "Насыщ +", () -> sat = Math.min(0.5f, sat + 0.1f));
        y += 24;

        addBtn(cx - 155, y, 150, "Полёт: " + Math.round(speed) + " b/s", () -> {
            speed = (speed >= 64f) ? 8f : speed + 8f;
        });
        addBtn(cx + 5, y, 150, "Готово", this::close);
    }

    private void addBtn(int x, int y, int w, String label, Runnable action) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            action.run();
            sendUpdate();
            this.clearAndInit();
        }).dimensions(x, y, w, 20).build());
    }

    private void sendUpdate() {
        ClientPlayNetworking.send(new LanternConfigPayload(
                suit, mask, aura, insignia, style, maskY, bright, sat, insDist, eyesMask, eyesCorps, speed, pose
        ));
    }

    private static String onOff(boolean v) {
        return v ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        PlayerLanternComponent d = ModComponents.LANTERN.get(MinecraftClient.getInstance().player);
        int color = d.getActiveCorps() != null ? (0xFF000000 | d.getActiveCorps().getBaseRgb()) : 0xFFFFFFFF;
        String header = d.getActiveCorps() != null
                ? d.getActiveCorps().getDisplayName() + " | Высота маски: " + maskY + " | Дист: " + String.format("%.2f", insDist)
                : "Активируй любое кольцо Фонаря (ПКМ)!";
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(header), this.width / 2, 18, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
