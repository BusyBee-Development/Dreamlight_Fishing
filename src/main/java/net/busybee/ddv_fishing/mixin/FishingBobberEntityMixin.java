package net.busybee.ddv_fishing.mixin;

import net.busybee.ddv_fishing.FishingLootHandler;
import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.busybee.ddv_fishing.networking.FishingMinigameS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin implements FishingBobberReelAccess {

    @Unique
    private int rippleBiteDelay = -1;

    @Unique
    private boolean minigameActive;

    @Unique
    private boolean reeling;

    @Inject(method = "tickFishingLogic", at = @At("HEAD"), cancellable = true)
    private void onTickFishing(BlockPos pos, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        if (this.reeling) {
            FishingLootHandler.boost((FishingBobberEntity) (Object) this);
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkRipples(CallbackInfo ci) {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;
        World world = bobber.getWorld();
        if (world.isClient() || this.minigameActive || this.reeling || bobber.getHookedEntity() != null || bobber.isRemoved()) return;

        List<FishingRippleEntity> ripples = world.getEntitiesByClass(
                FishingRippleEntity.class,
                bobber.getBoundingBox().expand(0.5, 0.5, 0.5),
                ripple -> true
        );

        if (!ripples.isEmpty()) {
            FishingRippleEntity ripple = ripples.get(0);
            if (rippleBiteDelay == -1) {
                rippleBiteDelay = world.getRandom().nextBetween(40, 100);
            } else if (rippleBiteDelay > 0) {
                rippleBiteDelay--;
            } else if (rippleBiteDelay == 0) {
                startMinigame(bobber, ripple);
                rippleBiteDelay = -2;
                ripple.discard();
            }
        } else {
            rippleBiteDelay = -1;
        }
    }

    @Unique
    private void startMinigame(FishingBobberEntity bobber, FishingRippleEntity ripple) {
        if (bobber.getOwner() instanceof ServerPlayerEntity player) {
            this.minigameActive = true;
            int hits = switch (ripple.getRarity()) {
                case 1 -> 3;
                case 2 -> 4;
                default -> 2;
            };
            float speed = switch (ripple.getRarity()) {
                case 1 -> 0.035f;
                case 2 -> 0.05f;
                default -> 0.025f;
            };

            ServerPlayNetworking.send(player, new FishingMinigameS2CPacket(hits, speed));
        }
    }

    @Override
    public boolean ddv$consumeMinigameResult() {
        if (!this.minigameActive) {
            return false;
        }

        this.minigameActive = false;
        this.rippleBiteDelay = -1;
        return true;
    }

    @Override
    public boolean ddv$isReeling() {
        return this.reeling;
    }

    @Override
    public void ddv$setReeling(boolean reeling) {
        this.reeling = reeling;
    }
}
