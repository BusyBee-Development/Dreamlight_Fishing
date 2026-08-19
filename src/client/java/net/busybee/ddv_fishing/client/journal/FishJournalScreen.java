package net.busybee.ddv_fishing.client.journal;

import net.busybee.ddv_fishing.journal.FishJournalEntry;
import net.busybee.ddv_fishing.journal.FishSpecies;
import net.busybee.ddv_fishing.world.FishBiome;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Read-only journal display: no {@code ScreenHandler}, nothing here mutates server state. Built
 * entirely from vanilla widgets and flat-color panels rather than a custom background texture -
 * there is no art budget for a bespoke journal texture right now.
 */
public class FishJournalScreen extends Screen {
    private static final FishBiome[] TABS = {FishBiome.OTHER, FishBiome.OCEAN, FishBiome.SWAMP, FishBiome.JUNGLE};
    private static final int ROW_HEIGHT = 22;
    private static final int PANEL_WIDTH = 300;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    private @Nullable FishBiome filter = null;
    private int panelX;
    private int panelY;
    private int panelHeight;

    public FishJournalScreen() {
        super(Text.translatable("screen.ddv_fishing.journal.title"));
    }

    @Override
    protected void init() {
        int rowCount = filter == null ? FishSpecies.values().length : countInBiome(filter);
        panelHeight = 50 + rowCount * ROW_HEIGHT + 30;
        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = Math.max(10, (this.height - panelHeight) / 2);

        Map<FishBiome, int[]> counts = FishJournalClientState.data().biomeCounts();
        int tabWidth = PANEL_WIDTH / TABS.length;
        int tabY = panelY + 22;
        for (int i = 0; i < TABS.length; i++) {
            FishBiome biome = TABS[i];
            int[] count = counts.get(biome);
            MutableText label = Text.translatable(biomeKey(biome))
                    .append(Text.literal(" " + count[0] + "/" + count[1]));
            boolean selected = filter == biome;
            this.addDrawableChild(ButtonWidget.builder(selected ? label.formatted(Formatting.YELLOW) : label,
                            btn -> {
                                this.filter = selected ? null : biome;
                                this.clearAndInit();
                            })
                    .dimensions(panelX + i * tabWidth, tabY, tabWidth, 18)
                    .build());
        }

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, btn -> this.close())
                .dimensions(panelX + PANEL_WIDTH / 2 - 50, panelY + panelHeight - 26, 100, 20)
                .build());
    }

    private static int countInBiome(FishBiome biome) {
        int count = 0;
        for (FishSpecies species : FishSpecies.values()) {
            if (species.biome() == biome) count++;
        }
        return count;
    }

    private static String biomeKey(FishBiome biome) {
        return switch (biome) {
            case OCEAN -> "screen.ddv_fishing.journal.biome.ocean";
            case SWAMP -> "screen.ddv_fishing.journal.biome.swamp";
            case JUNGLE -> "screen.ddv_fishing.journal.biome.jungle";
            case OTHER -> "screen.ddv_fishing.journal.biome.other";
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, panelX + PANEL_WIDTH / 2, panelY + 6, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);

        int rowY = panelY + 48;
        for (FishSpecies species : FishSpecies.values()) {
            if (filter != null && species.biome() != filter) continue;

            FishJournalEntry entry = FishJournalClientState.data().get(species);
            context.drawItem(new ItemStack(species.item()), panelX + 8, rowY);

            if (entry.caught()) {
                context.drawText(this.textRenderer, species.item().getName(), panelX + 30, rowY, 0xFFFFFFFF, false);

                String firstCaught = entry.firstCaughtEpochMillis() >= 0
                        ? DATE_FORMAT.format(Instant.ofEpochMilli(entry.firstCaughtEpochMillis()))
                        : "-";
                MutableText detail = Text.translatable("screen.ddv_fishing.journal.largest", Math.round(entry.largestSize()))
                        .append(Text.literal("  "))
                        .append(Text.translatable("screen.ddv_fishing.journal.times_caught", entry.timesCaught()))
                        .append(Text.literal("  "))
                        .append(Text.translatable("screen.ddv_fishing.journal.first_caught", firstCaught));
                context.drawText(this.textRenderer, detail, panelX + 30, rowY + 10, 0xFFAAAAAA, false);
            } else {
                context.drawText(this.textRenderer, species.item().getName(), panelX + 30, rowY, 0xFF777777, false);
                context.drawText(this.textRenderer, Text.translatable("screen.ddv_fishing.journal.not_caught"),
                        panelX + 30, rowY + 10, 0xFF555555, false);
            }

            rowY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
