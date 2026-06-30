package org.oddlama.vane.core.item;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.oddlama.vane.core.Core;
import org.oddlama.vane.core.Listener;
import org.oddlama.vane.core.item.api.CustomItem;
import org.oddlama.vane.core.module.Context;

public class ExistingItemConverter extends Listener<Core> {
    public ExistingItemConverter(final Context<Core> context) {
        super(context.namespace("existing_item_converter"));
    }

    private Integer old_custom_model_data(final ItemStack item_stack) {
        if (!item_stack.hasItemMeta()) {
            return null;
        }

        final var modelDataList = item_stack.getItemMeta().getCustomModelDataComponent().getFloats();
        if (modelDataList.isEmpty() || modelDataList.getFirst() == null) {
            return null;
        }

        return modelDataList.getFirst().intValue();
    }

    private boolean is_legacy_ancient_tome_model_data(final int model_data) {
        return switch (model_data) {
            case 7823726,
                7823727,
                7823790,
                7823791,
                7823854,
                7823855,
                7798784,
                7798785,
                7798786,
                7798787,
                7798788,
                7798789 -> true;
            default -> false;
        };
    }

    private ItemStack migrate_legacy_ancient_tome(final ItemStack item_stack) {
        final var model_data = old_custom_model_data(item_stack);
        if (model_data == null || !is_legacy_ancient_tome_model_data(model_data)) {
            return null;
        }

        final var meta = item_stack.getItemMeta();
        final var stored_enchants = meta instanceof EnchantmentStorageMeta storage_meta
            ? storage_meta.getStoredEnchants()
            : Map.<org.bukkit.enchantments.Enchantment, Integer>of();
        final var replacement_type = stored_enchants.isEmpty() ? Material.BOOK : Material.ENCHANTED_BOOK;
        final var replacement = new ItemStack(replacement_type, item_stack.getAmount());
        if (!stored_enchants.isEmpty()) {
            replacement.editMeta(EnchantmentStorageMeta.class, replacement_meta ->
                stored_enchants.forEach((enchantment, level) -> replacement_meta.addStoredEnchant(enchantment, level, true))
            );
        }
        return replacement;
    }

    private CustomItem from_old_item(final ItemStack item_stack) {
        final var model_data = old_custom_model_data(item_stack);
        if (model_data == null) {
            return null;
        }

        // If lookups fail, we return null and nothing will be done.
        String new_item_key = switch (model_data) {
            case 7758190 -> "vane_trifles:wooden_sickle";
            case 7758191 -> "vane_trifles:stone_sickle";
            case 7758192 -> "vane_trifles:iron_sickle";
            case 7758193 -> "vane_trifles:golden_sickle";
            case 7758194 -> "vane_trifles:diamond_sickle";
            case 7758195 -> "vane_trifles:netherite_sickle";
            case 7758254,7758255,7758256,7758257,7758258,7758259 -> "vane_trifles:file";
            case 7758318 -> "vane_trifles:empty_xp_bottle";
            case 7758382 -> "vane_trifles:small_xp_bottle";
            case 7758383 -> "vane_trifles:medium_xp_bottle";
            case 7758384 -> "vane_trifles:large_xp_bottle";
            case 7758446 -> "vane_trifles:home_scroll";
            case 7758510 -> "vane_trifles:unstable_scroll";
            case 7758574 -> "vane_trifles:reinforced_elytra";
            default -> null;
        };

        if (new_item_key == null) {
            return null;
        }
        return get_module().item_registry().get(NamespacedKey.fromString(new_item_key));
    }

    private ItemStack process_item_stack(@NotNull final ItemStack is) {
        final var custom_item = get_module().item_registry().get(is);
        if (custom_item == null) {
            final var migrated_ancient_tome = migrate_legacy_ancient_tome(is);
            if (migrated_ancient_tome != null) {
                get_module().enchantment_manager.update_enchanted_item(migrated_ancient_tome);
                get_module().log.info("Converted legacy ancient tome to " + migrated_ancient_tome.getType().key());
                return migrated_ancient_tome;
            }

            // Determine if the item stack should be converted to a custom item from a legacy definition
            final var convert_to_custom_item = from_old_item(is);
            if (convert_to_custom_item != null) {
                final var converted = convert_to_custom_item.convertExistingStack(is);
                converted.editMeta(meta -> meta.itemName(convert_to_custom_item.displayName()));
                get_module().enchantment_manager.update_enchanted_item(converted);
                get_module().log.info("Converted legacy item to " + convert_to_custom_item.key());
                return converted;
            }

            if (is.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof Container container) {
                if (process_inventory(container.getInventory())) {
                    meta.setBlockState(container);
                    is.setItemMeta(meta);
                    return is;
                }
            }

            return null;
        }

        // Remove obsolete custom items
        if (get_module().item_registry().shouldRemove(custom_item.key())) {
            get_module().log.info("Removed obsolete item " + custom_item.key());
            return new ItemStack(Material.AIR);
        }

        // Update custom items to a new version, or if another detectable property changed.
        final var key_and_version = CustomItemHelper.customItemTagsFromItemStack(is);
        final var meta = is.getItemMeta();
        if (
            meta.getCustomModelData() != custom_item.customModelData() ||
            is.getType() != custom_item.baseMaterial() ||
            key_and_version.getRight() != custom_item.version()) {
            // Also includes durability max update.
            final var converted = custom_item.convertExistingStack(is);
            get_module().log.info("Updated item " + custom_item.key());
            return converted;
        }

        // Update maximum durability on existing items if changed.
        Damageable damageableMeta = (Damageable) is.getItemMeta();
        int max_damage = damageableMeta.hasMaxDamage()
            ? damageableMeta.getMaxDamage()
            : is.getType().getMaxDurability();
        int correct_max_damage = custom_item.durability() == 0 ? is.getType().getMaxDurability() : custom_item.durability();
        if (
            max_damage != correct_max_damage ||
            meta.getPersistentDataContainer().has(DurabilityManager.ITEM_DURABILITY_DAMAGE)) {
            get_module().log.info("Updated item durability " + custom_item.key());
            DurabilityManager.update_damage(custom_item, is);
            return is;
        }

        return null;
    }

    private boolean process_inventory(@NotNull Inventory inventory) {
        final var contents = inventory.getContents();
        int changed = 0;

        for (int i = 0; i < contents.length; ++i) {
            final var is = contents[i];
            if (is == null || !is.hasItemMeta()) {
                continue;
            }

            final var converted = process_item_stack(is);
            if (converted == null) {
                continue;
            }

            if (converted.getType() == Material.AIR) {
                contents[i] = null;
                ++changed;
                continue;
            }

            contents[i] = converted;
            ++changed;
        }

        if (changed > 0) {
            inventory.setContents(contents);
        }

        return changed > 0;
    }

    private void process_item_frame(@NotNull final ItemFrame item_frame) {
        final var item = item_frame.getItem();
        if (item.getType() == Material.AIR || !item.hasItemMeta()) {
            return;
        }

        final var converted = process_item_stack(item);
        if (converted != null) {
            item_frame.setItem(converted);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on_player_join(final PlayerJoinEvent event) {
        process_inventory(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on_inventory_open(final InventoryOpenEvent event) {
        // Catches enderchests, and inventories by other plugins
        process_inventory(event.getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on_chunk_load(final ChunkLoadEvent event) {
        final var chunk = event.getChunk();
        for (final var tile_entity : chunk.getTileEntities(state -> state instanceof Container, false)) {
            if (tile_entity instanceof Container container) {
                process_inventory(container.getInventory());
            }
        }

        for (final var entity : chunk.getEntities()) {
            if (entity instanceof ItemFrame item_frame) {
                process_item_frame(item_frame);
            }
        }
    }
}
