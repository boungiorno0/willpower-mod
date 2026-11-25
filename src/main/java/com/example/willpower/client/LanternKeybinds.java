package com.example.willpower.client;

import com.example.willpower.client.gui.LanternScreen;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.network.LanternConfigPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class LanternKeybinds {
    private static final long DEFAULT_EYES = (1L << 33) | (1L << 34) | (1L << 37) | (1L << 38);

    private static KeyBinding openMenuKey;
    private static KeyBinding toggleSuitKey;
    private static KeyBinding cycleSpeedKey;

    private LanternKeybinds() {}

    public static void init() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.willpower.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.willpower"
        ));
        toggleSuitKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.willpower.toggle_suit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.willpower"
        ));
        cycleSpeedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.willpower.cycle_speed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.willpower"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            PlayerLanternComponent d = ModComponents.LANTERN.get(client.player);

            while (openMenuKey.wasPressed()) {
                client.setScreen(new LanternScreen());
            }

            while (toggleSuitKey.wasPressed()) {
                if (d.getActiveCorps() == null) {
                    client.player.sendMessage(Text.literal("Сначала активируй кольцо (ПКМ)!"), true);
                    continue;
                }
                boolean next = !d.isSuitActive();
                long eyes = next && d.getGlowingEyesMask() == 0 ? DEFAULT_EYES : d.getGlowingEyesMask();
                ClientPlayNetworking.send(new LanternConfigPayload(
                        next, d.isMaskActive(), next, next,
                        d.getSuitStyle(), d.getMaskHeight(), d.getBrightness(), d.getSaturation(),
                        d.getInsigniaDistance(), eyes, d.isEyesCorpsColor(), d.getCruiseSpeed(), d.getActivePose()
                ));
            }

            while (cycleSpeedKey.wasPressed()) {
                if (d.getActiveCorps() == null) continue;
                float s = d.getCruiseSpeed();
                float nextSpeed = s < 16f ? 16f : (s < 24f ? 24f : (s < 40f ? 40f : (s < 64f ? 64f : 8f)));
                ClientPlayNetworking.send(new LanternConfigPayload(
                        d.isSuitActive(), d.isMaskActive(), d.isAuraActive(), d.isInsigniaActive(),
                        d.getSuitStyle(), d.getMaskHeight(), d.getBrightness(), d.getSaturation(),
                        d.getInsigniaDistance(), d.getGlowingEyesMask(), d.isEyesCorpsColor(),
                        nextSpeed, d.getActivePose()
                ));
                client.player.sendMessage(Text.literal("Скорость полёта: " + Math.round(nextSpeed) + " b/s"), true);
            }
        });
    }
}
