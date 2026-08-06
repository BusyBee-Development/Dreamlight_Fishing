package net.busybee.ddv_fishing.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FishingMinigamePhaseC2SPacket(int phase) implements CustomPayload {
    public static final CustomPayload.Id<FishingMinigamePhaseC2SPacket> ID = new CustomPayload.Id<>(Identifier.of("ddv_fishing", "minigame_phase"));
    public static final PacketCodec<RegistryByteBuf, FishingMinigamePhaseC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, FishingMinigamePhaseC2SPacket::phase,
            FishingMinigamePhaseC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
