package org.oddlama.vane.enchantments.enchantments.registry;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.enchantments.Enchantment;
import org.oddlama.vane.enchantments.CustomEnchantmentRegistry;

public class SouldboundRegistry extends CustomEnchantmentRegistry {

    public SouldboundRegistry(RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder> composeEvent, int weight) {
        super("soulbound", ItemTypeTagKeys.ENCHANTABLE_DURABILITY, 1);
        this.weight(weight).cost(25, 10, 40, 10);
        this.register(composeEvent);
    }
}
