package net.busybee.ddv_fishing.registry;

import com.mojang.serialization.Codec;
import net.busybee.ddv_fishing.journal.FishJournalData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public class ModAttachments {
    public static final AttachmentType<FishJournalData> FISH_JOURNAL = AttachmentRegistry.create(
            Identifier.of("ddv_fishing", "fish_journal"),
            builder -> builder.persistent(FishJournalData.CODEC).initializer(FishJournalData::empty)
    );

    /**
     * Bitmask of journal-completion milestone rewards already granted (bit 0 = 25%, bit 1 = 50%,
     * bit 2 = 75%, bit 3 = 100%/Legendary capstone) - see {@code FishJournalManager}. Kept separate
     * from {@link #FISH_JOURNAL} itself so the reward-granting logic never has to touch the journal
     * data's own codec/network shape.
     */
    public static final AttachmentType<Integer> MILESTONE_REWARDS = AttachmentRegistry.create(
            Identifier.of("ddv_fishing", "milestone_rewards"),
            builder -> builder.persistent(Codec.INT).initializer(() -> 0)
    );

    /**
     * Permanent per-player bump to green/orange ripple chance, granted once at the 50% "Seasoned
     * Angler" milestone. Additive on top of the config values in {@code RippleSpawner.rollRarity}.
     */
    public static final AttachmentType<Float> RIPPLE_BONUS = AttachmentRegistry.create(
            Identifier.of("ddv_fishing", "ripple_bonus"),
            builder -> builder.persistent(Codec.FLOAT).initializer(() -> 0f)
    );

    /**
     * One-shot flag set by Lucky Bait: forces the next catch to resolve as a Perfect Catch, then
     * clears itself. Persistent rather than a plain server-side field so it survives a relog
     * between using the bait and actually landing the next fish.
     */
    public static final AttachmentType<Boolean> GUARANTEED_PERFECT = AttachmentRegistry.create(
            Identifier.of("ddv_fishing", "guaranteed_perfect"),
            builder -> builder.persistent(Codec.BOOL).initializer(() -> false)
    );

    public static void register() {
    }
}
