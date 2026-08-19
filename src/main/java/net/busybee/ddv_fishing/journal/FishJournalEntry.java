package net.busybee.ddv_fishing.journal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/** One species' journal row: whether it's been caught, how many times, the biggest one on record, and when it was first caught. */
public record FishJournalEntry(FishSpecies species, boolean caught, int timesCaught, float largestSize, long firstCaughtEpochMillis) {

    public static FishJournalEntry empty(FishSpecies species) {
        return new FishJournalEntry(species, false, 0, 0f, -1L);
    }

    public static final Codec<FishJournalEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FishSpecies.CODEC.fieldOf("species").forGetter(FishJournalEntry::species),
            Codec.BOOL.fieldOf("caught").forGetter(FishJournalEntry::caught),
            Codec.INT.fieldOf("times_caught").forGetter(FishJournalEntry::timesCaught),
            Codec.FLOAT.fieldOf("largest_size").forGetter(FishJournalEntry::largestSize),
            Codec.LONG.fieldOf("first_caught").forGetter(FishJournalEntry::firstCaughtEpochMillis)
    ).apply(instance, FishJournalEntry::new));

    public static final PacketCodec<RegistryByteBuf, FishJournalEntry> PACKET_CODEC = PacketCodec.tuple(
            FishSpecies.PACKET_CODEC.cast(), FishJournalEntry::species,
            PacketCodecs.BOOLEAN, FishJournalEntry::caught,
            PacketCodecs.VAR_INT, FishJournalEntry::timesCaught,
            PacketCodecs.FLOAT, FishJournalEntry::largestSize,
            PacketCodecs.VAR_LONG, FishJournalEntry::firstCaughtEpochMillis,
            FishJournalEntry::new
    );
}
