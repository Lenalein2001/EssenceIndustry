package com.neolena.essenceindustry.quantum;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;

/** Vanilla combining recipes do not transfer two independent module inventories. */
public final class QuantumModuleSafety {
    public static boolean loaded(ItemStack s) {
        return s.getItem() instanceof QuantumToolItem && !s.getOrDefault(QuantumTools.MODULES.get(),ItemContainerContents.EMPTY).equals(ItemContainerContents.EMPTY);
    }
    public static void anvil(AnvilUpdateEvent e) {
        if(loaded(e.getRight())) {e.setCanceled(true);return;}
        if(!(e.getLeft().getItem() instanceof QuantumToolItem))return;
        var right=e.getRight().getOrDefault(DataComponents.STORED_ENCHANTMENTS,e.getRight().getTagEnchantments());
        for(var enchantment:right.keySet()) {
            if(enchantment.is(Enchantments.FORTUNE) && QuantumLoadout.count(e.getLeft(),QuantumModule.Kind.SILK_TOUCH)>0 || enchantment.is(Enchantments.SILK_TOUCH) && QuantumLoadout.count(e.getLeft(),QuantumModule.Kind.FORTUNE)>0) {e.setCanceled(true);return;}
        }
    }
    public static void grindstone(GrindstoneEvent.OnPlaceItem e) {
        if(loaded(e.getTopItem()) || loaded(e.getBottomItem()))e.setCanceled(true);
    }
    private QuantumModuleSafety() {}
}
