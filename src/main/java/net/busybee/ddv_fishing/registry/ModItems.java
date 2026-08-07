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

    public static void register() {
        // Creative menu: sits next to the vanilla rod under Tools & Utilities.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(MAGICAL_FISHING_ROD));
    }
}
