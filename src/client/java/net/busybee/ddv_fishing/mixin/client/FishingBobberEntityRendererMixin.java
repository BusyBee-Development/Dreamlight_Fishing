package net.busybee.ddv_fishing.mixin.client;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntityRenderer.class)
public class FishingBobberEntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void applyVibrationAndRenderHead(@Coerce Object state, MatrixStack matrices, @Coerce Object commands, @Coerce Object cameraState, CallbackInfo ci) {
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
    private void renderMinigameTail(@Coerce Object state, MatrixStack matrices, @Coerce Object commands, @Coerce Object cameraState, CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {
            VertexConsumerProvider vcp = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            FishingMinigameOverlay.renderWorld(matrices, vcp, 0.0f);
        }
    }
}