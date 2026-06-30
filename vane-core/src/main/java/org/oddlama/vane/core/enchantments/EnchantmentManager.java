package org.oddlama.vane.core.enchantments;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.oddlama.vane.core.Core;
import org.oddlama.vane.core.Listener;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.util.ItemUtil;
import org.oddlama.vane.util.StorageUtil;

public class EnchantmentManager extends Listener<Core> {

    private static final NamespacedKey SENTINEL = StorageUtil.namespaced_key("vane", "enchantment_lore");

    public EnchantmentManager(Context<Core> context) {
        super(context);
    }

    public ItemStack update_enchanted_item(ItemStack item_stack) {
        return update_enchanted_item(item_stack, new HashMap<Enchantment, Integer>(), false);
    }

    public ItemStack update_enchanted_item(ItemStack item_stack, Map<Enchantment, Integer> additional_enchantments) {
        return update_enchanted_item(item_stack, additional_enchantments, false);
    }

    public ItemStack update_enchanted_item(ItemStack item_stack, boolean only_if_enchanted) {
        return update_enchanted_item(item_stack, new HashMap<Enchantment, Integer>(), only_if_enchanted);
    }

    public ItemStack update_enchanted_item(
        ItemStack item_stack,
        Map<Enchantment, Integer> additional_enchantments,
        boolean only_if_enchanted
    ) {
        if (only_if_enchanted && enchantments_on(item_stack).isEmpty() && additional_enchantments.isEmpty()) {
            return item_stack;
        }
        remove_old_lore(item_stack);
        return item_stack;
    }

    private Map<Enchantment, Integer> enchantments_on(ItemStack item_stack) {
        final var enchantments = new HashMap<Enchantment, Integer>(item_stack.getEnchantments());
        final var meta = item_stack.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storage_meta) {
            enchantments.putAll(storage_meta.getStoredEnchants());
        }
        return enchantments;
    }

    private CustomEnchantment<?> custom_enchantment(Enchantment enchantment) {
        return CustomEnchantment.instances()
            .stream()
            .filter(custom_enchantment -> custom_enchantment.key().equals(enchantment.getKey()))
            .findFirst()
            .orElse(null);
    }

    private boolean is_disabled_custom_enchantment(Enchantment enchantment) {
        final var custom_enchantment = custom_enchantment(enchantment);
        return custom_enchantment != null && !custom_enchantment.allows_anvil();
    }

    private boolean is_blocked_table_enchantment(Enchantment enchantment) {
        final var custom_enchantment = custom_enchantment(enchantment);
        return custom_enchantment != null && !custom_enchantment.allows_enchanting_table();
    }

    private boolean is_blocked_trade_enchantment(Enchantment enchantment) {
        final var custom_enchantment = custom_enchantment(enchantment);
        return custom_enchantment != null && !custom_enchantment.allows_villager_trades();
    }

    private boolean has_blocked_trade_enchantment(ItemStack item_stack) {
        return enchantments_on(item_stack).keySet().stream().anyMatch(this::is_blocked_trade_enchantment);
    }

    private void remove_superseded(ItemStack item_stack, Map<Enchantment, Integer> enchantments) {
        // if (enchantments.isEmpty()) {
        // 	return;
        // }

        // 1. Build a list of all enchantments that would be removed because
        //    they are superseded by some enchantment.
        // final var to_remove_inclusive = enchantments.keySet().stream()
        // 	.map(x -> ((CraftEnchantment)x).getHandle())
        // 	.filter(x -> x instanceof NativeEnchantmentWrapper)
        // 	.map(x -> ((NativeEnchantmentWrapper)x).custom().supersedes())
        // 	.flatMap(Set::stream)
        // 	.collect(Collectors.toSet());

        // 2. Before removing these enchantments, first re-build the list but
        //    ignore any enchantments in the calculation that would themselves
        //    be removed. This prevents them from contributing to the list of
        //    enchantments to remove. Consider this: A supersedes B, and B supersedes C, but
        //    A doesn't supersede C. Now an item with A B and C should get reduced to
        //    A and C, not just to A.
        // var to_remove = enchantments.keySet().stream()
        // 	.map(x -> ((CraftEnchantment)x).getHandle())
        // 	.filter(x -> x instanceof NativeEnchantmentWrapper)
        // 	.filter(x ->
        // !to_remove_inclusive.contains(((NativeEnchantmentWrapper)x).custom().key())) // Ignore
        // enchantments that are themselves removed.
        // 	.map(x -> ((NativeEnchantmentWrapper)x).custom().supersedes())
        // 	.flatMap(Set::stream)
        // 	.map(x -> org.bukkit.Registry.ENCHANTMENT.get(x))
        // 	.collect(Collectors.toSet());

        // for (var e : to_remove) {
        // 	item_stack.removeEnchantment(e);
        // 	enchantments.remove(e);
        // }
    }

    private void remove_old_lore(ItemStack item_stack) {
        var lore = item_stack.lore();
        if (lore == null) {
            lore = new ArrayList<Component>();
        }

        lore.removeIf(this::is_enchantment_lore);

        // Set lore
        item_stack.lore(lore.isEmpty() ? null : lore);
    }

    private boolean is_enchantment_lore(final Component component) {
        // FIXME legacy If the component begins with a translated lore from vane enchantments, it is
        // always from us. (needed for backward compatibility)
        if (
            component instanceof TranslatableComponent translatable_component &&
            translatable_component.key().startsWith("vane_enchantments.")
        ) {
            return true;
        }

        return ItemUtil.has_sentinel(component, SENTINEL);
    }

    // Triggers on Anvils, grindstones, and smithing tables.
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on_prepare_enchanted_edit(final PrepareResultEvent event) {
        if (event.getResult() == null) {
            return;
        }

        event.setResult(update_enchanted_item(event.getResult().clone()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on_enchant_item(final EnchantItemEvent event) {
        event.getEnchantsToAdd().keySet().removeIf(this::is_blocked_table_enchantment);
        update_enchanted_item(event.getItem(), event.getEnchantsToAdd());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on_prepare_item_enchant(final PrepareItemEnchantEvent event) {
        final var offers = event.getOffers();
        for (int i = 0; i < offers.length; ++i) {
            if (offers[i] != null && is_blocked_table_enchantment(offers[i].getEnchantment())) {
                offers[i] = null;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on_prepare_anvil(final PrepareAnvilEvent event) {
        final var result = event.getResult();
        if (result == null) {
            return;
        }

        if (enchantments_on(result).keySet().stream().anyMatch(this::is_disabled_custom_enchantment)) {
            event.setResult(null);
        }
    }

    private MerchantRecipe process_recipe(final MerchantRecipe recipe) {
        var result = recipe.getResult().clone();

        // Create a new recipe
        final var new_recipe = new MerchantRecipe(
            update_enchanted_item(result, true),
            recipe.getUses(),
            recipe.getMaxUses(),
            recipe.hasExperienceReward(),
            recipe.getVillagerExperience(),
            recipe.getPriceMultiplier()
        );
        recipe.getIngredients().forEach(i -> new_recipe.addIngredient(i));
        return new_recipe;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on_acquire_trade(final VillagerAcquireTradeEvent event) {
        if (has_blocked_trade_enchantment(event.getRecipe().getResult())) {
            event.setCancelled(true);
            return;
        }

        event.setRecipe(process_recipe(event.getRecipe()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on_right_click_villager(final PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Merchant merchant)) {
            return;
        }

        final var recipes = new ArrayList<MerchantRecipe>();
        for (final var recipe : merchant.getRecipes()) {
            if (!has_blocked_trade_enchantment(recipe.getResult())) {
                recipes.add(process_recipe(recipe));
            }
        }
        merchant.setRecipes(recipes);
    }
}
