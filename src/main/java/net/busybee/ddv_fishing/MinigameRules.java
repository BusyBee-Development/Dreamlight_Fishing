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
            case 3 -> 5;
            default -> 2;
        };
    }

    /** Ring shrink per tick, before weather and the config multiplier. */
    public static float baseSpeedFor(int rarity) {
        return switch (rarity) {
            case 1 -> 0.035f;
            case 2 -> 0.05f;
            case 3 -> 0.06f;
            default -> 0.025f;
        };
    }

    /**
     * Tension added per tick while reeling.
     * <p>
     * Epic (2) is deliberately not a straight-line continuation of common/rare's step - see
     * {@link #progressGainFor} and {@link #resistanceFor} for why.
     */
    public static float tensionGainFor(int rarity) {
        return switch (rarity) {
            case 1 -> 0.020f;
            case 2 -> 0.021f;
            case 3 -> 0.0215f;
            default -> 0.015f;
        };
    }

    /**
     * Progress added per tick while reeling.
     * <p>
     * A winnable rarity needs {@code resistance * tensionGain < TENSION_RELIEF * progressGain} -
     * some reel/release ratio where progress trends up while tension trends flat or down. The old
     * linear formulas (tensionGain climbing while progressGain fell, both by a fixed step per
     * rarity) put epic on the wrong side of that inequality: even holding the reel key down the
     * entire time capped out around 57% progress before the tension cap forced a snap, and every
     * more cautious ratio just traded that for bleeding progress to zero instead. These three
     * values keep epic the hardest tier on every axis (worse than rare's tension gain, progress
     * gain, and resistance) while landing back on the winnable side, just with a much tighter
     * margin than rare's. Legendary (3) tightens that same margin further still - hardest on all
     * three axes, but solved against the same inequality rather than eyeballed, so it stays
     * winnable rather than repeating epic's old mistake.
     */
    public static float progressGainFor(int rarity) {
        return switch (rarity) {
            case 1 -> 0.010f;
            case 2 -> 0.0105f;
            case 3 -> 0.0098f;
            default -> 0.012f;
        };
    }

    /** Progress lost per tick while not reeling. */
    public static float resistanceFor(int rarity) {
        return switch (rarity) {
            case 1 -> 0.007f;
            case 2 -> 0.0075f;
            case 3 -> 0.0079f;
            default -> 0.004f;
        };
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
