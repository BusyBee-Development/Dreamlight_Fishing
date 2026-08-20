package net.busybee.ddv_fishing.journal;

import com.mojang.serialization.Codec;
import net.busybee.ddv_fishing.world.FishBiome;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A player's full journal: exactly one entry per {@link FishSpecies}, always present and always
 * ordinal-ordered, so callers never need to null-check a missing species.
 */
public record FishJournalData(List<FishJournalEntry> entries) {

    public static FishJournalData empty() {
        return of(List.of());
    }

    /**
     * Builds a complete, ordinal-ordered entry list from a possibly-partial or unordered one -
     * missing species are filled in as {@link FishJournalEntry#empty}, entries for a species that
     * no longer exists (e.g. read back after a future update removed one) are dropped. This is what
     * lets the persisted/network form tolerate the species list changing shape later without a
     * migration step.
     */
    public static FishJournalData of(List<FishJournalEntry> raw) {
        EnumMap<FishSpecies, FishJournalEntry> bySpecies = new EnumMap<>(FishSpecies.class);
        for (FishJournalEntry entry : raw) {
            if (entry != null && entry.species() != null) {
                bySpecies.put(entry.species(), entry);
            }
        }

        List<FishJournalEntry> normalized = new ArrayList<>(FishSpecies.values().length);
        for (FishSpecies species : FishSpecies.values()) {
            normalized.add(bySpecies.getOrDefault(species, FishJournalEntry.empty(species)));
        }
        return new FishJournalData(List.copyOf(normalized));
    }

    public static final Codec<FishJournalData> CODEC =
            FishJournalEntry.CODEC.listOf().xmap(FishJournalData::of, FishJournalData::entries);

    public FishJournalEntry get(FishSpecies species) {
        return entries.get(species.ordinal());
    }

    public FishJournalData with(FishJournalEntry updated) {
        List<FishJournalEntry> copy = new ArrayList<>(entries);
        copy.set(updated.species().ordinal(), updated);
        return new FishJournalData(List.copyOf(copy));
    }

    /**
     * Completion, 0.0-1.0, across the species that were catchable from the start - i.e. everything
     * except {@link FishBiome#LEGENDARY}. This is what the 25/50/75/100% milestone rewards and the
     * Legendary-tier unlock are measured against; it deliberately excludes the Legendary fish
     * itself, since that fish can't be caught until this same fraction hits 1.0 and unlocks the
     * ripple tier it comes from - counting it would make 100% unreachable.
     */
    public float completionFraction() {
        int caught = 0;
        int total = 0;
        for (FishJournalEntry entry : entries) {
            if (entry.species().biome() == FishBiome.LEGENDARY) continue;
            total++;
            if (entry.caught()) caught++;
        }
        return total == 0 ? 0f : (float) caught / total;
    }

    /** Every species catchable from the start has been caught - see {@link #completionFraction}. */
    public boolean isComplete() {
        return completionFraction() >= 1.0f;
    }

    /** Per-biome {@code {caught, total}} counts, for the journal's "Ocean: 2/2"-style headers. */
    public Map<FishBiome, int[]> biomeCounts() {
        Map<FishBiome, int[]> counts = new EnumMap<>(FishBiome.class);
        for (FishBiome biome : FishBiome.values()) {
            counts.put(biome, new int[2]);
        }
        for (FishJournalEntry entry : entries) {
            int[] count = counts.get(entry.species().biome());
            count[1]++;
            if (entry.caught()) {
                count[0]++;
            }
        }
        return counts;
    }
}
