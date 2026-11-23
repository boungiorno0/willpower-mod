package com.example.willpower;

import com.example.willpower.item.WillRingItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
    /** Максимальный заряд кольца (в единицах «прочности»). */
    public static final int RING_MAX_CHARGE = 300;

    public static final Item WILL_RING = Registry.register(Registries.ITEM, WillpowerMod.id("will_ring"),
            new WillRingItem(new Item.Settings().maxDamage(RING_MAX_CHARGE).rarity(Rarity.EPIC)));

    public static void init() {
    }

    private ModItems() {
    }
}
