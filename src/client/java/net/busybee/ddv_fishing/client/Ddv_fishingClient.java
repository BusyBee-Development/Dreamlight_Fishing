package net.busybee.ddv_fishing.client;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.busybee.ddv_fishing.networking.ModPacketsClient;
import net.busybee.ddv_fishing.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.render.entity.EmptyEntityRenderer;

public class Ddv_fishingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(FishingMinigameOverlay::render);

        EntityRendererRegistry.register(ModEntities.FISHING_RIPPLE, EmptyEntityRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !client.isPaused()) {
                FishingMinigameOverlay.tick();
            }
        });

        ModPacketsClient.registerHandlers();
    }
}
