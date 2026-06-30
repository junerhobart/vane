package org.oddlama.vane.enchantments.enchantments.registry;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.enchantments.Enchantment;
import org.oddlama.vane.enchantments.CustomEnchantmentRegistry;

public class LightningRegistry extends CustomEnchantmentRegistry {

    public LightningRegistry(RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder> composeEvent, int weight) {
        super("lightning", ItemTypeTagKeys.SWORDS, 1);
        this.weight(weight).cost(15, 10, 25, 10);
        this.register(composeEvent);
    }
}
