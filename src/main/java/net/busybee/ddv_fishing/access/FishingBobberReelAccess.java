package net.busybee.ddv_fishing.access;

/**
 * The minigame state the bobber carries, exposed to the packet handlers.
 * <p>
 * The validation methods exist because the client used to be trusted outright: it sent a result and
 * a perfect flag, and the server generated loot from them. The server now runs its own copy of the
 * timing and reeling rules so a forged success packet has nothing to land on.
 */
public interface FishingBobberReelAccess {
    /**
     * Claims the minigame result exactly once. Returns false if no minigame is running, which is
     * what stops a duplicate result packet from producing a second helping of loot.
     */
    boolean ddv$consumeMinigameResult();

    int ddv$getRarity();

    void ddv$setRarity(int rarity);

    void ddv$setRippleState(int state);

    /**
     * Checks and records one timing-phase hit.
     *
     * @param claimedTicksSinceReset how many ticks the client says the ring had been closing
     * @return true if the hit was inside the window and could have happened in the time available
     */
    boolean ddv$registerTimingHit(int claimedTicksSinceReset);

    /** True once the server has counted enough valid hits to justify the reeling phase. */
    boolean ddv$isTimingComplete();

    /** The server's own verdict on whether every hit was dead centre. The client's is not used. */
    boolean ddv$isPerfectCatch();

    /** Opens the reeling phase. False if the timing phase was never completed. */
    boolean ddv$beginReeling();

    /** True once enough time has passed since reeling began for a win to be physically possible. */
    boolean ddv$canCompleteReeling();
}
