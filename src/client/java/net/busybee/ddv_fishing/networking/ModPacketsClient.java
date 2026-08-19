package net.busybee.ddv_fishing.networking;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.busybee.ddv_fishing.client.journal.FishJournalClientState;
import net.busybee.ddv_fishing.journal.FishJournalEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.List;

public class ModPacketsClient {
    public static void registerHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(FishingMinigameS2CPacket.ID, (payload, context) -> {
            int hits = payload.hits();
            float speed = payload.speed();
            int rarity = payload.rarity();
            context.client().execute(() -> {
                FishingMinigameOverlay.start(hits, speed, rarity);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FishJournalS2CPacket.ID, (payload, context) -> {
            List<FishJournalEntry> entries = payload.entries();
            context.client().execute(() -> FishJournalClientState.update(entries));
        });
    }
}
