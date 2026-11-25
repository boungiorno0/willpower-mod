package com.example.willpower;

import com.example.willpower.core.SpectrumSynergy;
import com.example.willpower.entity.ModEntities;
import com.example.willpower.network.LanternConfigPayload;
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
        ModCorpsRegistry.init();
        ModItemGroups.init();
        ModEntities.init();
        SpectrumSynergy.init();
        LanternConfigPayload.registerServer();
    }
}
