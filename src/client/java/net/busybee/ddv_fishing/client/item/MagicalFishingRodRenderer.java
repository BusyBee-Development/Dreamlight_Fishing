package net.busybee.ddv_fishing.client.item;

import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Renders the Dreamlight fishing rod's GeckoLib model in inventories and in hand.
 * <p>
 * Reached via vanilla's special-model system: {@code assets/ddv_fishing/items/magical_fishing_rod.json}
 * points at the {@code geckolib:geckolib} special renderer, which looks this up through the
 * item's {@code GeoRenderProvider}.
 */
public class MagicalFishingRodRenderer extends GeoItemRenderer<MagicalFishingRodItem> {
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
     * advance each other's timeline.
     */
    @Override
    public long getInstanceId(MagicalFishingRodItem animatable, RenderData renderData) {
        return renderData.renderPerspective().ordinal();
    }
}
