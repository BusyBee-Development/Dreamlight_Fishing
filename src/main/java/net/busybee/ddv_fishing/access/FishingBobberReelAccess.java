package net.busybee.ddv_fishing.access;

public interface FishingBobberReelAccess {
    boolean ddv$consumeMinigameResult();
    int ddv$getRarity();
    void ddv$setRarity(int rarity);
    void ddv$setRippleState(int state);
}
