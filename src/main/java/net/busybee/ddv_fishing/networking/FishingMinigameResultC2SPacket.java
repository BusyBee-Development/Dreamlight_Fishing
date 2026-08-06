package net.busybee.ddv_fishing.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FishingMinigameResultC2SPacket(MinigameResult result, boolean isPerfect) implements CustomPayload {
    public static final Id<FishingMinigameResultC2SPacket> ID = new Id<>(Identifier.of("ddv_fishing", "minigame_result"));

    public static final PacketCodec<RegistryByteBuf, FishingMinigameResultC2SPacket> CODEC = PacketCodec.tuple(
            MinigameResult.CODEC.cast(), FishingMinigameResultC2SPacket::result,
            //? if >=1.21.4 {
            PacketCodecs.BOOLEAN, FishingMinigameResultC2SPacket::isPerfect,
            //?}
            //? if <1.21.4 {
            /*PacketCodecs.BOOL, FishingMinigameResultC2SPacket::isPerfect,
            *///?}
            FishingMinigameResultC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
