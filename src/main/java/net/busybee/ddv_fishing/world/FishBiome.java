package net.busybee.ddv_fishing.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * The biome flavours the fishing loot and ripple particles care about.
 * <p>
 * Single source of truth so {@code FishingRippleEntity}'s particles and {@code FishingLootHandler}'s
 * loot table pick agree on what a spot "is" - they used to each read the biome independently.
 */
public enum FishBiome {
    OCEAN, SWAMP, JUNGLE, OTHER,
    /**
     * Not a real world biome - {@link #classify} never returns this. It exists purely so the
     * Legendary capstone fish gets its own journal tab instead of being lumped into OTHER.
     */
    LEGENDARY;

    public static FishBiome classify(ServerWorld world, BlockPos pos) {
        RegistryEntry<Biome> biome = world.getBiome(pos);
        boolean isJungle = biome.getKey().map(key -> key.getValue().getPath().contains("jungle")).orElse(false);
        boolean isSwamp = biome.getKey().map(key -> key.getValue().getPath().contains("swamp")).orElse(false);
        boolean isOcean = biome.isIn(BiomeTags.IS_OCEAN);

        if (isOcean) return OCEAN;
        if (isJungle) return JUNGLE;
        if (isSwamp) return SWAMP;
        return OTHER;
    }

    /** Persisted/JSON form, used by {@code BiomeCompleteCriterion}'s advancement conditions. */
    public static final Codec<FishBiome> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                try {
                    return DataResult.success(FishBiome.valueOf(name.toUpperCase(java.util.Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown fish biome: " + name);
                }
            },
            biome -> biome.name().toLowerCase(java.util.Locale.ROOT)
    );
}
