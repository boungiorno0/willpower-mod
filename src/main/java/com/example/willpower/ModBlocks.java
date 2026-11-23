package com.example.willpower;

import com.example.willpower.block.WillConstructBlock;
import com.example.willpower.block.WillReservoirBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    /** Временный светящийся блок, создаваемый кольцом. В инвентаре не появляется. */
    public static final Block WILL_CONSTRUCT = register("will_construct",
            new WillConstructBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIME)
                    .strength(0.3f)
                    .nonOpaque()
                    .luminance(state -> 9)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .dropsNothing()
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            false);

    /** Хранилище воли: ПКМ кольцом по блоку — полная подзарядка. */
    public static final Block WILL_RESERVOIR = register("will_reservoir",
            new WillReservoirBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(3.0f, 6.0f)
                    .requiresTool()
                    .luminance(state -> 15)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)),
            true);

    private static Block register(String name, Block block, boolean withItem) {
        Identifier id = WillpowerMod.id(name);
        Registry.register(Registries.BLOCK, id, block);
        if (withItem) {
            Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        }
        return block;
    }

    public static void init() {
        // Вызов нужен, чтобы статические поля инициализировались в нужный момент.
    }

    private ModBlocks() {
    }
}
