package net.busybee.ddv_fishing.journal;

import net.busybee.ddv_fishing.Ddv_fishing;
import net.busybee.ddv_fishing.networking.FishJournalS2CPacket;
import net.busybee.ddv_fishing.registry.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/** Server-side journal bookkeeping: recording catches and keeping the client's copy in sync. */
public class FishJournalManager {

    /** Result of a single recorded catch, for the caller to build the catch notification from. */
    public record CatchResult(FishSpecies species, float size, boolean firstCatch, boolean newRecord) {
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

        return new CatchResult(species, size, firstCatch, newRecord);
    }

    private static void sync(ServerPlayerEntity player, FishJournalData data) {
        ServerPlayNetworking.send(player, new FishJournalS2CPacket(data.entries()));
    }
}
