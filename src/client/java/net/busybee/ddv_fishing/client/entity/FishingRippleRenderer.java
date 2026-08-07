package net.busybee.ddv_fishing.client.entity;

import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Renders the ripple rings, tinted to signal how hard the catch will be.
 * <p>
 * The generated texture is deliberately pale so this tint reads cleanly - multiplying a colour
 * over a saturated base would come out muddy. One texture and three tints replaces the three
 * separate coloured models an earlier attempt used.
 */
public class FishingRippleRenderer<R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<FishingRippleEntity, R> {

    public FishingRippleRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new FishingRippleModel());
    }

    @Override
    public int getRenderColor(FishingRippleEntity animatable, Void relatedObject, float partialTick) {
        return 0xFF000000 | FishingRippleEntity.getRarityRgb(animatable.getRarity());
    }
}
