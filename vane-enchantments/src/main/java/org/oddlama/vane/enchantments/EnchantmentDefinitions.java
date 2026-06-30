package org.oddlama.vane.enchantments;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import java.util.List;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;

public final class EnchantmentDefinitions {

    private static final List<TagKey<Enchantment>> COMMON_TRADE_TAGS = List.of(
        EnchantmentTagKeys.TRADES_PLAINS_COMMON,
        EnchantmentTagKeys.TRADES_TAIGA_COMMON
    );

    public static final Definition ANGEL = new Definition(
        "angel",
        true,
        Acquisition.treasure(),
        List.of()
    );
    public static final Definition GRAPPLING_HOOK = new Definition(
        "grappling_hook",
        true,
        Acquisition.uncommon().withFishingLoot(),
        COMMON_TRADE_TAGS
    );
    public static final Definition HELL_BENT = new Definition(
        "hell_bent",
        true,
        Acquisition.common().withStructureLoot(),
        COMMON_TRADE_TAGS
    );
    public static final Definition LEAFCHOPPER = new Definition(
        "leafchopper",
        true,
        Acquisition.common(),
        COMMON_TRADE_TAGS
    );
    public static final Definition LIGHTNING = new Definition(
        "lightning",
        false,
        Acquisition.treasure(),
        List.of()
    );
    public static final Definition RAKE = new Definition(
        "rake",
        true,
        Acquisition.common(),
        COMMON_TRADE_TAGS
    );
    public static final Definition SEEDING = new Definition(
        "seeding",
        true,
        Acquisition.common(),
        COMMON_TRADE_TAGS
    );
    public static final Definition SOULBOUND = new Definition(
        "soulbound",
        true,
        Acquisition.treasure(),
        List.of()
    );
    public static final Definition TAKE_OFF = new Definition(
        "take_off",
        true,
        Acquisition.uncommon().withStructureLoot(),
        COMMON_TRADE_TAGS
    );
    public static final Definition UNBREAKABLE = new Definition(
        "unbreakable",
        true,
        Acquisition.treasure(),
        List.of()
    );
    public static final Definition WINGS = new Definition(
        "wings",
        true,
        Acquisition.rare(),
        COMMON_TRADE_TAGS
    );

    private static final Map<String, Definition> BY_KEY = Map.ofEntries(
        Map.entry(ANGEL.key(), ANGEL),
        Map.entry(GRAPPLING_HOOK.key(), GRAPPLING_HOOK),
        Map.entry(HELL_BENT.key(), HELL_BENT),
        Map.entry(LEAFCHOPPER.key(), LEAFCHOPPER),
        Map.entry(LIGHTNING.key(), LIGHTNING),
        Map.entry(RAKE.key(), RAKE),
        Map.entry(SEEDING.key(), SEEDING),
        Map.entry(SOULBOUND.key(), SOULBOUND),
        Map.entry(TAKE_OFF.key(), TAKE_OFF),
        Map.entry(UNBREAKABLE.key(), UNBREAKABLE),
        Map.entry(WINGS.key(), WINGS)
    );

    private EnchantmentDefinitions() {}

    public static Map<String, Definition> byKey() {
        return BY_KEY;
    }

    public record Definition(
        String key,
        boolean defaultEnabled,
        Acquisition acquisition,
        List<TagKey<Enchantment>> tradeTags
    ) {
        public int weight() {
            return acquisition.weight();
        }

        public boolean treasure() {
            return !acquisition.enchanting_table() && !acquisition.villager_trades();
        }
    }

    public record Acquisition(
        boolean enchanting_table,
        boolean villager_trades,
        boolean structure_loot,
        boolean fishing_loot,
        int weight
    ) {
        public static Acquisition common() {
            return new Acquisition(true, true, false, false, 10);
        }

        public static Acquisition uncommon() {
            return new Acquisition(true, true, false, false, 5);
        }

        public static Acquisition rare() {
            return new Acquisition(true, true, true, false, 2);
        }

        public static Acquisition treasure() {
            return new Acquisition(false, false, true, false, 1);
        }

        public Acquisition withFishingLoot() {
            return new Acquisition(enchanting_table, villager_trades, structure_loot, true, weight);
        }

        public Acquisition withStructureLoot() {
            return new Acquisition(enchanting_table, villager_trades, true, fishing_loot, weight);
        }
    }
}
