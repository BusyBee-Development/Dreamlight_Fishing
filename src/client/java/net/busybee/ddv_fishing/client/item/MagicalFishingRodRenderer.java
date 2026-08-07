package net.busybee.ddv_fishing.client.item;

import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemDisplayContext;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Renders the Dreamlight fishing rod's GeckoLib model in inventories and in hand.
 * <p>
 * Reached via vanilla's special-model system: {@code assets/ddv_fishing/items/magical_fishing_rod.json}
 * points at the {@code geckolib:geckolib} special renderer, which looks this up through the
 * item's {@code GeoRenderProvider}.
 */
public class MagicalFishingRodRenderer extends GeoItemRenderer<MagicalFishingRodItem> {
    /** Stride for the owner bit in {@link #getInstanceId}, so the two ranges can't collide. */
    private static final long PERSPECTIVE_COUNT = ItemDisplayContext.values().length;

    public MagicalFishingRodRenderer() {
        super(new MagicalFishingRodModel());
    }

    /**
     * Give the rod a stable animation instance instead of one keyed on the stack's identity.
     * <p>
     * GeckoLib's default is {@code GeoItem.getId(stack)}, which reads a stack component and, when
     * that component was never assigned, falls back to {@code stack.hashCode()} - the identity
     * hash. The client replaces the held ItemStack object on every inventory sync, so that id
     * keeps changing, and each new id hands the controller a fresh manager that restarts the
     * animation from tick zero. The result is a rod that never visibly animates.
     * <p>
     * Keying on the render perspective instead is stable for the life of the game, and still
     * keeps the hotbar icon and the in-hand rod on separate animation instances so they do not
     * advance each other's timeline. The owner bit separates your rod from everyone else's, so a
     * reeling animation and an idle one can be in flight at the same perspective without the two
     * fighting over one manager.
     */
    @Override
    public long getInstanceId(MagicalFishingRodItem animatable, RenderData renderData) {
        return (isLocalPlayersRod(renderData) ? 0L : 1L) * PERSPECTIVE_COUNT
                + renderData.renderPerspective().ordinal();
    }

    /**
     * Stamp the minigame's animation state onto the render state, but only for the rod the local
     * player is holding.
     * <p>
     * The state is a client-side static, so reading it in the controller applied it to every rod
     * being rendered: other players' rods reeled in sympathy with yours, and so did your own
     * inventory and hotbar icons. Resolving it here means the controller sees {@code ANIM_IDLE} for
     * everything that isn't the rod in your hands.
     */
    @Override
    public void addRenderData(MagicalFishingRodItem animatable, RenderData renderData,
                              GeoRenderState renderState, float partialTick) {
        super.addRenderData(animatable, renderData, renderState, partialTick);

        boolean inHand = renderData.renderPerspective().isFirstPerson()
                || renderData.renderPerspective() == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || renderData.renderPerspective() == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        int state = inHand && isLocalPlayersRod(renderData)
                ? MagicalFishingRodItem.getClientAnimState()
                : MagicalFishingRodItem.ANIM_IDLE;

        renderState.addGeckolibData(MagicalFishingRodItem.ANIM_STATE, state);
    }

    /**
     * True when this render is of the rod the local player is holding.
     * <p>
     * Compared by reference against the client's own player entity rather than by name or UUID -
     * there is exactly one local player object, so identity is the cheapest correct test.
     */
    private static boolean isLocalPlayersRod(RenderData renderData) {
        var owner = renderData.itemOwner();
        return owner != null && owner == MinecraftClient.getInstance().player;
    }
}
