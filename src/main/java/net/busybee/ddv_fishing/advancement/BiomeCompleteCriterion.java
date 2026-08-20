package net.busybee.ddv_fishing.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.busybee.ddv_fishing.world.FishBiome;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

/**
 * Fires whenever a specific biome's journal tab is fully caught - one registered criterion,
 * {@code biome} in the advancement's own JSON conditions picks which of Ocean/Swamp/Jungle it's
 * for, rather than needing three separate criterion classes.
 */
public class BiomeCompleteCriterion extends AbstractCriterion<BiomeCompleteCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, FishBiome biome) {
        this.trigger(player, conditions -> conditions.biome() == biome);
    }

    public record Conditions(Optional<LootContextPredicate> player, FishBiome biome) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                FishBiome.CODEC.fieldOf("biome").forGetter(Conditions::biome)
        ).apply(instance, Conditions::new));
    }
}
