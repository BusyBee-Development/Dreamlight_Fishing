package net.busybee.ddv_fishing.registry;

import net.busybee.ddv_fishing.advancement.BiomeCompleteCriterion;
import net.busybee.ddv_fishing.advancement.SimpleTriggerCriterion;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Server-side advancement criteria for the Phase 3 fishing achievements. */
public class ModCriteria {
    public static final SimpleTriggerCriterion FIRST_CATCH = register("first_catch", new SimpleTriggerCriterion());
    public static final SimpleTriggerCriterion FIRST_PERFECT_CATCH = register("first_perfect_catch", new SimpleTriggerCriterion());
    public static final SimpleTriggerCriterion FIRST_EPIC_RIPPLE = register("first_epic_ripple", new SimpleTriggerCriterion());
    public static final SimpleTriggerCriterion FIRST_LEGENDARY_RIPPLE = register("first_legendary_ripple", new SimpleTriggerCriterion());
    public static final SimpleTriggerCriterion JOURNAL_COMPLETE = register("journal_complete", new SimpleTriggerCriterion());
    public static final BiomeCompleteCriterion BIOME_COMPLETE = register("biome_complete", new BiomeCompleteCriterion());

    private static <T extends Criterion<?>> T register(String path, T criterion) {
        return Registry.register(Registries.CRITERION, Identifier.of("ddv_fishing", path), criterion);
    }

    /** Forces the static registrations above to run - same empty-body pattern as {@link ModAttachments#register}. */
    public static void register() {
    }
}
