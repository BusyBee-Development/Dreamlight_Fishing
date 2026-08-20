package net.busybee.ddv_fishing.item;

/**
 * Cosmetic reskin of {@link MagicalFishingRodItem}, granted once as the Phase 3 capstone reward
 * for 100% journal completion. Behaves identically to the base rod - all casting/reeling/animation
 * logic is inherited - only the rendered model differs, via its own {@link #clientRenderProvider}.
 */
public class LegendaryFishingRodItem extends MagicalFishingRodItem {

    /** Same pattern as {@link MagicalFishingRodItem#clientRenderProvider}, installed by {@code Ddv_fishingClient}. */
    public static volatile Object legendaryClientRenderProvider;

    public LegendaryFishingRodItem(Settings settings) {
        super(settings);
    }

    /**
     * Falls back to {@code super.getRenderProvider()} - the base rod's own provider-or-GeoItem-
     * default logic - rather than repeating {@code GeoItem.super} here, which the compiler rejects
     * as redundant since {@link MagicalFishingRodItem} already implements {@code GeoItem}.
     */
    @Override
    public Object getRenderProvider() {
        return legendaryClientRenderProvider != null ? legendaryClientRenderProvider : super.getRenderProvider();
    }
}
