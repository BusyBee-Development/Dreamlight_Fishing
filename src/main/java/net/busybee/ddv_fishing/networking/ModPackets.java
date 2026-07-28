package net.busybee.ddv_fishing.networking;

import net.busybee.ddv_fishing.FishingLootHandler;
import net.busybee.ddv_fishing.access.FishingBobberReelAccess;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


public class ModPackets {

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(FishingMinigameS2CPacket.ID, FishingMinigameS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(FishingMinigameResultC2SPacket.ID, FishingMinigameResultC2SPacket.CODEC);
    }

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(FishingMinigameResultC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            FishingBobberEntity bobber = player.fishHook;
            if (bobber == null || bobber.isRemoved() || !((FishingBobberReelAccess) bobber).ddv$consumeMinigameResult()) {
                return;
            }

            if (!payload.success()) {
                player.sendMessage(Text.literal("The fish got away..."), true);
                return;
            }

            ItemStack loot = getRandomFish(player);
            FishingLootHandler.catchFish(player, bobber, loot, payload.isPerfect());
            player.sendMessage(Text.literal("Caught a " + loot.getName().getString() + "!"), true);
        });
    }

    private static ItemStack getRandomFish(ServerPlayerEntity player) {
        float r = player.getRandom().nextFloat();
        if (r < 0.1f) return new ItemStack(Items.PUFFERFISH);
        if (r < 0.25f) return new ItemStack(Items.TROPICAL_FISH);
        if (r < 0.5f) return new ItemStack(Items.SALMON);
        return new ItemStack(Items.COD);
    }

}
