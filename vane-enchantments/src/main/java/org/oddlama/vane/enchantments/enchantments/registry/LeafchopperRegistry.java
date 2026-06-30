package org.oddlama.vane.enchantments.enchantments.registry;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.enchantments.Enchantment;
import org.oddlama.vane.enchantments.CustomEnchantmentRegistry;

public class LeafchopperRegistry extends CustomEnchantmentRegistry {

    public LeafchopperRegistry(RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder> composeEvent, int weight) {
        super("leafchopper", ItemTypeTagKeys.AXES, 1);
        this.weight(weight).cost(1, 5, 10, 5);
        this.register(composeEvent);
    }
}
