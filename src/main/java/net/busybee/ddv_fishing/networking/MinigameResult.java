package net.busybee.ddv_fishing.networking;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum MinigameResult {
    SUCCESS,
    SNAP,
    ESCAPE;

    public static final PacketCodec<io.netty.buffer.ByteBuf, MinigameResult> CODEC = PacketCodecs.indexed(i -> MinigameResult.values()[i], MinigameResult::ordinal);
}
