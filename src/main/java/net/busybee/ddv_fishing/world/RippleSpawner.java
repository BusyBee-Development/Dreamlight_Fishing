package net.busybee.ddv_fishing.world;

import net.busybee.ddv_fishing.Ddv_fishing;
import net.busybee.ddv_fishing.entity.FishingRippleEntity;
import net.busybee.ddv_fishing.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RippleSpawner {
    /** Height a full still-water block's surface actually renders at, relative to the block base. */
    private static final double WATER_SURFACE_HEIGHT = 8.0 / 9.0;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (Ddv_fishing.CONFIG.enabled && world.getServer().getTicks() % 20 == 0) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (world.getRandom().nextFloat() < Ddv_fishing.CONFIG.ripple_spawn_chance) {
                        spawnRippleNear(world, player);
                    }
                }
            }
        });
    }

    private static void spawnRippleNear(ServerWorld world, ServerPlayerEntity player) {
        int radius = Ddv_fishing.CONFIG.search_radius;
        int maxAttempts = 20;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = player.getBlockX() + world.getRandom().nextBetween(-radius, radius);
            int z = player.getBlockZ() + world.getRandom().nextBetween(-radius, radius);
            
            for (int y = player.getBlockY() + 10; y > player.getBlockY() - 10; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                if (world.getFluidState(checkPos).isIn(FluidTags.WATER) && (Ddv_fishing.CONFIG.spawn_in_flowing_water || world.getFluidState(checkPos).isStill()) && world.getBlockState(checkPos.up()).isAir()) {
                    if (!isOpenWater(world, checkPos)) continue;

                    if (attempt > 15 || isNearLand(world, checkPos, 6)) {
                        FishingRippleEntity ripple = new FishingRippleEntity(ModEntities.FISHING_RIPPLE, world);
                        // Still water renders its surface at 8/9 of a block, not the block top, so
                        // y + 1.0 left the ring hovering visibly above the water. The extra sliver
                        // keeps the flat plate from z-fighting with the surface it sits on.
                        ripple.refreshPositionAndAngles(x + 0.5, y + WATER_SURFACE_HEIGHT + 0.01, z + 0.5, 0, 0);

                        ripple.setRarity(rollRarity(world, player));
                        
                        world.spawnEntity(ripple);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Picks a ripple rarity: 0 blue, 1 green, 2 orange.
     * <p>
     * Reads the two chances from config so the mix can be tuned - and so orange can be turned up
     * to test, since at its 10% default you can fish for a long while without ever meeting one.
     * <p>
     * Luck raises the roll rather than lowering it. It used to subtract, which pushed every lucky
     * player towards blue: exactly backwards, and on by default.
     */
    private static int rollRarity(ServerWorld world, ServerPlayerEntity player) {
        float orange = Math.max(0.0f, Ddv_fishing.CONFIG.orange_ripple_chance);
        float green = Math.max(0.0f, Ddv_fishing.CONFIG.green_ripple_chance);
        // A misconfigured pair that sums past 1 would make blue unreachable and orange's own
        // threshold negative, so scale them back into range instead of trusting the file.
        if (orange + green > 1.0f) {
            float scale = 1.0f / (orange + green);
            orange *= scale;
            green *= scale;
        }

        float r = world.getRandom().nextFloat();
        if (Ddv_fishing.CONFIG.luck_affects_rarity) {
            r += player.getLuck() * 0.05f;
        }

        if (r >= 1.0f - orange) return 2;
        if (r >= 1.0f - orange - green) return 1;
        return 0;
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

    private static boolean isOpenWater(ServerWorld world, BlockPos pos) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (!world.getFluidState(pos.offset(direction)).isIn(FluidTags.WATER)) {
                return false;
            }
        }
        return true;
    }
}
