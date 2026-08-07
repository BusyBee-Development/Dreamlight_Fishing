package net.busybee.ddv_fishing.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Reports one timing-phase hit so the server can judge it.
 *
 * @param ticksSinceReset how many ticks the closing ring had been shrinking when the player
 *                        clicked. The server turns this back into a ring scale using the speed it
 *                        sent, and cross-checks it against its own clock - so the value is a claim
 *                        to be verified, not a measurement to be trusted.
 */
public record FishingMinigameHitC2SPacket(int ticksSinceReset) implements CustomPayload {
    public static final CustomPayload.Id<FishingMinigameHitC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of("ddv_fishing", "minigame_hit"));

    public static final PacketCodec<RegistryByteBuf, FishingMinigameHitC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, FishingMinigameHitC2SPacket::ticksSinceReset,
            FishingMinigameHitC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
