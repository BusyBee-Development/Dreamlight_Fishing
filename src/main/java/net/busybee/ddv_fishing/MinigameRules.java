package net.busybee.ddv_fishing;

/**
 * The numbers that drive the fishing minigame, in one place.
 * <p>
 * Both sides need these: the client to run the minigame, the server to check that the result it is
 * told about was actually reachable. They were previously literals duplicated across the client
 * overlay and the bobber mixin, which is exactly the setup where one side gets tuned and the other
 * silently disagrees.
 */
public final class MinigameRules {
    private MinigameRules() {}

    /** Scale the closing ring starts at. */
    public static final float RING_START = 2.0f;
    /** Scale of the fixed target ring - hits are judged against this. */
    public static final float TARGET_SCALE = 0.5f;
    /** Once the closing ring is smaller than this, the fish is gone. */
    public static final float RING_FAIL_SCALE = 0.2f;
    /** How far from {@link #TARGET_SCALE} still counts as a hit. */
    public static final float HIT_MARGIN = 0.12f;
    /** The tighter window during a thunderstorm. */
    public static final float HIT_MARGIN_THUNDER = 0.08f;
    /** How close every hit must be for the catch to count as perfect. */
    public static final float PERFECT_MARGIN = 0.05f;
    /** Rain makes the ring close faster. Applied server-side so both sides use one speed. */
    public static final float RAIN_SPEED_MULTIPLIER = 1.2f;
    /** The reeling phase opens part-filled so it doesn't feel hopeless. */
    public static final float REEL_START_PROGRESS = 0.25f;
    /** Tension bled off per tick while the reel key is released. */
    public static final float TENSION_RELIEF = 0.02f;

    /** Consecutive successful hits needed to move on to reeling. */
    public static int hitsFor(int rarity) {
        return switch (rarity) {
            case 1 -> 3;
            case 2 -> 4;
            default -> 2;
        };
    }

    /** Ring shrink per tick, before weather and the config multiplier. */
    public static float baseSpeedFor(int rarity) {
        return switch (rarity) {
            case 1 -> 0.035f;
            case 2 -> 0.05f;
            default -> 0.025f;
        };
    }

    /** Tension added per tick while reeling. */
    public static float tensionGainFor(int rarity) {
        return 0.015f + rarity * 0.005f;
    }

    /** Progress added per tick while reeling. */
    public static float progressGainFor(int rarity) {
        return 0.012f - rarity * 0.002f;
    }

    /** Progress lost per tick while not reeling. */
    public static float resistanceFor(int rarity) {
        return 0.004f + rarity * 0.003f;
    }

    /** The margin in force right now - a storm tightens it. */
    public static float hitMargin(boolean thundering) {
        return thundering ? HIT_MARGIN_THUNDER : HIT_MARGIN;
    }

    /** Where the closing ring sits after {@code ticks} ticks at {@code speed}. */
    public static float ringScaleAt(int ticks, float speed) {
        return RING_START - speed * ticks;
    }

    /**
     * The fewest ticks the reeling phase can take, assuming the player holds the key the whole way
     * and never lets tension force a pause. Used as the server's floor for a plausible win.
     */
    public static int minReelTicks(int rarity) {
        return (int) Math.floor((1.0f - REEL_START_PROGRESS) / progressGainFor(rarity));
    }
}
