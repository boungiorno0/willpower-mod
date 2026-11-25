package com.example.willpower;

import com.example.willpower.core.LanternCorps;
import com.example.willpower.item.CorpsRingItem;
import com.example.willpower.item.PowerLanternItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.EnumMap;
import java.util.Map;

public final class ModCorpsRegistry {
    public static final Map<LanternCorps, Item> RINGS = new EnumMap<>(LanternCorps.class);
    public static final Map<LanternCorps, Item> LANTERNS = new EnumMap<>(LanternCorps.class);

    public static final ItemGroup CORPS_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            WillpowerMod.id("corps_tab"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(RINGS.get(LanternCorps.GREEN)))
                    .displayName(Text.literal("Lantern Corps"))
                    .entries((ctx, entries) -> {
                        for (LanternCorps corps : LanternCorps.values()) {
                            entries.add(RINGS.get(corps));
                            entries.add(LANTERNS.get(corps));
                        }
                        entries.add(ModBlocks.WILL_RESERVOIR);
                    })
                    .build()
    );

    public static void init() {
        for (LanternCorps corps : LanternCorps.values()) {
            Item ring = Registry.register(
                    Registries.ITEM,
                    WillpowerMod.id(corps.getId() + "_ring"),
                    new CorpsRingItem(corps, new Item.Settings().maxCount(1).rarity(Rarity.EPIC))
            );
            Item lantern = Registry.register(
                    Registries.ITEM,
                    WillpowerMod.id(corps.getId() + "_lantern"),
                    new PowerLanternItem(corps, new Item.Settings().maxCount(1).rarity(Rarity.RARE))
            );
            RINGS.put(corps, ring);
            LANTERNS.put(corps, lantern);
        }
    }

    private ModCorpsRegistry() {}
}
