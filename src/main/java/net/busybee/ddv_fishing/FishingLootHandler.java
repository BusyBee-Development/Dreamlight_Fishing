package net.busybee.ddv_fishing;

import net.busybee.ddv_fishing.access.FishingBobberReelAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FishingLootHandler {
    private static final double CATCH_DISTANCE = 1.2D;
    private static final double SNAP_DISTANCE = 24.0D;
    private static final double RESISTANCE_SPEED = -0.015D;
    private static final double PULL_SPEED = 0.24D;
    private static final double MAX_SPEED = 0.28D;
    private static final double ARRIVAL_DISTANCE = 4.0D;
    private static final double BOBBER_CORRECTION_DISTANCE = 0.35D;
    private static final Map<Item, EntityType<? extends LivingEntity>> ITEM_TO_ENTITY = new HashMap<>();
    private static final Map<UUID, ReelState> ACTIVE_REELS = new HashMap<>();

    static {
        ITEM_TO_ENTITY.put(Items.COD, EntityType.COD);
        ITEM_TO_ENTITY.put(Items.SALMON, EntityType.SALMON);
        ITEM_TO_ENTITY.put(Items.TROPICAL_FISH, EntityType.TROPICAL_FISH);
        ITEM_TO_ENTITY.put(Items.PUFFERFISH, EntityType.PUFFERFISH);
    }

    public static boolean startReeling(ServerPlayerEntity player, FishingBobberEntity bobber, ItemStack lootStack, boolean isPerfect) {
        if (ACTIVE_REELS.containsKey(player.getUuid())) {
            return false;
        }

        EntityType<? extends LivingEntity> entityType = ITEM_TO_ENTITY.get(lootStack.getItem());
        if (entityType == null) {
            return false;
        }

        //? if >1.21.8 {
        /*ServerWorld world = (ServerWorld) bobber.getEntityWorld();
        *///?}
        //? if <=1.21.8 {
        ServerWorld world = (ServerWorld) bobber.getWorld();
        //?}

        //? if >1.21.1 {
        LivingEntity fish = entityType.create(world, (f) -> {}, player.getBlockPos(), SpawnReason.EVENT, false, false);
        //?}
        //? if <=1.21.1 {
        /*LivingEntity fish = entityType.create(world);
        *///?}
        if (fish == null) {
            return false;
        }

        fish.refreshPositionAndAngles(bobber.getX(), bobber.getY(), bobber.getZ(), world.getRandom().nextFloat() * 360.0F, 0.0F);
        fish.setHealth(1.0F);
        fish.setInvulnerable(true);
        fish.setNoGravity(true);
        if (fish instanceof MobEntity mob) {
            mob.setAiDisabled(true);
        }
        fish.setVelocity(Vec3d.ZERO);
        bobber.setNoGravity(true);
        world.spawnEntity(fish);

        ACTIVE_REELS.put(player.getUuid(), new ReelState(player, bobber, fish, lootStack.copy()));
        ((FishingBobberReelAccess) bobber).ddv$setReeling(true);
        damageFishingRod(player);

        if (isPerfect) {
            world.spawnParticles(ParticleTypes.SPLASH, fish.getX(), fish.getY(), fish.getZ(), 20, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticles(ParticleTypes.BUBBLE, fish.getX(), fish.getY(), fish.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
        }

        return true;
    }

    public static void boost(FishingBobberEntity bobber) {
        if (bobber.getOwner() instanceof ServerPlayerEntity player) {
            ReelState reel = ACTIVE_REELS.get(player.getUuid());
            if (reel != null && reel.bobber == bobber) {
                reel.boosting = 8;
                //? if >1.21.8 {
                /*ServerWorld world = (ServerWorld) player.getEntityWorld();
                *///?}
                //? if <=1.21.8 {
                ServerWorld world = (ServerWorld) player.getWorld();
                //?}
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.25F, 1.5F);
                world.spawnParticles(ParticleTypes.FISHING, reel.fish.getX(), reel.fish.getY() + 0.5, reel.fish.getZ(), 5, 0.1, 0.1, 0.1, 0.05);
                damageFishingRod(player);
            }
        }
        }

        public static void tick() {
        Iterator<ReelState> iterator = ACTIVE_REELS.values().iterator();
        while (iterator.hasNext()) {
            ReelState reel = iterator.next();
            LivingEntity fish = reel.fish;
            ServerPlayerEntity player = reel.player;
            FishingBobberEntity bobber = reel.bobber;

            //? if >1.21.8 {
            /*boolean wrongWorld = fish.getEntityWorld() != player.getEntityWorld();
            *///?}
            //? if <=1.21.8 {
            boolean wrongWorld = fish.getWorld() != player.getWorld();
            //?}
            if (fish.isRemoved() || bobber.isRemoved() || !player.isAlive() || wrongWorld) {
                cancelReel(reel);
                iterator.remove();
                continue;
            }

            reel.tickPull();

            Vec3d target = new Vec3d(player.getX(), player.getY() + 1.2D, player.getZ());
            Vec3d pull = target.subtract(new Vec3d(fish.getX(), fish.getY(), fish.getZ()));
            double distance = pull.length();

            if (distance > CATCH_DISTANCE) {
                Vec3d direction = pull.normalize();
                double desiredSpeed = RESISTANCE_SPEED + (PULL_SPEED - RESISTANCE_SPEED) * reel.pullStrength;
                if (desiredSpeed > 0.0D && distance < ARRIVAL_DISTANCE) {
                    desiredSpeed *= Math.max(0.2D, (distance - CATCH_DISTANCE) / (ARRIVAL_DISTANCE - CATCH_DISTANCE));
                }

                Vec3d desiredVelocity = direction.multiply(desiredSpeed);
                Vec3d currentVelocity = fish.getVelocity();
                Vec3d nextVelocity = currentVelocity.multiply(0.72D).add(desiredVelocity.multiply(0.28D));
                if (nextVelocity.lengthSquared() > MAX_SPEED * MAX_SPEED) {
                    nextVelocity = nextVelocity.normalize().multiply(MAX_SPEED);
                }

                fish.setVelocity(nextVelocity);
                updateFishRotation(fish, nextVelocity);

                bobber.setVelocity(nextVelocity);
                double bobberDx = bobber.getX() - fish.getX();
                double bobberDy = bobber.getY() - fish.getY();
                double bobberDz = bobber.getZ() - fish.getZ();
                if (bobberDx * bobberDx + bobberDy * bobberDy + bobberDz * bobberDz
                        > BOBBER_CORRECTION_DISTANCE * BOBBER_CORRECTION_DISTANCE) {
                    bobber.setPosition(fish.getX(), fish.getY(), fish.getZ());
                }

                //? if >1.21.8 {
                /*ServerWorld world = (ServerWorld) fish.getEntityWorld();
                *///?}
                //? if <=1.21.8 {
                ServerWorld world = (ServerWorld) fish.getWorld();
                //?}
                
                if (reel.isBoosting() && reel.age % 2 == 0) {
                    world.spawnParticles(ParticleTypes.SPLASH, fish.getX(), fish.getY(), fish.getZ(), 3, 0.1, 0.1, 0.1, 0.05);
                } else if (reel.age % 4 == 0) {
                    world.spawnParticles(ParticleTypes.BUBBLE, fish.getX(), fish.getY(), fish.getZ(), 1, 0.1, 0.1, 0.1, 0.01);
                }
                
                if (distance > SNAP_DISTANCE) {
                    player.sendMessage(net.minecraft.text.Text.literal("The line snapped!"), true);
                    cancelReel(reel);
                    iterator.remove();
                }
                continue;
            }

            awardCatch(reel);
            iterator.remove();
        }
    }

    private static void updateFishRotation(LivingEntity fish, Vec3d velocity) {
        double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (horizontalSpeed < 0.001D && Math.abs(velocity.y) < 0.001D) {
            return;
        }

        float targetYaw = (float) (Math.atan2(-velocity.x, velocity.z) * 180.0D / Math.PI);
        float targetPitch = (float) (Math.atan2(-velocity.y, Math.max(0.001D, horizontalSpeed)) * 180.0D / Math.PI);
        targetPitch = Math.max(-25.0F, Math.min(25.0F, targetPitch));
        fish.setYaw(fish.getYaw() + wrapDegrees(targetYaw - fish.getYaw()) * 0.25F);
        fish.setPitch(fish.getPitch() + (targetPitch - fish.getPitch()) * 0.2F);
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) wrapped -= 360.0F;
        if (wrapped < -180.0F) wrapped += 360.0F;
        return wrapped;
    }

    private static void awardCatch(ReelState reel) {
        ServerPlayerEntity player = reel.player;
        ItemStack loot = reel.lootStack;

        if (!player.getInventory().insertStack(loot)) {
            player.dropItem(loot, false);
        }

        reel.fish.discard();
        reel.bobber.discard();
        //? if >1.21.8 {
        /*player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 2.0F);
        *///?}
        //? if <=1.21.8 {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 2.0F);
        //?}
        }

    private static void cancelReel(ReelState reel) {
        reel.fish.discard();
        if (!reel.bobber.isRemoved()) {
            reel.bobber.setNoGravity(false);
            ((FishingBobberReelAccess) reel.bobber).ddv$setReeling(false);
        }
    }

    private static void damageFishingRod(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        if (mainHand.isOf(Items.FISHING_ROD)) {
            mainHand.damage(1, player, EquipmentSlot.MAINHAND);
        } else if (offHand.isOf(Items.FISHING_ROD)) {
            offHand.damage(1, player, EquipmentSlot.OFFHAND);
        }
    }

    private static class ReelState {
        final ServerPlayerEntity player;
        final FishingBobberEntity bobber;
        final LivingEntity fish;
        final ItemStack lootStack;
        int boosting;
        int age;
        double pullStrength;

        ReelState(ServerPlayerEntity player, FishingBobberEntity bobber, LivingEntity fish, ItemStack lootStack) {
            this.player = player;
            this.bobber = bobber;
            this.fish = fish;
            this.lootStack = lootStack;
        }

        boolean isBoosting() { return boosting > 0; }
        void tickPull() {
            age++;
            double targetStrength = isBoosting() ? 1.0D : 0.0D;
            double easing = isBoosting() ? 0.25D : 0.12D;
            pullStrength += (targetStrength - pullStrength) * easing;
            if (boosting > 0) boosting--;
        }
    }
}
