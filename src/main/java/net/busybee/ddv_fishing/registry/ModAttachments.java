package net.busybee.ddv_fishing.registry;

import net.busybee.ddv_fishing.journal.FishJournalData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public class ModAttachments {
    public static final AttachmentType<FishJournalData> FISH_JOURNAL = AttachmentRegistry.create(
            Identifier.of("ddv_fishing", "fish_journal"),
            builder -> builder.persistent(FishJournalData.CODEC).initializer(FishJournalData::empty)
    );

    public static void register() {
    }
}
