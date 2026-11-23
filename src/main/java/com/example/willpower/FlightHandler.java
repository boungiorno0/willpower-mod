package com.example.willpower;

import com.example.willpower.item.WillRingItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Полёт с кольцом и эффекты костюма Фонаря. */
public final class FlightHandler {
    private static final Set<UUID> FLYERS = new HashSet<>();

    /** Как часто списывается заряд (в тиках) и сколько. */
    private static final int DRAIN_INTERVAL = 20;
    private static final int DRAIN_COST = 2;
    private static final int DRAIN_COST_SUIT = 1;
    /** Минимум заряда, чтобы включить полёт. */
    private static final int START_MIN_CHARGE = 10;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(FlightHandler::tick);
        // Если игрок вышел во время полёта, при входе возможность летать сбрасывается.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> resetAbilities(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FLYERS.remove(handler.getPlayer().getUuid()));
    }

    /** Включить или выключить полёт (вызывается кольцом в режиме «Полёт»). */
    public static void toggle(ServerPlayerEntity player, ItemStack ring) {
        if (FLYERS.contains(player.getUuid())) {
            stop(player, "message.willpower.flight_off");
            return;
        }
        if (ring.getDamage() + START_MIN_CHARGE >= ring.getMaxDamage()) {
            player.sendMessage(Text.translatable("message.willpower.no_charge"), true);
            return;
        }
        FLYERS.add(player.getUuid());
        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.sendAbilitiesUpdate();
        player.sendMessage(Text.translatable("message.willpower.flight_on"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS, 1.0f, 1.6f);
    }

    private static void tick(MinecraftServer server) {
        int ticks = server.getTicks();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            boolean suit = ModArmorMaterials.hasFullSet(player);

            // Зелёное свечение вокруг игрока в полном костюме
            if (suit && ticks % 10 == 0) {
                ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getBodyY(0.5), player.getZ(),
                        2, 0.35, 0.5, 0.35, 0.0);
            }

            if (!FLYERS.contains(player.getUuid())) {
                continue;
            }

            // В творческом режиме и в наблюдателе кольцо полётом не управляет
            if (player.isCreative() || player.isSpectator()) {
                FLYERS.remove(player.getUuid());
                continue;
            }

            ItemStack ring = findRing(player);
            if (ring.isEmpty() || ring.getDamage() >= ring.getMaxDamage() - 1) {
                stop(player, "message.willpower.flight_empty");
                continue;
            }

            if (player.getAbilities().flying && ticks % DRAIN_INTERVAL == 0) {
                int cost = suit ? DRAIN_COST_SUIT : DRAIN_COST;
                ring.setDamage(Math.min(ring.getMaxDamage() - 1, ring.getDamage() + cost));
            }
        }
    }

    private static ItemStack findRing(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof WillRingItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void stop(ServerPlayerEntity player, String messageKey) {
        FLYERS.remove(player.getUuid());
        resetAbilities(player);
        player.sendMessage(Text.translatable(messageKey), true);
    }

    private static void resetAbilities(ServerPlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();
    }

    private FlightHandler() {
    }
}
