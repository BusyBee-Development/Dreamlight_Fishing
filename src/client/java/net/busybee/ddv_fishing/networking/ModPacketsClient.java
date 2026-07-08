package net.busybee.ddv_fishing.networking;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ModPacketsClient {
    public static void registerHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(FishingMinigameS2CPacket.ID, (payload, context) -> {
            int hits = payload.hits();
            float speed = payload.speed();
            context.client().execute(() -> {
                FishingMinigameOverlay.start(hits, speed);
            });
        });
    }
}
