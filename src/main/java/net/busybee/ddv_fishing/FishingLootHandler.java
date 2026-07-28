package net.busybee.ddv_fishing;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

public class FishingLootHandler {

    public static void catchFish(ServerPlayerEntity player, FishingBobberEntity bobber, ItemStack lootStack, boolean isPerfect) {
        //? if >1.21.8 {
        ServerWorld world = (ServerWorld) bobber.getEntityWorld();
        //?} else {
        /*ServerWorld world = (ServerWorld) bobber.getWorld();
        *///?}

        if (!player.getInventory().insertStack(lootStack)) {
            player.dropItem(lootStack, false);
        }

        if (isPerfect) {
            world.spawnParticles(ParticleTypes.SPLASH, bobber.getX(), bobber.getY(), bobber.getZ(), 20, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticles(ParticleTypes.BUBBLE, bobber.getX(), bobber.getY(), bobber.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 2.0F);
        
        damageFishingRod(player);
        bobber.discard();
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
