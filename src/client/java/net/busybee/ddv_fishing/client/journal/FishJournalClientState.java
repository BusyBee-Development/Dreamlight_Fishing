package net.busybee.ddv_fishing.client.journal;

import net.busybee.ddv_fishing.journal.FishJournalData;
import net.busybee.ddv_fishing.journal.FishJournalEntry;

import java.util.List;

/**
 * The client's last-synced copy of the player's journal. Static, mirroring
 * {@link net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay}'s state-holding pattern -
 * {@link FishJournalScreen} just reads this when it opens rather than round-tripping a request.
 */
public class FishJournalClientState {
    private static FishJournalData data = FishJournalData.empty();

    public static void update(List<FishJournalEntry> entries) {
        data = FishJournalData.of(entries);
    }

    public static FishJournalData data() {
        return data;
    }

    /** Called on disconnect so a stale journal from one server/world doesn't show up in the next. */
    public static void reset() {
        data = FishJournalData.empty();
    }
}
