package com.neolena.essenceindustry.library;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Read-only display/ranking data. Never changes the stored seed or its components. */
public record SeedTraits(int growth, int yield, int resistance, int mutability, boolean known) {
    public long production() { return (long)growth + yield; }
    public static SeedTraits read(ItemStack stack) {
        if (ModList.get().isLoaded("productivefarming")) return ProductiveSeedTraits.read(stack);
        int growth = 0, yield = 0, resistance = 0, mutability = 0; boolean known = false;
        for (var component : stack.getComponents()) {
            var id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
            if (id == null || id.getNamespace().equals("minecraft") || !(component.value() instanceof Number n)) continue;
            int value = n.intValue();
            switch (id.getPath()) {
                case "growth" -> { growth = value; known = true; }
                case "yield", "gain" -> { yield = value; known = true; }
                case "resistance", "strength" -> { resistance = value; known = true; }
                case "mutability" -> { mutability = value; known = true; }
            }
        }
        return new SeedTraits(growth, yield, resistance, mutability, known);
    }
}
