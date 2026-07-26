package net.busybee.ddv_fishing.mixin.client;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
//? if <=1.21.8 {
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FishingBobberEntityRenderer.class)
public class FishingBobberEntityRendererMixin {

    //? if <=1.21.8 {
    @Inject(method = "render", at = @At("HEAD"))
    private void applyVibrationAndRenderHead(FishingBobberEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {

            float diff = FishingMinigameOverlay.getTargetDiff();
            float intensity = 0.005f + (0.8f - MathHelper.clamp(diff, 0, 0.8f)) * 0.035f;

            float time = (float) (System.nanoTime() / 1000000.0 % 10000.0);
            float freq = 0.12f;
            float offsetX = MathHelper.sin(time * freq) * intensity;
            float offsetY = MathHelper.cos(time * freq * 1.1f) * intensity;
            float offsetZ = MathHelper.sin(time * freq * 0.8f) * intensity;
            matrices.translate(offsetX, offsetY, offsetZ);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderMinigameTail(FishingBobberEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {
            FishingMinigameOverlay.renderWorld(matrices, vertexConsumers, tickDelta);
        }
    }
    //?}
}