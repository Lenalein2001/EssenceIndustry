package com.neolena.essenceindustry.library;

import cy.jdkdigital.productivefarming.util.ExternalCropStats;
import net.minecraft.world.item.ItemStack;

/** Loaded only when Productive Farming is installed; includes defaults from its data maps. */
final class ProductiveSeedTraits {
    static SeedTraits read(ItemStack stack) {
        var traits = ExternalCropStats.fromSeedStack(stack);
        return new SeedTraits(traits.growth(), traits.yield(), traits.resistance(), traits.mutability(), true);
    }
}
