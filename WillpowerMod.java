package com.example.willpower;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class WillpowerMod implements ModInitializer {
    public static final String MOD_ID = "willpower";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModItems.init();
        ModItemGroups.init();
    }
}
