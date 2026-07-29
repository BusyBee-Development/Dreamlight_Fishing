package net.busybee.ddv_fishing;

import net.busybee.ddv_fishing.networking.ModPackets;
import net.busybee.ddv_fishing.registry.ModEntities;
import net.busybee.ddv_fishing.world.RippleSpawner;
import net.fabricmc.api.ModInitializer;

public class Ddv_fishing implements ModInitializer {
    public static ModConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = ModConfig.load();
        ModEntities.register();
        ModPackets.registerPayloads();
        ModPackets.registerServerHandlers();
        RippleSpawner.register();
    }
}
