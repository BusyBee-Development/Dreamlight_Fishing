package net.busybee.ddv_fishing.mixin;

import net.busybee.ddv_fishing.Ddv_fishing;
import net.busybee.ddv_fishing.FishingLootHandler;
import net.busybee.ddv_fishing.MinigameRules;
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
     * Slack in ticks when comparing the client's claimed ring time against the server's own clock.
     * <p>
     * The server measures from when it received the previous hit, which is later than when the
     * client actually reset its ring, so under jitter a legitimate claim can run a tick or two
     * ahead. This is wide enough to absorb that and still nowhere near the ~50 ticks a forged
     * "instant hit" would have to invent.
     */
    @Unique
    private static final int DDV_TICK_SLACK = 5;

    /** Same idea for the reeling phase, where the count is longer and one bad tick matters less. */
    @Unique
    private static final int DDV_REEL_SLACK = 10;

    /** Ring speed as sent to the client, weather and config already folded in. */
    @Unique
    private float ddv$minigameSpeed;

    /** Hits the server requires before it will accept the reeling phase. */
    @Unique
    private int ddv$requiredHits;

    @Unique
    private int ddv$hitCount;

    /** Server time the ring was last known to have restarted. */
    @Unique
    private long ddv$lastRingReset;

    @Unique
    private boolean ddv$timingComplete;

    @Unique
    private boolean ddv$perfect;

    @Unique
    private long ddv$reelStart = -1L;

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
        // With the mod disabled the rod must behave exactly like a vanilla rod. Without this the
        // spawner stops making ripples but any ripple still in the world keeps hooking players.
        if (!Ddv_fishing.CONFIG.enabled) return;
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
            //? if >1.21.8 {
            World world = bobber.getEntityWorld();
            //?} else {
            /*World world = bobber.getWorld();
            *///?}

            this.minigameActive = true;
            this.rippleRarity = ripple.getRarity();

            int hits = MinigameRules.hitsFor(this.rippleRarity);
            float speed = MinigameRules.baseSpeedFor(this.rippleRarity)
                    * Ddv_fishing.CONFIG.minigame_difficulty_multiplier;
            // Rain used to be applied on the client after the packet arrived, which meant the two
            // sides disagreed about how fast the ring was closing - and the server can't check a
            // hit against a speed it doesn't know. Folded in here so the sent value is final.
            if (world.isRaining()) {
                speed *= MinigameRules.RAIN_SPEED_MULTIPLIER;
            }

            this.ddv$minigameSpeed = speed;
            this.ddv$requiredHits = hits;
            this.ddv$hitCount = 0;
            this.ddv$timingComplete = false;
            this.ddv$perfect = true;
            this.ddv$reelStart = -1L;
            this.ddv$lastRingReset = world.getTime();

            ServerPlayNetworking.send(player, new FishingMinigameS2CPacket(hits, speed, this.rippleRarity));
        }
    }

    @Override
    public boolean ddv$registerTimingHit(int claimedTicksSinceReset) {
        if (!this.minigameActive || this.ddv$timingComplete) return false;
        if (claimedTicksSinceReset < 0) return false;

        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;
        //? if >1.21.8 {
        long now = bobber.getEntityWorld().getTime();
        //?} else {
        /*long now = bobber.getWorld().getTime();
        *///?}

        // A client claiming more ring time than has actually passed is compressing the minigame -
        // the shape an auto-completer takes, firing every hit in one tick.
        if (claimedTicksSinceReset > (now - this.ddv$lastRingReset) + DDV_TICK_SLACK) return false;

        float diff = Math.abs(MinigameRules.ringScaleAt(claimedTicksSinceReset, this.ddv$minigameSpeed)
                - MinigameRules.TARGET_SCALE);
        // Always judged against the wide margin. A storm tightens the client's window, and weather
        // can turn between the click and this check, so being lenient here can only ever accept a
        // hit the player really did land - never reject one.
        if (diff > MinigameRules.HIT_MARGIN) return false;

        if (diff > MinigameRules.PERFECT_MARGIN) {
            this.ddv$perfect = false;
        }

        this.ddv$hitCount++;
        this.ddv$lastRingReset = now;
        if (this.ddv$hitCount >= this.ddv$requiredHits) {
            this.ddv$timingComplete = true;
        }
        return true;
    }

    @Override
    public boolean ddv$isTimingComplete() {
        return this.ddv$timingComplete;
    }

    @Override
    public boolean ddv$isPerfectCatch() {
        return this.ddv$perfect;
    }

    @Override
    public boolean ddv$beginReeling() {
        if (!this.minigameActive || !this.ddv$timingComplete || this.ddv$reelStart != -1L) return false;

        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;
        //? if >1.21.8 {
        this.ddv$reelStart = bobber.getEntityWorld().getTime();
        //?} else {
        /*this.ddv$reelStart = bobber.getWorld().getTime();
        *///?}
        return true;
    }

    @Override
    public boolean ddv$canCompleteReeling() {
        if (!this.ddv$timingComplete || this.ddv$reelStart == -1L) return false;

        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;
        //? if >1.21.8 {
        long elapsed = bobber.getEntityWorld().getTime() - this.ddv$reelStart;
        //?} else {
        /*long elapsed = bobber.getWorld().getTime() - this.ddv$reelStart;
        *///?}

        // Filling the progress bar takes a known minimum number of ticks even with the key held
        // down the whole way, so a success that arrives sooner than that never happened.
        return elapsed >= MinigameRules.minReelTicks(this.rippleRarity) - DDV_REEL_SLACK;
    }

    @Override
    public boolean ddv$consumeMinigameResult() {
        if (!this.minigameActive) {
            return false;
        }

        this.minigameActive = false;
        this.rippleBiteDelay = -1;
        this.ddv$timingComplete = false;
        this.ddv$hitCount = 0;
        this.ddv$reelStart = -1L;
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
