package net.busybee.ddv_fishing.client.item;

import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeoModel for the Dreamlight fishing rod.
 * <p>
 * GeckoLib 5.x scans {@code assets/<ns>/geckolib/{animations,models}} and keys its caches by the
 * path with that prefix and the {@code .geo.json}/{@code .animation.json} suffix stripped, so the
 * model and animation ids here are the bare name, NOT a full file path.
 */
public class MagicalFishingRodModel extends GeoModel<MagicalFishingRodItem> {
    private static final Identifier ASSET = Identifier.of("ddv_fishing", "magical_fishing_rod");
    private static final Identifier TEXTURE = Identifier.of("ddv_fishing", "textures/item/magical_fishing_rod.png");

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return ASSET;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(MagicalFishingRodItem animatable) {
        return ASSET;
    }
}
