package net.busybee.ddv_fishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("DreamlightFishing");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("dreamlight_fishing.json").toFile();

    public boolean enabled = true;
    public float ripple_spawn_chance = 0.3f;
    public int search_radius = 32;
    public boolean spawn_in_flowing_water = false;
    public boolean luck_affects_rarity = true;
    public float minigame_difficulty_multiplier = 1.0f;

    /** Share of ripples that spawn green - the middling catch. */
    public float green_ripple_chance = 0.20f;
    /** Share of ripples that spawn orange - the hardest catch. The rest spawn blue. */
    public float orange_ripple_chance = 0.10f;

    /**
     * Share of ripples that spawn gold (Legendary) instead of blue, for players who have unlocked
     * it. Rolled separately from green/orange rather than folded into their pool - see
     * {@code RippleSpawner.rollRarity}.
     */
    public float legendary_ripple_chance = 0.02f;

    /** Extra size rolls taken (keeping the largest) on a Perfect Catch - higher values push perfect-catch sizes closer to the max. */
    public int perfect_catch_size_rolls = 2;

    public static ModConfig load() {
        // Logged because the file's location is otherwise guesswork - it sits in the instance's
        // config dir, which is not where people look first.
        LOGGER.info("Config: {}", CONFIG_FILE.getAbsolutePath());
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    // Write straight back so keys added in a new version appear in the file at
                    // their defaults. Without this an existing config silently hides new options.
                    config.save();
                    return config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load config", e);
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}