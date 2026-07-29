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

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) return config;
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