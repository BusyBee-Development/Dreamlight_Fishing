package net.busybee.ddv_fishing.client.item;

import net.busybee.ddv_fishing.item.LegendaryFishingRodItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeoModel for the Legendary reskin - same wiring as {@link MagicalFishingRodModel}, pointed at
 * the {@code dreamlight_fishing_rod} asset instead of {@code magical_fishing_rod}.
 */
public class LegendaryFishingRodModel extends GeoModel<LegendaryFishingRodItem> {
    private static final Identifier ASSET = Identifier.of("ddv_fishing", "dreamlight_fishing_rod");
    private static final Identifier TEXTURE = Identifier.of("ddv_fishing", "textures/item/dreamlight_fishing_rod.png");

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return ASSET;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(LegendaryFishingRodItem animatable) {
        return ASSET;
    }
}
