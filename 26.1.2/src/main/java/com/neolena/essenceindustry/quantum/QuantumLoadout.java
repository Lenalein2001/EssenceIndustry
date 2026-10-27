package com.neolena.essenceindustry.quantum;

import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;

/** Immutable item components own installed modules. A menu is only a view, never another inventory copy. */
public final class QuantumLoadout {
    public static final int COLUMNS=8, ROWS=6, SLOTS=COLUMNS*ROWS;
    private record Counts(int[] values, boolean valid) {}
    // Immutable contents are shared by item snapshots. Values never retain their weak keys.
    private static final Map<ItemContainerContents,Counts> COUNTS=Collections.synchronizedMap(new WeakHashMap<>());
    private static Counts counts(ItemContainerContents contents) {
        return COUNTS.computeIfAbsent(contents,c->{
            int[] values=new int[QuantumModule.Kind.values().length];boolean valid=c.getSlots()<=SLOTS;
            var iterator=c.nonEmptyItems().iterator();
            while(iterator.hasNext()) {
                var s=iterator.next();
                if(!(s.typeHolder().value() instanceof QuantumModule m) || s.count()!=1) {valid=false;continue;}
                if(++values[m.kind.ordinal()]>m.kind.limit)valid=false;
            }
            if(values[QuantumModule.Kind.SILK_TOUCH.ordinal()]>0 && values[QuantumModule.Kind.FORTUNE.ordinal()]>0)valid=false;
            return new Counts(values,valid);
        });
    }
    public static NonNullList<ItemStack> read(ItemInstance tool) {
        var result=NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        tool.getOrDefault(QuantumTools.MODULES.get(), ItemContainerContents.EMPTY).copyInto(result);
        return result;
    }
    public static boolean valid(ItemStack tool) {
        if (!(tool.getItem() instanceof QuantumToolItem item) || tool.getCount()!=1) return false;
        var contents=tool.getOrDefault(QuantumTools.MODULES.get(), ItemContainerContents.EMPTY);
        return contents.getSlots()<=SLOTS && valid(item.staff, read(tool), tool);
    }
    public static boolean valid(boolean staff, List<ItemStack> modules, ItemInstance tool) {
        if(modules.size()>SLOTS)return false;
        var counts=new EnumMap<QuantumModule.Kind,Integer>(QuantumModule.Kind.class);
        for (var stack: modules) {
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof QuantumModule m) || stack.getCount()!=1 || !m.kind.accepts(staff)) return false;
            if (counts.merge(m.kind,1,Integer::sum)>m.kind.limit) return false;
        }
        boolean silk=counts.containsKey(QuantumModule.Kind.SILK_TOUCH), fortune=counts.containsKey(QuantumModule.Kind.FORTUNE);
        if (silk && fortune) return false;
        return enchantCompatible(tool,silk,fortune);
    }
    private static boolean enchantCompatible(ItemInstance tool,boolean silk,boolean fortune) {
        // Respect existing enchantments without ever replacing their components.
        for (var e: tool.getTagEnchantments().keySet()) {
            if ((silk && e.is(Enchantments.FORTUNE)) || (fortune && e.is(Enchantments.SILK_TOUCH))) return false;
        }
        return true;
    }
    public static int count(ItemInstance tool, QuantumModule.Kind kind) {
        if (!(tool.typeHolder().value() instanceof QuantumToolItem item)) return 0;
        var contents=tool.getOrDefault(QuantumTools.MODULES.get(), ItemContainerContents.EMPTY);
        if (contents.getSlots()>SLOTS) return 0;
        var counts=counts(contents);if(!counts.valid)return 0;
        for(var k:QuantumModule.Kind.values())if(counts.values[k.ordinal()]>0 && !k.accepts(item.staff))return 0;
        if(!enchantCompatible(tool,counts.values[QuantumModule.Kind.SILK_TOUCH.ordinal()]>0,counts.values[QuantumModule.Kind.FORTUNE.ordinal()]>0))return 0;
        return counts.values[kind.ordinal()];
    }
    public static void write(ItemStack tool,List<ItemStack> modules) {
        if (!(tool.getItem() instanceof QuantumToolItem item) || modules.size()!=SLOTS || !valid(item.staff,modules,tool)) throw new IllegalArgumentException("Invalid quantum module layout");
        var contents=ItemContainerContents.fromItems(modules);
        if(contents.equals(ItemContainerContents.EMPTY))tool.remove(QuantumTools.MODULES.get());else tool.set(QuantumTools.MODULES.get(),contents);
    }
    private QuantumLoadout() {}
}
