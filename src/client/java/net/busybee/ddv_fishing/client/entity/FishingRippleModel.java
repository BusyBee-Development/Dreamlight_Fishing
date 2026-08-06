package net.busybee.ddv_fishing.client.entity;

import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.minecraft.util.Identifier;
//? if <1.21.2 {
/*import software.bernie.geckolib.model.GeoModel;
*///?} else {
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
//?}

//? if <1.21.2 {
/*public class FishingRippleModel extends GeoModel<FishingRippleEntity> {
*///?} else {
public class FishingRippleModel<R extends software.bernie.geckolib.renderer.base.GeoRenderState> extends GeoModel<FishingRippleEntity> {
//?}
    //? if <1.21.2 {
    /*@Override
    public Identifier getModelResource(FishingRippleEntity animatable) {
        return Identifier.of("ddv_fishing", "geo/fish_ripple.geo.json");
    }

    @Override
    public Identifier getTextureResource(FishingRippleEntity animatable) {
        return Identifier.of("ddv_fishing", "textures/entity/fish_ripple.png");
    }
    *///?} else {
    @Override
    public Identifier getModelResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("ddv_fishing", "geo/fish_ripple.geo.json");
    }

    @Override
    public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("ddv_fishing", "textures/entity/fish_ripple.png");
    }
    //?}

    @Override
    public Identifier getAnimationResource(FishingRippleEntity animatable) {
        return Identifier.of("ddv_fishing", "animations/fish_ripple.animation.json");
    }
}
