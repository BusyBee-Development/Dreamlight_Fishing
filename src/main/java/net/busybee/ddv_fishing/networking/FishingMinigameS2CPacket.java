package net.busybee.ddv_fishing.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FishingMinigameS2CPacket(int hits, float speed, int rarity) implements CustomPayload {
    public static final Id<FishingMinigameS2CPacket> ID = new Id<>(Identifier.of("ddv_fishing", "start_minigame"));
    
    public static final PacketCodec<RegistryByteBuf, FishingMinigameS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, FishingMinigameS2CPacket::hits,
            PacketCodecs.FLOAT, FishingMinigameS2CPacket::speed,
            PacketCodecs.VAR_INT, FishingMinigameS2CPacket::rarity,
            FishingMinigameS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
