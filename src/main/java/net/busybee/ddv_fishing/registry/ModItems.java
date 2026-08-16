package net.busybee.ddv_fishing.registry;

import net.busybee.ddv_fishing.item.MagicalFishingRodItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Identifier MAGICAL_FISHING_ROD_ID = Identifier.of("ddv_fishing", "magical_fishing_rod");
    public static final RegistryKey<Item> MAGICAL_FISHING_ROD_KEY = RegistryKey.of(RegistryKeys.ITEM, MAGICAL_FISHING_ROD_ID);

    public static final Item MAGICAL_FISHING_ROD = Registry.register(
            Registries.ITEM,
            MAGICAL_FISHING_ROD_KEY,
            new MagicalFishingRodItem(new Item.Settings()
                    .registryKey(MAGICAL_FISHING_ROD_KEY)
                    .maxDamage(64))
    );

    // Biome-tier fishing loot. Placeholder art (flat colour icons) - real textures come later, per
    // the Phase 2 roadmap; the items and loot wiring don't need to wait on them.
    public static final Item OCEAN_PEARL = register("ocean_pearl");
    public static final Item LARGE_FISH = register("large_fish");
    public static final Item ALGAE = register("algae");
    public static final Item CATFISH = register("catfish");
    public static final Item EXOTIC_FISH = register("exotic_fish");
    public static final Item RIVER_PIRANHA = register("river_piranha");

    private static Item register(String path) {
        Identifier id = Identifier.of("ddv_fishing", path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key, new Item(new Item.Settings().registryKey(key)));
    }

    public static void register() {
        // Creative menu: sits next to the vanilla rod under Tools & Utilities.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(MAGICAL_FISHING_ROD));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(entries -> {
                    entries.add(OCEAN_PEARL);
                    entries.add(LARGE_FISH);
                    entries.add(ALGAE);
                    entries.add(CATFISH);
                    entries.add(EXOTIC_FISH);
                    entries.add(RIVER_PIRANHA);
                });
    }
}
