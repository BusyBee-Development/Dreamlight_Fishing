package net.busybee.ddv_fishing.client;

import net.busybee.ddv_fishing.client.entity.FishingRippleRenderer;
import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.busybee.ddv_fishing.client.item.MagicalFishingRodRenderer;
import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import net.busybee.ddv_fishing.networking.ModPacketsClient;
import net.busybee.ddv_fishing.registry.ModEntities;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}

public class Ddv_fishingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //? if >=1.21.6 {
        HudElementRegistry.addLast(Identifier.of("ddv_fishing", "minigame"), FishingMinigameOverlay::render);
        //?} else {
        /*HudRenderCallback.EVENT.register(FishingMinigameOverlay::render);
        *///?}

        // Rings on the water, tinted by rarity, on top of the entity's own particles.
        EntityRendererRegistry.register(ModEntities.FISHING_RIPPLE, FishingRippleRenderer::new);

        // Hand the rod its client-side renderer. The item itself lives in the main source set
        // and can't name client types, so it holds this as an Object and we fill it in here.
        MagicalFishingRodItem.clientRenderProvider = new GeoRenderProvider() {
            private @Nullable MagicalFishingRodRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new MagicalFishingRodRenderer();
                }

                return this.renderer;
            }
        };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !client.isPaused()) {
                FishingMinigameOverlay.tick();
            }
        });

        ModPacketsClient.registerHandlers();
    }
}
