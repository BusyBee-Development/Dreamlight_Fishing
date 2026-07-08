package net.busybee.ddv_fishing;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class FishingLootHandler {
    private static final Map<Item, EntityType<? extends LivingEntity>> ITEM_TO_ENTITY = new HashMap<>();
    private static final Map<EntityType<?>, Item> ENTITY_TO_ITEM = new HashMap<>();
    private static final Map<LivingEntity, ServerPlayerEntity> TRACKED_FISH = new WeakHashMap<>();

    static {
        ITEM_TO_ENTITY.put(Items.COD, EntityType.COD);
        ITEM_TO_ENTITY.put(Items.SALMON, EntityType.SALMON);
        ITEM_TO_ENTITY.put(Items.TROPICAL_FISH, EntityType.TROPICAL_FISH);
        ITEM_TO_ENTITY.put(Items.PUFFERFISH, EntityType.PUFFERFISH);
        
        ITEM_TO_ENTITY.forEach((item, type) -> ENTITY_TO_ITEM.put(type, item));
    }

    public static void handleCatch(ServerPlayerEntity player, FishingBobberEntity bobber, ItemStack lootStack, boolean isPerfect) {
        ServerWorld world = (ServerWorld) bobber.getEntityWorld();
        EntityType<? extends LivingEntity> entityType = ITEM_TO_ENTITY.get(lootStack.getItem());

        if (entityType != null) {
            LivingEntity fish = entityType.create(world, SpawnReason.EVENT);
            if (fish != null) {
                fish.refreshPositionAndAngles(bobber.getX(), bobber.getY(), bobber.getZ(), world.getRandom().nextFloat() * 360.0F, 0.0F);
                fish.setHealth(1.0f);
                fish.setInvulnerable(true);
                
                applyYeetVelocity(player, bobber, fish, isPerfect);
                
                TRACKED_FISH.put(fish, player);
                world.spawnEntity(fish);

                if (isPerfect) {
                    world.spawnParticles(ParticleTypes.SPLASH, fish.getX(), fish.getY(), fish.getZ(), 20, 0.2, 0.2, 0.2, 0.1);
                    world.spawnParticles(ParticleTypes.BUBBLE, fish.getX(), fish.getY(), fish.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
                }
                
                damageFishingRod(player);
            }
        } else {
            if (!player.getInventory().insertStack(lootStack)) {
                player.dropItem(lootStack, false);
            }
        }
    }

    private static void applyYeetVelocity(ServerPlayerEntity player, FishingBobberEntity bobber, LivingEntity fish, boolean isPerfect) {
        double dx = player.getX() - bobber.getX();
        double dy = player.getY() - bobber.getY();
        double dz = player.getZ() - bobber.getZ();

        double baseForward = 0.11D;
        double baseUp = 0.5F;

        double yeetAmountForward = isPerfect ? baseForward * 1.3D : baseForward;
        double yeetAmountUp = isPerfect ? baseUp * 1.5F : baseUp;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double verticalBoost = Math.sqrt(distance) * 0.08D;

        fish.setVelocity(
                dx * yeetAmountForward,
                dy * yeetAmountUp * 0.5 + verticalBoost,
                dz * yeetAmountForward
        );
    }

    public static void tick() {
        Iterator<Map.Entry<LivingEntity, ServerPlayerEntity>> it = TRACKED_FISH.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, ServerPlayerEntity> entry = it.next();
            LivingEntity fish = entry.getKey();
            ServerPlayerEntity player = entry.getValue();

            if (fish.isRemoved() || !player.isAlive() || fish.getEntityWorld() != player.getEntityWorld()) {
                it.remove();
                continue;
            }

            fish.setYaw(fish.getYaw() + 30.0f);
            fish.setPitch(fish.getPitch() + 15.0f);

            // Reeling in effect: pull fish towards player
            double dx = player.getX() - fish.getX();
            double dy = (player.getY() + 1.2) - fish.getY();
            double dz = player.getZ() - fish.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.1) {
                double pullStrength = 0.12;
                fish.addVelocity(dx / dist * pullStrength, dy / dist * pullStrength, dz / dist * pullStrength);
                fish.velocityModified = true;
            }

            if (fish.getEntityWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SPLASH, fish.getX(), fish.getY(), fish.getZ(), 1, 0.1, 0.1, 0.1, 0.02);
                if (fish.age % 2 == 0) {
                    serverWorld.spawnParticles(ParticleTypes.BUBBLE, fish.getX(), fish.getY(), fish.getZ(), 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            if (fish.squaredDistanceTo(player) < 2.25D) {
                Item item = ENTITY_TO_ITEM.get(fish.getType());
                if (item != null) {
                    ItemStack stack = new ItemStack(item);
                    if (player.getInventory().insertStack(stack)) {
                        fish.discard();
                        it.remove();
                        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
                                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 2.0f);
                    }
                }
            }
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
}
