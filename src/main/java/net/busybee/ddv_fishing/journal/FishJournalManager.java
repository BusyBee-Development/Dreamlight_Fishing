package net.busybee.ddv_fishing.journal;

import net.busybee.ddv_fishing.Ddv_fishing;
import net.busybee.ddv_fishing.networking.FishJournalS2CPacket;
import net.busybee.ddv_fishing.registry.ModAttachments;
import net.busybee.ddv_fishing.registry.ModCriteria;
import net.busybee.ddv_fishing.registry.ModItems;
import net.busybee.ddv_fishing.world.FishBiome;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

/** Server-side journal bookkeeping: recording catches and keeping the client's copy in sync. */
public class FishJournalManager {

    /**
     * Bits in {@link ModAttachments#MILESTONE_REWARDS} - a reward is granted exactly once, the
     * first time {@link FishJournalData#completionFraction} crosses its threshold, regardless of
     * how many times the player later dips back below it (they can't, completion never regresses,
     * but the bitmask means a re-check on relog can't double-grant either).
     */
    private static final int MILESTONE_25 = 1 << 0;
    private static final int MILESTONE_50 = 1 << 1;
    private static final int MILESTONE_75 = 1 << 2;
    private static final int MILESTONE_100 = 1 << 3;

    /** Result of a single recorded catch, for the caller to build the catch notification from. */
    public record CatchResult(FishSpecies species, float size, boolean firstCatch, boolean newRecord, boolean firstCatchEver) {
    }

    public static void register() {
        // Covers relogs and server restarts - the client otherwise has nothing to show in the
        // journal screen until its next catch, even though the server already remembers everything.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            sync(player, player.getAttachedOrCreate(ModAttachments.FISH_JOURNAL));
        });
    }

    public static CatchResult recordCatch(ServerPlayerEntity player, FishSpecies species, boolean isPerfect) {
        FishJournalData data = player.getAttachedOrCreate(ModAttachments.FISH_JOURNAL);
        FishJournalEntry previous = data.get(species);

        boolean firstCatchEver = true;
        for (FishJournalEntry entry : data.entries()) {
            if (entry.caught()) {
                firstCatchEver = false;
                break;
            }
        }

        // One roll normally; a Perfect Catch takes the best of several, biasing the result toward
        // the top of the species' size range without a second RNG mechanism.
        float t = 0f;
        int rolls = isPerfect ? Ddv_fishing.CONFIG.perfect_catch_size_rolls : 1;
        for (int i = 0; i < rolls; i++) {
            t = Math.max(t, player.getRandom().nextFloat());
        }
        float size = species.minSize() + t * (species.maxSize() - species.minSize());

        boolean firstCatch = !previous.caught();
        boolean newRecord = size > previous.largestSize();
        long firstCaughtEpochMillis = firstCatch ? System.currentTimeMillis() : previous.firstCaughtEpochMillis();

        FishJournalEntry updated = new FishJournalEntry(
                species,
                true,
                previous.timesCaught() + 1,
                Math.max(previous.largestSize(), size),
                firstCaughtEpochMillis
        );

        FishJournalData updatedData = data.with(updated);
        player.setAttached(ModAttachments.FISH_JOURNAL, updatedData);
        sync(player, updatedData);
        checkMilestones(player, updatedData);
        checkCompletionAdvancements(player, updatedData);

        return new CatchResult(species, size, firstCatch, newRecord, firstCatchEver);
    }

    /**
     * Fires the biome-completion and full-completion advancements. Safe to call on every catch and
     * re-fire the same one repeatedly - {@code PlayerAdvancementTracker} already no-ops a criterion
     * that's already satisfied, so there's no need for our own "already fired" bookkeeping here the
     * way {@link #checkMilestones} needs one for its item/effect grants.
     */
    private static void checkCompletionAdvancements(ServerPlayerEntity player, FishJournalData data) {
        Map<FishBiome, int[]> counts = data.biomeCounts();
        for (FishBiome biome : new FishBiome[]{FishBiome.OCEAN, FishBiome.SWAMP, FishBiome.JUNGLE}) {
            int[] count = counts.get(biome);
            if (count[1] > 0 && count[0] == count[1]) {
                ModCriteria.BIOME_COMPLETE.trigger(player, biome);
            }
        }
        if (data.isComplete()) {
            ModCriteria.JOURNAL_COMPLETE.trigger(player);
        }
    }

    /**
     * Grants the 25/50/75/100% rewards the first time each threshold is crossed. Checked on every
     * catch rather than only when a new species first appears, because a bigger largest-size roll
     * never changes {@link FishJournalData#completionFraction} anyway - this only ever does
     * anything on the catch that actually pushes completion over a threshold.
     */
    private static void checkMilestones(ServerPlayerEntity player, FishJournalData data) {
        float fraction = data.completionFraction();
        int granted = player.getAttachedOrCreate(ModAttachments.MILESTONE_REWARDS);

        if (fraction >= 0.25f && (granted & MILESTONE_25) == 0) {
            granted |= MILESTONE_25;
            giveStack(player, new ItemStack(Items.EXPERIENCE_BOTTLE, 8));
            // A potion item would need its own PotionContentsComponent wiring for no real benefit
            // over just applying the effect outright - same pattern the Perfect Catch bonus in
            // FishingLootHandler already uses. Amplifier 1 is "Luck II".
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 30 * 20, 1));
            player.sendMessage(Text.translatable("message.ddv_fishing.milestone_25").formatted(Formatting.GREEN), false);
        }
        if (fraction >= 0.5f && (granted & MILESTONE_50) == 0) {
            granted |= MILESTONE_50;
            float bonus = player.getAttachedOrCreate(ModAttachments.RIPPLE_BONUS);
            player.setAttached(ModAttachments.RIPPLE_BONUS, bonus + 0.025f);
            player.sendMessage(Text.translatable("message.ddv_fishing.milestone_50").formatted(Formatting.GREEN), false);
        }
        if (fraction >= 0.75f && (granted & MILESTONE_75) == 0) {
            granted |= MILESTONE_75;
            giveStack(player, new ItemStack(ModItems.LUCKY_BAIT));
            player.sendMessage(Text.translatable("message.ddv_fishing.milestone_75").formatted(Formatting.GREEN), false);
        }
        if (fraction >= 1.0f && (granted & MILESTONE_100) == 0) {
            granted |= MILESTONE_100;
            giveStack(player, new ItemStack(ModItems.LEGENDARY_FISHING_ROD));
            player.sendMessage(Text.translatable("message.ddv_fishing.milestone_100").formatted(Formatting.GOLD), false);
        }

        player.setAttached(ModAttachments.MILESTONE_REWARDS, granted);
    }

    private static void giveStack(ServerPlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    private static void sync(ServerPlayerEntity player, FishJournalData data) {
        ServerPlayNetworking.send(player, new FishJournalS2CPacket(data.entries()));
    }
}
