package net.busybee.ddv_fishing.mixin;

public interface FishingBobberReelAccess {
    boolean ddv$consumeMinigameResult();

    boolean ddv$isReeling();

    void ddv$setReeling(boolean reeling);
}