package net.busybee.ddv_fishing.client.item;

import net.busybee.ddv_fishing.item.LegendaryFishingRodItem;
import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemDisplayContext;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Renders the Legendary rod skin. Identical logic to {@link MagicalFishingRodRenderer} - the
 * animation state it reads ({@link MagicalFishingRodItem#ANIM_STATE}/{@code getClientAnimState})
 * is inherited from the base rod class, since the client only ever has one minigame running
 * regardless of which rod skin is equipped. Kept as a separate class only because the renderer
 * type parameter has to match the item type for GeckoLib's special-model lookup to find it.
 */
public class LegendaryFishingRodRenderer extends GeoItemRenderer<LegendaryFishingRodItem> {
    private static final long PERSPECTIVE_COUNT = ItemDisplayContext.values().length;

    public LegendaryFishingRodRenderer() {
        super(new LegendaryFishingRodModel());
    }

    @Override
    public long getInstanceId(LegendaryFishingRodItem animatable, RenderData renderData) {
        return (isLocalPlayersRod(renderData) ? 0L : 1L) * PERSPECTIVE_COUNT
                + renderData.renderPerspective().ordinal();
    }

    @Override
    public void addRenderData(LegendaryFishingRodItem animatable, RenderData renderData,
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

    private static boolean isLocalPlayersRod(RenderData renderData) {
        var owner = renderData.itemOwner();
        return owner != null && owner == MinecraftClient.getInstance().player;
    }
}
