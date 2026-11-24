package com.example.willpower.entity;

import com.example.willpower.WillpowerMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {
    public static final EntityType<ConstructEntity> CONSTRUCT = Registry.register(
            Registries.ENTITY_TYPE,
            WillpowerMod.id("construct"),
            EntityType.Builder.<ConstructEntity>create(ConstructEntity::new, SpawnGroup.MISC)
                    .dimensions(0.8f, 0.8f)
                    .maxTrackingRange(8)
                    .trackingTickInterval(1)
                    .build()
    );

    public static void init() {}

    private ModEntities() {}
}
