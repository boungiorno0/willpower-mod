package com.example.willpower.component;

import com.example.willpower.WillpowerMod;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<PlayerLanternComponent> LANTERN =
            ComponentRegistry.getOrCreate(WillpowerMod.id("lantern"), PlayerLanternComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(LANTERN, PlayerLanternComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
