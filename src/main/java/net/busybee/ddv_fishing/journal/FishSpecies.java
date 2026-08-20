package net.busybee.ddv_fishing.journal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.busybee.ddv_fishing.registry.ModItems;
import net.busybee.ddv_fishing.world.FishBiome;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The fish the journal tracks: the four vanilla catches plus the six Phase 2 biome fish. Junk and
 * treasure loot (rods, bows, enchanted books, sticks, ...) intentionally have no entry here - they
 * are not "species" and {@link #fromItem} returns empty for them, which is what keeps them out of
 * the journal and off the size roll entirely.
 * <p>
 * Biome bucketing is hardcoded rather than read from the loot tables at runtime: a species buckets
 * to {@link FishBiome#OCEAN}/{@link FishBiome#SWAMP}/{@link FishBiome#JUNGLE} only if it is
 * exclusive to that biome's tables, otherwise it falls to {@link FishBiome#OTHER} ("General").
 * Every vanilla fish is currently catchable everywhere, so all four land in General; the six custom
 * fish are each exclusive to one biome.
 */
public enum FishSpecies {
    COD(Items.COD, FishBiome.OTHER, 25f, 80f),
    SALMON(Items.SALMON, FishBiome.OTHER, 30f, 90f),
    TROPICAL_FISH(Items.TROPICAL_FISH, FishBiome.OTHER, 5f, 15f),
    PUFFERFISH(Items.PUFFERFISH, FishBiome.OTHER, 8f, 25f),
    OCEAN_PEARL(ModItems.OCEAN_PEARL, FishBiome.OCEAN, 1f, 6f),
    LARGE_FISH(ModItems.LARGE_FISH, FishBiome.OCEAN, 60f, 150f),
    ALGAE(ModItems.ALGAE, FishBiome.SWAMP, 10f, 40f),
    CATFISH(ModItems.CATFISH, FishBiome.SWAMP, 30f, 110f),
    EXOTIC_FISH(ModItems.EXOTIC_FISH, FishBiome.JUNGLE, 8f, 20f),
    RIVER_PIRANHA(ModItems.RIVER_PIRANHA, FishBiome.JUNGLE, 15f, 35f),
    /** The Legendary capstone catch - see {@link FishBiome#LEGENDARY}. Only reachable once every other species here has been caught. */
    STARFIN_LEVIATHAN(ModItems.STARFIN_LEVIATHAN, FishBiome.LEGENDARY, 100f, 220f);

    private static final FishSpecies[] VALUES = values();
    private static final Map<Item, FishSpecies> BY_ITEM = new HashMap<>();

    static {
        for (FishSpecies species : VALUES) {
            BY_ITEM.put(species.item, species);
        }
    }

    private final Item item;
    private final FishBiome biome;
    private final float minSize;
    private final float maxSize;

    FishSpecies(Item item, FishBiome biome, float minSize, float maxSize) {
        this.item = item;
        this.biome = biome;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public Item item() {
        return item;
    }

    public FishBiome biome() {
        return biome;
    }

    public float minSize() {
        return minSize;
    }

    public float maxSize() {
        return maxSize;
    }

    public static Optional<FishSpecies> fromItem(Item item) {
        return Optional.ofNullable(BY_ITEM.get(item));
    }

    /**
     * Persisted form, used inside {@link FishJournalEntry#CODEC}. A name no longer in the enum (a
     * species removed in a future update) fails this one entry via {@link DataResult#error} instead
     * of the whole journal - {@link FishJournalData#of} already tolerates a short entry list and
     * fills in the gap, so a bad name here is dropped rather than corrupting everything else.
     */
    public static final Codec<FishSpecies> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                try {
                    return DataResult.success(FishSpecies.valueOf(name));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown fish species: " + name);
                }
            },
            Enum::name
    );

    /**
     * Network form. Reads an out-of-range index as {@code COD} rather than throwing, mirroring
     * {@link net.busybee.ddv_fishing.networking.MinigameResult#CODEC}'s defensive clamp - this only
     * ever travels server-to-client here, but there's no reason to let a future misuse crash the
     * handler instead of just displaying the wrong row.
     */
    public static final PacketCodec<io.netty.buffer.ByteBuf, FishSpecies> PACKET_CODEC =
            PacketCodecs.indexed(i -> i >= 0 && i < VALUES.length ? VALUES[i] : COD, FishSpecies::ordinal);
}
