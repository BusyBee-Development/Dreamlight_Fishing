package net.busybee.ddv_fishing.registry;

import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final Identifier RIPPLE_ID = Identifier.of("ddv_fishing", "fishing_ripple");
    
    public static final EntityType<FishingRippleEntity> FISHING_RIPPLE = Registry.register(
            Registries.ENTITY_TYPE,
            RIPPLE_ID,
            EntityType.Builder.create(FishingRippleEntity::new, SpawnGroup.MISC)
                    .dimensions(2.0f, 0.5f)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, RIPPLE_ID))
    );

    public static void register() {
    }
}
