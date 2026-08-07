package net.busybee.ddv_fishing.networking;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum MinigameResult {
    SUCCESS,
    SNAP,
    ESCAPE,
    /** The minigame ended without being played out - the player died, disconnected, or reeled in. */
    CANCEL;

    private static final MinigameResult[] VALUES = values();

    /**
     * Reads an out-of-range index as {@link #CANCEL} rather than throwing. The index arrives from
     * the client, so indexing the array directly turned a malformed packet into an exception inside
     * the network handler.
     */
    public static final PacketCodec<io.netty.buffer.ByteBuf, MinigameResult> CODEC =
            PacketCodecs.indexed(i -> i >= 0 && i < VALUES.length ? VALUES[i] : CANCEL, MinigameResult::ordinal);
}
