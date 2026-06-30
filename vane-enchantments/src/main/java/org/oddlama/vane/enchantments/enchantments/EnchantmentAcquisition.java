package org.oddlama.vane.enchantments.enchantments;

import org.oddlama.vane.core.enchantments.CustomEnchantment.AcquisitionSettings;
import org.oddlama.vane.enchantments.EnchantmentDefinitions;

public final class EnchantmentAcquisition {

    private EnchantmentAcquisition() {}

    public static AcquisitionSettings settings(final EnchantmentDefinitions.Definition definition) {
        final var acquisition = definition.acquisition();
        return new AcquisitionSettings(
            acquisition.enchanting_table(),
            acquisition.villager_trades(),
            acquisition.structure_loot(),
            acquisition.fishing_loot(),
            acquisition.weight()
        );
    }
}
