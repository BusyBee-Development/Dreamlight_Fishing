package net.busybee.ddv_fishing.item;

import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The Dreamlight fishing rod.
 * <p>
 * Extends the vanilla {@link FishingRodItem} so casting, reeling and durability behave exactly
 * like a normal rod, and implements {@link GeoItem} so it renders with the GeckoLib model in
 * {@code assets/ddv_fishing/geckolib/models/magical_fishing_rod.geo.json}.
 */
public class MagicalFishingRodItem extends FishingRodItem implements GeoItem {

    /** Names must match the animations in magical_fishing_rod.animation.json. */
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("fishing_idle");
    /** The strike, then settle back into idle while the player times their clicks. */
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("bite").thenLoop("fishing_idle");
    private static final RawAnimation REELING = RawAnimation.begin().thenLoop("reeling");

    /** The rod is being held, nothing on the line. */
    public static final int ANIM_IDLE = 0;
    /** A fish has struck - the timing phase of the minigame. */
    public static final int ANIM_BITE = 1;
    /** The player is hauling the fish in. */
    public static final int ANIM_REELING = 2;

    /**
     * The animation state, carried per render state rather than read from the static field.
     * <p>
     * The renderer resolves it to {@link #ANIM_IDLE} for anyone who isn't the local player, which
     * is what keeps the minigame off other people's rods. The controller reads this ticket, so the
     * decision lives in one place instead of being baked into the shared field.
     */
    public static final DataTicket<Integer> ANIM_STATE = DataTicket.create("ddv_anim_state", Integer.class);

    /**
     * Which animation the local player's rod should be playing.
     * <p>
     * The minigame is a client-side HUD, so its phase never reaches the server and there is no
     * per-stack state to read. {@code FishingMinigameOverlay} pushes the phase in here, the renderer
     * copies it onto the render state via {@link #ANIM_STATE}, and the controller reads it there.
     * <p>
     * Still one field, because the local player has only one minigame at a time - but it no longer
     * reaches the controller directly. It used to, which meant every visible rod in multiplayer
     * animated along with your reel, and your own hotbar icon did too.
     */
    private static volatile int clientAnimState = ANIM_IDLE;

    /** @param state one of {@link #ANIM_IDLE}, {@link #ANIM_BITE}, {@link #ANIM_REELING}. */
    public static void setClientAnimState(int state) {
        clientAnimState = state;
    }

    /** Read by the renderer when it decides what to stamp onto a render state. */
    public static int getClientAnimState() {
        return clientAnimState;
    }

    /**
     * The client's {@code GeoRenderProvider}, installed by {@code Ddv_fishingClient}.
     * <p>
     * Held as {@link Object} on purpose: this class lives in the {@code main} source set, which
     * has no client classes on its compile classpath under {@code splitEnvironmentSourceSets()}.
     * GeckoLib upcasts the provider to {@code Object} for exactly this reason, so the type only
     * has to be known on the client side. Stays null on a dedicated server.
     */
    public static volatile Object clientRenderProvider;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MagicalFishingRodItem(Settings settings) {
        super(settings);
    }

    @Override
    public Object getRenderProvider() {
        return clientRenderProvider != null ? clientRenderProvider : GeoItem.super.getRenderProvider();
    }

    /**
     * Convenience check used by the fishing code to decide whether a held stack is our rod.
     */
    public static boolean isMagicalRod(ItemStack stack) {
        return stack.getItem() instanceof MagicalFishingRodItem;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Re-evaluated every frame. setAndContinue only restarts the animation when the chosen
        // RawAnimation actually changes, so holding a state keeps it looping rather than
        // retriggering it. The 4-tick transition blends the bone poses between states.
        controllers.add(new AnimationController<>("controller", 4, state -> switch (state.getDataOrDefault(ANIM_STATE, ANIM_IDLE)) {
            case ANIM_BITE -> state.setAndContinue(BITE);
            case ANIM_REELING -> state.setAndContinue(REELING);
            default -> state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
