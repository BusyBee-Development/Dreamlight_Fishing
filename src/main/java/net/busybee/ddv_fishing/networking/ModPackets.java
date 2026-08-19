package net.busybee.ddv_fishing.networking;

import net.busybee.ddv_fishing.FishingLootHandler;
import net.busybee.ddv_fishing.access.FishingBobberReelAccess;
import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.busybee.ddv_fishing.journal.FishJournalManager;
import net.busybee.ddv_fishing.journal.FishSpecies;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.Optional;


public class ModPackets {

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(FishingMinigameS2CPacket.ID, FishingMinigameS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(FishJournalS2CPacket.ID, FishJournalS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(FishingMinigameResultC2SPacket.ID, FishingMinigameResultC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(FishingMinigamePhaseC2SPacket.ID, FishingMinigamePhaseC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(FishingMinigameHitC2SPacket.ID, FishingMinigameHitC2SPacket.CODEC);
    }

    public static void registerServerHandlers() {
        // Each reported hit is checked against the ring speed the server sent and against the
        // server's own clock. The client is told nothing about the verdict: a rejected hit simply
        // isn't counted, and the shortfall surfaces when a SUCCESS arrives with the timing phase
        // still incomplete.
        ServerPlayNetworking.registerGlobalReceiver(FishingMinigameHitC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            FishingBobberEntity bobber = player.fishHook;
            if (bobber instanceof FishingBobberReelAccess reelAccess) {
                reelAccess.ddv$registerTimingHit(payload.ticksSinceReset());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(FishingMinigamePhaseC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            FishingBobberEntity bobber = player.fishHook;
            if (!(bobber instanceof FishingBobberReelAccess reelAccess)) return;

            // Entering the reeling phase starts the server's clock for it, and is refused outright
            // if the timing phase was never actually completed.
            if (payload.phase() == FishingRippleEntity.STATE_REELING && !reelAccess.ddv$beginReeling()) {
                return;
            }
            reelAccess.ddv$setRippleState(payload.phase());
        });

        ServerPlayNetworking.registerGlobalReceiver(FishingMinigameResultC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            FishingBobberEntity bobber = player.fishHook;
            if (bobber == null || bobber.isRemoved() || !(bobber instanceof FishingBobberReelAccess reelAccess)) {
                return;
            }

            int rarity = reelAccess.ddv$getRarity();
            // The server's own verdict, built from the hits it validated. The client used to send
            // this and was believed.
            boolean isPerfect = reelAccess.ddv$isPerfectCatch();
            // A success the server can't account for is downgraded rather than refused, so the
            // bobber and ripple are still cleaned up instead of being left stuck mid-minigame.
            MinigameResult result = payload.result();
            if (result == MinigameResult.SUCCESS
                    && !(reelAccess.ddv$isTimingComplete() && reelAccess.ddv$canCompleteReeling())) {
                result = MinigameResult.ESCAPE;
            }

            // Consumed last so the checks above read live state. Also the one-shot guard: a
            // duplicate result packet finds the minigame already claimed and gets nothing.
            if (!reelAccess.ddv$consumeMinigameResult()) {
                return;
            }

            if (result != MinigameResult.SUCCESS) {
                if (result == MinigameResult.SNAP) {
                    player.sendMessage(Text.literal("Your line snapped!"), true);
                    FishingLootHandler.damageFishingRod(player, 5 + player.getRandom().nextInt(5));
                } else if (result == MinigameResult.ESCAPE) {
                    player.sendMessage(Text.literal("The fish got away..."), true);
                    FishingLootHandler.damageFishingRod(player, 1);
                }
                // CANCEL is silent and free - the player didn't fail, the minigame was called off
                // because they died, disconnected, or reeled the line back in.
                bobber.discard();
                player.fishHook = null;
                return;
            }

            List<ItemStack> loot = FishingLootHandler.generateLoot(player, bobber, rarity);
            FishingLootHandler.catchFish(player, bobber, loot, isPerfect);
            player.fishHook = null;

            MutableText message = Text.literal(isPerfect ? "Perfect Catch! Caught " : "Caught ");
            for (int i = 0; i < loot.size(); i++) {
                ItemStack stack = loot.get(i);
                int count = stack.getCount();
                if (isPerfect && !FishingLootHandler.isSpecialItem(stack)) {
                    count *= 2;
                }

                if (count > 1) {
                    message.append(Text.literal(count + "x "));
                } else {
                    message.append(Text.literal("a "));
                }
                message.append(stack.getName());

                Optional<FishSpecies> species = FishSpecies.fromItem(stack.getItem());
                if (species.isPresent()) {
                    FishJournalManager.CatchResult catchResult =
                            FishJournalManager.recordCatch(player, species.get(), isPerfect);
                    message.append(Text.literal(" (" + Math.round(catchResult.size()) + "cm)"));
                    if (catchResult.firstCatch()) {
                        message.append(Text.translatable("message.ddv_fishing.new_species").formatted(Formatting.GOLD));
                    } else if (catchResult.newRecord()) {
                        message.append(Text.translatable("message.ddv_fishing.new_record").formatted(Formatting.AQUA));
                    }
                }

                if (i < loot.size() - 1) {
                    message.append(Text.literal(", "));
                }
            }
            message.append(Text.literal("!"));
            player.sendMessage(message, true);
        });
    }
}
