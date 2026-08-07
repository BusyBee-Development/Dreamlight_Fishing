package net.busybee.ddv_fishing.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Tells the server how the minigame ended.
 * <p>
 * This used to also carry an {@code isPerfect} flag, which the server passed straight into the loot
 * bonus - a client could simply always claim a perfect catch. The server now tracks that itself from
 * the individual hits it validated, so there is nothing here to lie about beyond the outcome, and
 * a claimed {@link MinigameResult#SUCCESS} is checked against the server's own phase state.
 */
public record FishingMinigameResultC2SPacket(MinigameResult result) implements CustomPayload {
    public static final Id<FishingMinigameResultC2SPacket> ID = new Id<>(Identifier.of("ddv_fishing", "minigame_result"));

    public static final PacketCodec<RegistryByteBuf, FishingMinigameResultC2SPacket> CODEC = PacketCodec.tuple(
            MinigameResult.CODEC.cast(), FishingMinigameResultC2SPacket::result,
            FishingMinigameResultC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
