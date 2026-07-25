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
    public static final RegistryKey<EntityType<?>> FISHING_RIPPLE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, RIPPLE_ID);
    
    public static final EntityType<FishingRippleEntity> FISHING_RIPPLE = Registry.register(
            Registries.ENTITY_TYPE,
            //? if >1.21.1 {
            FISHING_RIPPLE_KEY,
            //?}
            //? if <=1.21.1 {
            /* RIPPLE_ID, */
            //?}
            EntityType.Builder.create(FishingRippleEntity::new, SpawnGroup.MISC)
                    .dimensions(2.0f, 0.5f)
                    //? if >1.21.1 {
                    .build(FISHING_RIPPLE_KEY)
                    //?}
                    //? if <=1.21.1 {
                    /*.build()*/
                    //?}
    );

    public static void register() {
    }
}
