package com.neolena.essenceindustry.array;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One replaceable tiered module per bank. Effects use integer arithmetic, without per-lane updates. */
public final class ArrayUpgrades {
 public static final int BASE_CAPACITY=50_000_000,MAX_CAPACITY=800_000_000,MAX_SPEED=64;
 public enum Kind {
  SPEED("speed"),EFFICIENCY("efficiency"),CAPACITY("capacity");
  public final String id;Kind(String id){this.id=id;}
 }
 private static final List<DeferredHolder<Item,ArrayUpgradeItem>> ITEMS=new ArrayList<>();
 static void register(DeferredRegister.Items items){for(var kind:Kind.values())for(int tier=1;tier<=(kind==Kind.SPEED?6:4);tier++){int t=tier;ITEMS.add(items.registerItem("array_"+kind.id+"_upgrade_"+tier,p->new ArrayUpgradeItem(p,kind,t)));}}
 static List<DeferredHolder<Item,ArrayUpgradeItem>> items(){return List.copyOf(ITEMS);}
 public static boolean valid(int slot,ItemStack stack){return slot>=0&&slot<3&&stack.getItem() instanceof ArrayUpgradeItem module&&module.kind.ordinal()==slot&&module.tier>=1&&module.tier<=(module.kind==Kind.SPEED?6:4)&&ArrayRecipes.plain(stack);}
 public static int tier(int slot,ItemStack stack){return stack.getCount()==1&&valid(slot,stack)?((ArrayUpgradeItem)stack.getItem()).tier:0;}
 public static long cost(long base,int efficiencyTier){
  if(base<0||efficiencyTier<0||efficiencyTier>4)throw new IllegalArgumentException("Invalid array energy profile");
  return base==0?0:Math.max(1,Math.addExact(Math.multiplyExact(base,100-15L*efficiencyTier),199)/200);
 }
 private ArrayUpgrades(){}
}
