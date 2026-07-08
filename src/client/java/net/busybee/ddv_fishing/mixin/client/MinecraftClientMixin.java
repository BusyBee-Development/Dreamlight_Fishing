package net.busybee.ddv_fishing.mixin.client;

import net.busybee.ddv_fishing.client.hud.FishingMinigameOverlay;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onDoItemUse(CallbackInfo ci) {
        if (FishingMinigameOverlay.isActive()) {
            FishingMinigameOverlay.onAction();
            ci.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (FishingMinigameOverlay.isActive()) {
            FishingMinigameOverlay.onAction();
            cir.setReturnValue(false);
        }
    }
}
