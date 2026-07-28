package net.busybee.ddv_fishing.mixin.client;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FishingBobberEntityRenderer.class)
public class FishingBobberEntityRendererMixin {

    /*//? if <1.21.2 {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderMinigameTailLegacy(FishingBobberEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {
            FishingMinigameOverlay.renderWorld(matrices, vertexConsumers, tickDelta);
        }
    }
    //?} */

    //? if >=1.21.2 {
    /*//? if <1.21.11 {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderMinigameTailModern(@org.spongepowered.asm.mixin.injection.Coerce Object state, MatrixStack matrices, @org.spongepowered.asm.mixin.injection.Coerce Object vertexConsumers, int light, CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {
            VertexConsumerProvider provider;
            if (vertexConsumers instanceof VertexConsumerProvider vcp) {
                provider = vcp;
            } else {
                provider = net.minecraft.client.MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            }
            FishingMinigameOverlay.renderWorld(matrices, provider, 0);
        }
    }
    //?}
    *///?}

    //? if >=1.21.11 {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void renderMinigameTailLatest(@org.spongepowered.asm.mixin.injection.Coerce Object state, MatrixStack matrices, @org.spongepowered.asm.mixin.injection.Coerce Object vertexConsumers, @org.spongepowered.asm.mixin.injection.Coerce Object cameraState, CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {
            VertexConsumerProvider provider;
            if (vertexConsumers instanceof VertexConsumerProvider vcp) {
                provider = vcp;
            } else {
                provider = net.minecraft.client.MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            }

            float tickDelta = 0;
            //? if >=1.21.5 {
            /^tickDelta = net.minecraft.client.MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
            ^///?} else {
            tickDelta = net.minecraft.client.MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
            //?}

            FishingMinigameOverlay.renderWorld(matrices, provider, tickDelta);
        }
    }
    *///?}
}
