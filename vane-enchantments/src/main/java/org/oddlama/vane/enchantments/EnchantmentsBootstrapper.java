package org.oddlama.vane.enchantments;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.oddlama.vane.enchantments.enchantments.registry.AngelRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.GrapplingHookRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.HellBentRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.LeafchopperRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.LightningRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.RakeRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.SeedingRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.SouldboundRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.TakeOffRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.UnbreakableRegistry;
import org.oddlama.vane.enchantments.enchantments.registry.WingsRegistry;

public class EnchantmentsBootstrapper implements PluginBootstrap {

    private static final String NAMESPACE = "vane_enchantments";
    private static final Map<String, EnchantmentDefinitions.Definition> ENCHANTMENTS = EnchantmentDefinitions.byKey();

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        final var config = YamlConfiguration.loadConfiguration(context.getDataDirectory().resolve("config.yml").toFile());
        context
            .getLifecycleManager()
            .registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose()
                    .newHandler(event -> {
                        new AngelRegistry(event, weight(config, "angel"));
                        new GrapplingHookRegistry(event, weight(config, "grappling_hook"));
                        new HellBentRegistry(event, weight(config, "hell_bent"));
                        new LeafchopperRegistry(event, weight(config, "leafchopper"));
                        new LightningRegistry(event, weight(config, "lightning"));
                        new RakeRegistry(event, weight(config, "rake"));
                        new SeedingRegistry(event, weight(config, "seeding"));
                        new WingsRegistry(event, weight(config, "wings"));
                        new SouldboundRegistry(event, weight(config, "soulbound"));
                        new TakeOffRegistry(event, weight(config, "take_off"));
                        new UnbreakableRegistry(event, weight(config, "unbreakable"));
                    })
            );

        context.getLifecycleManager()
            .registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT),
                event -> {
                    for (final var entry : ENCHANTMENTS.entrySet()) {
                        final var name = entry.getKey();
                        final var definition = entry.getValue();
                        if (!enabled(config, name, definition)) {
                            continue;
                        }

                        final var key = typed_key(name);
                        if (enchanting_table(config, name, definition)) {
                            event.registrar().addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, Set.of(key));
                        }

                        if (treasure(config, name, definition)) {
                            event.registrar().addToTag(EnchantmentTagKeys.TREASURE, Set.of(key));
                        } else {
                            event.registrar().addToTag(EnchantmentTagKeys.NON_TREASURE, Set.of(key));
                        }

                        if (villager_trades(config, name, definition)) {
                            event.registrar().addToTag(EnchantmentTagKeys.TRADEABLE, Set.of(key));
                            definition.tradeTags().forEach(tag -> event.registrar().addToTag(tag, Set.of(key)));
                        }
                    }
                }
            );
    }

    private static TypedKey<Enchantment> typed_key(String name) {
        return TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(NAMESPACE, name));
    }

    private static int weight(YamlConfiguration config, String name) {
        final var definition = ENCHANTMENTS.get(name);
        final var weight = config.getInt(path(name, "weight"), definition.weight());
        return Math.max(1, Math.min(1024, weight));
    }

    private static boolean enabled(YamlConfiguration config, String name, EnchantmentDefinitions.Definition definition) {
        return config.getBoolean(path(name, "enabled"), definition.defaultEnabled());
    }

    private static boolean enchanting_table(YamlConfiguration config, String name, EnchantmentDefinitions.Definition definition) {
        return config.getBoolean(path(name, "enchanting_table"), definition.acquisition().enchanting_table());
    }

    private static boolean villager_trades(YamlConfiguration config, String name, EnchantmentDefinitions.Definition definition) {
        return config.getBoolean(path(name, "villager_trades"), definition.acquisition().villager_trades());
    }

    private static boolean treasure(YamlConfiguration config, String name, EnchantmentDefinitions.Definition definition) {
        return !enchanting_table(config, name, definition) && !villager_trades(config, name, definition);
    }

    private static String path(String name, String key) {
        return "enchantment_" + name + "." + key;
    }
}
