package net.busybee.ddv_fishing;

import net.busybee.ddv_fishing.journal.FishJournalManager;
import net.busybee.ddv_fishing.networking.ModPackets;
import net.busybee.ddv_fishing.registry.ModAttachments;
import net.busybee.ddv_fishing.registry.ModCriteria;
import net.busybee.ddv_fishing.registry.ModEntities;
import net.busybee.ddv_fishing.registry.ModItems;
import net.busybee.ddv_fishing.world.RippleSpawner;
import dev.faststats.Metrics;
import dev.faststats.fabric.FabricContext;
import net.fabricmc.api.ModInitializer;

public class Ddv_fishing implements ModInitializer {
    public static ModConfig CONFIG;
    private FabricContext context;

    @Override
    public void onInitialize() {
        context = new FabricContext.Factory("ddv_fishing", "242afaa579719f8ee5df6db500fb6a6e")
                .metrics(Metrics.Factory::create)
                .create();

        CONFIG = ModConfig.load();
        ModEntities.register();
        ModItems.register();
        ModAttachments.register();
        ModCriteria.register();
        ModPackets.registerPayloads();
        ModPackets.registerServerHandlers();
        RippleSpawner.register();
        FishJournalManager.register();
    }
}
