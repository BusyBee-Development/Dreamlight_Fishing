package net.busybee.ddv_fishing.networking;

import net.busybee.ddv_fishing.journal.FishJournalEntry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Full journal snapshot, sent both on player join and after every catch of a tracked species.
 * Always carries all 10 species - cheap at this size, and simpler than a delta packet.
 */
public record FishJournalS2CPacket(List<FishJournalEntry> entries) implements CustomPayload {
    public static final Id<FishJournalS2CPacket> ID = new Id<>(Identifier.of("ddv_fishing", "sync_journal"));

    public static final PacketCodec<RegistryByteBuf, FishJournalS2CPacket> CODEC = PacketCodec.tuple(
            FishJournalEntry.PACKET_CODEC.collect(PacketCodecs.toList()), FishJournalS2CPacket::entries,
            FishJournalS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
