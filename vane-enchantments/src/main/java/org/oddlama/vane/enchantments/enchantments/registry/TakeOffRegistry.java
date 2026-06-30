package org.oddlama.vane.enchantments.enchantments.registry;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import java.util.List;
import org.bukkit.enchantments.Enchantment;
import org.oddlama.vane.enchantments.CustomEnchantmentRegistry;

public class TakeOffRegistry extends CustomEnchantmentRegistry {

    public TakeOffRegistry(RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder> composeEvent, int weight) {
        super("take_off", List.of(ItemTypeKeys.ELYTRA), 3);
        this.weight(weight).cost(5, 8, 15, 8);
        this.register(composeEvent);
    }
}
