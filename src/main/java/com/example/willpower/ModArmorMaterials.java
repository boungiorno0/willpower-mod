package com.example.willpower;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Map;

public final class ModArmorMaterials {
    /** Костюм Фонаря. Текстуры: textures/models/armor/lantern_layer_1.png и lantern_layer_2.png */
    public static final RegistryEntry<ArmorMaterial> LANTERN = Registry.registerReference(
            Registries.ARMOR_MATERIAL,
            WillpowerMod.id("lantern"),
            new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.BOOTS, 3,
                            ArmorItem.Type.LEGGINGS, 6,
                            ArmorItem.Type.CHESTPLATE, 8,
                            ArmorItem.Type.HELMET, 3,
                            ArmorItem.Type.BODY, 11
                    ),
                    20,
                    SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.ofItems(Items.EMERALD),
                    List.of(new ArmorMaterial.Layer(WillpowerMod.id("lantern"))),
                    2.0f,
                    0.0f
            ));

    /** Надет ли полный костюм Фонаря. */
    public static boolean hasFullSet(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.LANTERN_HELMET)
                && player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.LANTERN_CHESTPLATE)
                && player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.LANTERN_LEGGINGS)
                && player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.LANTERN_BOOTS);
    }

    private ModArmorMaterials() {
    }
}
