package net.busybee.ddv_fishing.client.entity;

import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
//? if <1.21.2 {
/*import software.bernie.geckolib.renderer.GeoEntityRenderer;
*///?} else {
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
//?}

//? if <1.21.2 {
/*public class FishingRippleRenderer extends GeoEntityRenderer<FishingRippleEntity> {
    public FishingRippleRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new FishingRippleModel());
    }
}
*///?} else {
public class FishingRippleRenderer<R extends net.minecraft.client.render.entity.state.EntityRenderState & GeoRenderState> extends GeoEntityRenderer<FishingRippleEntity, R> {
    public FishingRippleRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new FishingRippleModel<>());
    }
}
//?}
