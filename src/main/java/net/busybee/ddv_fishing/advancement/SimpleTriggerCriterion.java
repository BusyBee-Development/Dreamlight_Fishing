package net.busybee.ddv_fishing.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

/**
 * A criterion with no conditions beyond the standard optional player predicate every criterion
 * needs - "this specific moment happened to this player," nothing more to filter on. Registered
 * multiple times under different ids (see {@code ModCriteria}) for the one-shot Phase 3
 * advancements that don't need any extra data: first catch, first Perfect Catch, first Epic
 * ripple, first Legendary ripple, full journal completion.
 * <p>
 * Safe to call {@link #trigger} on every catch that satisfies the condition rather than tracking
 * "already granted" separately - {@link net.minecraft.advancement.PlayerAdvancementTracker}
 * already no-ops a criterion that's already satisfied, the same way vanilla's own triggers do.
 */
public class SimpleTriggerCriterion extends AbstractCriterion<SimpleTriggerCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> true);
    }

    public record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player)
        ).apply(instance, Conditions::new));
    }
}
