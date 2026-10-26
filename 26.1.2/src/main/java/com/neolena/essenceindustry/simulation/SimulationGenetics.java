package com.neolena.essenceindustry.simulation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
/** Lazy optional bridge: offspring retain the parent's other components and never replace it. */
public final class SimulationGenetics {
 public static ItemStack offspring(ItemStack parent,BlockState crop,RandomSource random){return net.neoforged.fml.ModList.get().isLoaded("productivefarming")?Productive.offspring(parent,crop,random):parent;}
 private static final class Productive {
  static ItemStack offspring(ItemStack parent,BlockState crop,RandomSource random){
   if(!cy.jdkdigital.productivefarming.util.ExternalCropStats.isEnabled() || !cy.jdkdigital.productivefarming.util.ExternalCropStats.isEligible(crop))return parent;
   var before=cy.jdkdigital.productivefarming.util.ExternalCropStats.fromSeedStack(parent);
   var stat=cy.jdkdigital.productivefarming.util.TraitsHelper.rollIncreasedStat(random,parent.typeHolder());if(stat==null)return parent;
   int current=switch(stat){case "growth"->before.growth();case "yield"->before.yield();case "resistance"->before.resistance();default->before.mutability();};
   if(current>=cy.jdkdigital.productivefarming.util.TraitsHelper.getMaxValue(stat))return parent;
   var after=before.increase(stat);if(after.equals(before))return parent;
   return cy.jdkdigital.productivefarming.util.TraitsHelper.applyTraits(parent.copyWithCount(1),after.growth(),after.yield(),after.resistance(),after.mutability());
  }
 }
 private SimulationGenetics(){}
}
