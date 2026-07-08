package net.busybee.ddv_fishing.world;

import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.busybee.ddv_fishing.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class RippleSpawner {
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % 20 == 0) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (world.getRandom().nextFloat() < 0.3f) {
                        spawnRippleNear(world, player);
                    }
                }
            }
        });
    }

    private static void spawnRippleNear(ServerWorld world, ServerPlayerEntity player) {
        int radius = 32;
        int maxAttempts = 20;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = player.getBlockX() + world.getRandom().nextBetween(-radius, radius);
            int z = player.getBlockZ() + world.getRandom().nextBetween(-radius, radius);

            for (int y = player.getBlockY() + 10; y > player.getBlockY() - 10; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                if (world.getBlockState(checkPos).isOf(Blocks.WATER) && world.getBlockState(checkPos.up()).isAir()) {

                    if (attempt > 15 || isNearLand(world, checkPos, 6)) {
                        FishingRippleEntity ripple = new FishingRippleEntity(ModEntities.FISHING_RIPPLE, world);
                        ripple.refreshPositionAndAngles(x + 0.5, y + 1.0, z + 0.5, 0, 0);

                        float r = world.getRandom().nextFloat();
                        int rarity = r < 0.7f ? 0 : (r < 0.9f ? 1 : 2);
                        ripple.setRarity(rarity);
                        
                        world.spawnEntity(ripple);
                        return;
                    }
                }
            }
        }
    }

    private static boolean isNearLand(ServerWorld world, BlockPos pos, int range) {
        for (int dx = -range; dx <= range; dx += 2) { // Step by 2 for performance
            for (int dz = -range; dz <= range; dz += 2) {
                BlockPos check = pos.add(dx, 0, dz);
                if (!world.getBlockState(check).isOf(Blocks.WATER) && !world.getBlockState(check).isAir()) {
                    return true;
                }
                if (!world.getBlockState(check.down()).isOf(Blocks.WATER) && !world.getBlockState(check.down()).isAir()) {
                    return true;
                }
            }
        }
        return false;
    }
}
