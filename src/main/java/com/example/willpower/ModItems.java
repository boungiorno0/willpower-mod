package com.example.willpower;

import com.example.willpower.item.WillRingItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
    /** Максимальный заряд кольца (в единицах «прочности»). */
    public static final int RING_MAX_CHARGE = 300;

    public static final Item WILL_RING = Registry.register(Registries.ITEM, WillpowerMod.id("will_ring"),
            new WillRingItem(new Item.Settings().maxDamage(RING_MAX_CHARGE).rarity(Rarity.EPIC)));

    // Костюм Фонаря
    public static final Item LANTERN_HELMET = Registry.register(Registries.ITEM, WillpowerMod.id("lantern_helmet"),
            new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.HELMET,
                    new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(26)).rarity(Rarity.RARE)));
    public static final Item LANTERN_CHESTPLATE = Registry.register(Registries.ITEM, WillpowerMod.id("lantern_chestplate"),
            new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(26)).rarity(Rarity.RARE)));
    public static final Item LANTERN_LEGGINGS = Registry.register(Registries.ITEM, WillpowerMod.id("lantern_leggings"),
            new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.LEGGINGS,
                    new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(26)).rarity(Rarity.RARE)));
    public static final Item LANTERN_BOOTS = Registry.register(Registries.ITEM, WillpowerMod.id("lantern_boots"),
            new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.BOOTS,
                    new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(26)).rarity(Rarity.RARE)));

    public static void init() {
    }

    private ModItems() {
    }
}
