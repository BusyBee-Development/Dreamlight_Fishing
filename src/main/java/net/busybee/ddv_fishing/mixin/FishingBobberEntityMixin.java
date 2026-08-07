package net.busybee.ddv_fishing.mixin;

import net.busybee.ddv_fishing.FishingLootHandler;
import net.busybee.ddv_fishing.access.FishingBobberReelAccess;
import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.busybee.ddv_fishing.networking.FishingMinigameS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
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
    private int rippleRarity = 0;

    @Unique
    private FishingRippleEntity activeRipple;

    /**
     * True when the bobber's owner is holding a fishing rod of any kind in either hand.
     */
    @Unique
    private boolean ddv$holdsFishingRod() {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;

        if (!(bobber.getOwner() instanceof PlayerEntity player)) return false;

        return FishingLootHandler.isFishingRod(player.getMainHandStack())
                || FishingLootHandler.isFishingRod(player.getOffHandStack());
    }

    /**
     * Keep the bobber alive for rods that are not literally {@code Items.FISHING_ROD}.
     * <p>
     * Vanilla's check is an identity test against the vanilla item, not
     * {@code instanceof FishingRodItem}. Any custom rod therefore casts fine and then has its
     * bobber discarded on the very first tick, so no line and no bobber ever appear. This
     * re-runs vanilla's own conditions with the broad {@link FishingLootHandler#isFishingRod}
     * test and reports "valid" when they pass. For a vanilla rod it falls through and changes
     * nothing.
     */
    @Inject(method = "removeIfInvalid", at = @At("HEAD"), cancellable = true)
    private void ddv$keepAliveForAnyRod(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;

        if (!player.isInteractable()) return;
        if (bobber.squaredDistanceTo(player) > 1024.0) return;
        if (!FishingLootHandler.isFishingRod(player.getMainHandStack())
                && !FishingLootHandler.isFishingRod(player.getOffHandStack())) return;

        cir.setReturnValue(false);
    }

    /**
     * Suppress vanilla fishing only while a ripple has the line.
     * <p>
     * This used to cancel for the whole time a Dreamlight rod was held, which meant that if a
     * ripple failed to register the rod caught nothing at all and looked broken. Away from a
     * ripple the rod now behaves exactly like a vanilla rod, so the minigame layers on top of
     * fishing instead of replacing it.
     */
    @Inject(method = "tickFishingLogic", at = @At("HEAD"), cancellable = true)
    private void onTickFishing(BlockPos pos, CallbackInfo ci) {
        if (this.minigameActive || this.rippleBiteDelay != -1) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkRipples(CallbackInfo ci) {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;
        //? if >1.21.8 {
        World world = bobber.getEntityWorld();
        //?} else {
        /*World world = bobber.getWorld();
        *///?}

        if (this.activeRipple != null && !this.activeRipple.isRemoved()) {
            this.activeRipple.refreshPositionAndAngles(bobber.getX(), bobber.getY(), bobber.getZ(), bobber.getYaw(), 0);
        }

        if (world.isClient() || this.minigameActive || bobber.getHookedEntity() != null || bobber.isRemoved()) return;
        if (!ddv$holdsFishingRod()) return;

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
                this.activeRipple = ripple;
                startMinigame(bobber, ripple);
                rippleBiteDelay = -2;
                ripple.setActivityState(FishingRippleEntity.STATE_BITE);
            }
        } else {
            rippleBiteDelay = -1;
        }
    }

    @Unique
    private void startMinigame(FishingBobberEntity bobber, FishingRippleEntity ripple) {
        if (bobber.getOwner() instanceof ServerPlayerEntity player) {
            this.minigameActive = true;
            this.rippleRarity = ripple.getRarity();
            int hits = switch (ripple.getRarity()) {
                case 1 -> 3;
                case 2 -> 4;
                default -> 2;
            };
            float speed = switch (ripple.getRarity()) {
                case 1 -> 0.035f;
                case 2 -> 0.05f;
                default -> 0.025f;
            } * net.busybee.ddv_fishing.Ddv_fishing.CONFIG.minigame_difficulty_multiplier;

            ServerPlayNetworking.send(player, new FishingMinigameS2CPacket(hits, speed, ripple.getRarity()));
        }
    }

    @Override
    public boolean ddv$consumeMinigameResult() {
        if (!this.minigameActive) {
            return false;
        }

        this.minigameActive = false;
        this.rippleBiteDelay = -1;
        if (this.activeRipple != null) {
            this.activeRipple.discard();
            this.activeRipple = null;
        }
        return true;
    }

    @Override
    public int ddv$getRarity() {
        return this.rippleRarity;
    }

    @Override
    public void ddv$setRarity(int rarity) {
        this.rippleRarity = rarity;
    }

    @Override
    public void ddv$setRippleState(int state) {
        if (this.activeRipple != null) {
            this.activeRipple.setActivityState(state);
        }
    }
}
