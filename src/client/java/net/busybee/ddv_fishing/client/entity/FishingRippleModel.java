package net.busybee.ddv_fishing.client.entity;

import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeoModel for the ripple rings.
 * <p>
 * GeckoLib 5.x scans {@code assets/<ns>/geckolib/{animations,models}} and keys its caches by the
 * bare name, so the model and animation ids here carry neither a directory nor an extension. The
 * texture, being an ordinary resource, still needs its full path.
 */
public class FishingRippleModel extends GeoModel<FishingRippleEntity> {
    private static final Identifier ASSET = Identifier.of("ddv_fishing", "water_ripple");
    private static final Identifier TEXTURE = Identifier.of("ddv_fishing", "textures/entity/water_ripple.png");

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return ASSET;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(FishingRippleEntity animatable) {
        return ASSET;
    }
}
