package net.busybee.ddv_fishing;

import net.busybee.ddv_fishing.networking.ModPackets;
import net.busybee.ddv_fishing.registry.ModEntities;
import net.busybee.ddv_fishing.world.RippleSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class Ddv_fishing implements ModInitializer {

    @Override
    public void onInitialize() {
        ModEntities.register();
        ModPackets.registerPayloads();
        ModPackets.registerServerHandlers();
        RippleSpawner.register();

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            FishingLootHandler.tick();
        });
    }
}
