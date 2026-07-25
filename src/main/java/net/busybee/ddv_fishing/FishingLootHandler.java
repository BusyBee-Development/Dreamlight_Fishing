package net.busybee.ddv_fishing;

import net.busybee.ddv_fishing.mixin.FishingBobberReelAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
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

        //? if >1.21.1 {
        ServerWorld world = (ServerWorld) bobber.getEntityWorld();
        //?}
        //? if <=1.21.1 {
        /*ServerWorld world = (ServerWorld) bobber.getWorld();*/
        //?}

        //? if >1.21.1 {
        LivingEntity fish = entityType.create(world, (f) -> {}, player.getBlockPos(), SpawnReason.EVENT, false, false);
        //?}
        //? if <=1.21.1 {
        /*LivingEntity fish = entityType.create(world);*/
        //?}
        if (fish == null) {
            return false;
        }

        fish.refreshPositionAndAngles(bobber.getX(), bobber.getY(), bobber.getZ(), world.getRandom().nextFloat() * 360.0F, 0.0F);
        fish.setHealth(1.0F);
        fish.setInvulnerable(true);
        fish.setNoGravity(true);
        fish.setVelocity(Vec3d.ZERO);
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
                reel.boosting = 10;
                //? if >1.21.1 {
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                //?}
                //? if <=1.21.1 {
                /*ServerWorld world = (ServerWorld) player.getWorld();*/
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

            //? if >1.21.1 {
            boolean wrongWorld = fish.getEntityWorld() != player.getEntityWorld();
            //?}
            //? if <=1.21.1 {
            /*boolean wrongWorld = fish.getWorld() != player.getWorld();*/
            //?}
            if (fish.isRemoved() || bobber.isRemoved() || !player.isAlive() || wrongWorld) {
                cancelReel(reel);
                iterator.remove();
                continue;
            }

            reel.tickBoost();

            Vec3d target = new Vec3d(player.getX(), player.getY() + 1.2D, player.getZ());
            Vec3d pull = target.subtract(new Vec3d(fish.getX(), fish.getY(), fish.getZ()));
            double distance = pull.length();

            if (distance > 1.2D) {
                double speed = reel.isBoosting() ? 0.4D : -0.02D;
                fish.setVelocity(pull.normalize().multiply(speed));
                fish.setYaw(fish.getYaw() + (reel.isBoosting() ? 40.0F : 10.0F));
                fish.setPitch(fish.getPitch() + (reel.isBoosting() ? 20.0F : 5.0F));
                bobber.setPosition(fish.getX(), fish.getY(), fish.getZ());

                //? if >1.21.1 {
                ServerWorld world = (ServerWorld) fish.getEntityWorld();
                //?}
                //? if <=1.21.1 {
                /*ServerWorld world = (ServerWorld) fish.getWorld();*/
                //?}
                
                if (reel.isBoosting()) {
                    world.spawnParticles(ParticleTypes.SPLASH, fish.getX(), fish.getY(), fish.getZ(), 3, 0.1, 0.1, 0.1, 0.05);
                } else {
                    world.spawnParticles(ParticleTypes.BUBBLE, fish.getX(), fish.getY(), fish.getZ(), 1, 0.1, 0.1, 0.1, 0.01);
                }
                
                if (distance > 24.0D) { // Too far!
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

        private static void awardCatch(ReelState reel) {
        ServerPlayerEntity player = reel.player;
        ItemStack loot = reel.lootStack;

        if (!player.getInventory().insertStack(loot)) {
            player.dropItem(loot, false);
        }

        reel.fish.discard();
        reel.bobber.discard();
        //? if >1.21.1 {
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 2.0F);
        //?}
        //? if <=1.21.1 {
        /*player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 2.0F);*/
        //?}
        }

    private static void cancelReel(ReelState reel) {
        reel.fish.discard();
        if (!reel.bobber.isRemoved()) {
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

        ReelState(ServerPlayerEntity player, FishingBobberEntity bobber, LivingEntity fish, ItemStack lootStack) {
            this.player = player;
            this.bobber = bobber;
            this.fish = fish;
            this.lootStack = lootStack;
        }

        boolean isBoosting() { return boosting > 0; }
        void tickBoost() { if (boosting > 0) boosting--; }
    }
}