package com.example.willpower;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    public static final ItemGroup MAIN = Registry.register(Registries.ITEM_GROUP, WillpowerMod.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.WILL_RING))
                    .displayName(Text.translatable("itemGroup.willpower.main"))
                    .entries((context, entries) -> {
                        entries.add(ModItems.WILL_RING);
                        entries.add(ModBlocks.WILL_RESERVOIR);
                        entries.add(ModItems.LANTERN_HELMET);
                        entries.add(ModItems.LANTERN_CHESTPLATE);
                        entries.add(ModItems.LANTERN_LEGGINGS);
                        entries.add(ModItems.LANTERN_BOOTS);
                    })
                    .build());

    public static void init() {
    }

    private ModItemGroups() {
    }
}
