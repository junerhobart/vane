package org.oddlama.vane.enchantments.enchantments;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.loot.LootTables;
import org.oddlama.vane.annotation.enchantment.Rarity;
import org.oddlama.vane.annotation.enchantment.VaneEnchantment;
import org.oddlama.vane.core.config.loot.LootDefinition;
import org.oddlama.vane.core.config.loot.LootTableList;
import org.oddlama.vane.core.enchantments.CustomEnchantment;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.enchantments.EnchantmentDefinitions;
import org.oddlama.vane.enchantments.Enchantments;

@VaneEnchantment(name = "unbreakable", rarity = Rarity.RARE, treasure = true, allow_custom = true)
public class Unbreakable extends CustomEnchantment<Enchantments> {

    public Unbreakable(Context<Enchantments> context) {
        super(context, EnchantmentAcquisition.settings(EnchantmentDefinitions.UNBREAKABLE));
    }

    @Override
    public LootTableList default_loot_tables() {
        return LootTableList.of(
            new LootDefinition("generic")
                .in(LootTables.ABANDONED_MINESHAFT)
                .add(1.0 / 120, 1, 1, book()),
            new LootDefinition("bastion")
                .in(LootTables.BASTION_TREASURE)
                .add(1.0 / 30, 1, 1, book())
        );
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void on_player_item_damage(final PlayerItemDamageEvent event) {
        // Check enchantment
        final var item = event.getItem();
        if (item.getEnchantmentLevel(this.bukkit()) == 0) {
            return;
        }

        // Set item unbreakable to prevent further event calls
        final var meta = item.getItemMeta();
        meta.setUnbreakable(true);
        // Also hide the internal unbreakable tag on the client
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);

        // Prevent damage
        event.setDamage(0);
        event.setCancelled(true);
    }
}
