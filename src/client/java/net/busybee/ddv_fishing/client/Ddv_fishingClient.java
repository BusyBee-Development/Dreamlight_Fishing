package net.busybee.ddv_fishing.client;

import net.busybee.ddv_fishing.client.entity.FishingRippleRenderer;
import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.busybee.ddv_fishing.client.item.MagicalFishingRodRenderer;
import net.busybee.ddv_fishing.client.journal.FishJournalClientState;
import net.busybee.ddv_fishing.client.journal.FishJournalScreen;
import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import net.busybee.ddv_fishing.networking.ModPacketsClient;
import net.busybee.ddv_fishing.registry.ModEntities;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}

public class Ddv_fishingClient implements ClientModInitializer {

    // J is free in vanilla and not claimed by any bundled/likely mod on this modpack, so it's safe
    // as a default rather than leaving the journal unbound.
    private static final KeyBinding OPEN_JOURNAL_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.ddv_fishing.open_journal", GLFW.GLFW_KEY_J, KeyBinding.Category.MISC));

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

            while (OPEN_JOURNAL_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FishJournalScreen());
                }
            }
        });

        // Both bits of static client state would otherwise carry a stale HUD/journal from one
        // world into the next - the overlay's click interception and the journal's last-synced data.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FishingMinigameOverlay.reset();
            FishJournalClientState.reset();
        });

        ModPacketsClient.registerHandlers();
    }
}
