package com.neolena.essenceindustry.array;

import appeng.api.stacks.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.EntityBlock;
import net.pedroksl.advanced_ae.common.entities.ReactionChamberEntity;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipe;

/** Native reaction recipes, including their exact fluid inputs and outputs. */
final class ArrayAdvancedRecipes {
 private static final Map<String,Boolean> MACHINES=new ConcurrentHashMap<>();
 private static boolean discover(String id){
  var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
  return item instanceof BlockItem bi&&bi.getBlock() instanceof EntityBlock entity&&entity.newBlockEntity(BlockPos.ZERO,bi.getBlock().defaultBlockState()) instanceof ReactionChamberEntity;
 }
 static boolean machine(ItemStack item){return MACHINES.computeIfAbsent(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),ArrayAdvancedRecipes::discover);}
 static void add(String id,Recipe<?> recipe,Set<String> installed,List<ArrayRecipes.Native> into){
  if(!(recipe instanceof ReactionChamberRecipe r))return;
  var needs=new ArrayList<ArrayRecipes.Demand>();
  for(var input:r.getInputs())if(!input.isEmpty()){var sample=input.sample();needs.add(new ArrayRecipes.Demand(k->k instanceof AEItemKey a&&sample.getIngredient().test(a.toStack()),sample.getAmount()));}
  var fluid=r.getFluid();if(!fluid.isEmpty()){var sample=fluid.sample();needs.add(new ArrayRecipes.Demand(k->k instanceof AEFluidKey f&&sample.getIngredient().test(f.toStack(1)),sample.getAmount()));}
  if(needs.isEmpty()||needs.size()>32||r.getEnergy()<0)return;
  var outputs=new ArrayList<GenericStack>();
  // Native completion chooses item OR fluid output, not both.
  if(r.isItemOutput())outputs.addAll(ArrayRecipes.output(r.getResultItem()));
  else{var output=r.getResultFluid();if(!output.isEmpty())outputs.add(new GenericStack(AEFluidKey.of(output),output.getAmount()));}
  if(outputs.isEmpty())return;
  // Unupgraded chamber advances two of its 200 processing steps each tick.
  long power=Math.max(1,(long)Math.ceil(appeng.api.config.PowerUnit.AE.convertTo(appeng.api.config.PowerUnit.FE,r.getEnergy())/100.0));
  for(String machine:installed)if(MACHINES.computeIfAbsent(machine,ArrayAdvancedRecipes::discover))into.add(new ArrayRecipes.Native(id,machine,100,power,List.copyOf(needs),List.copyOf(outputs),List.of()));
 }
 private ArrayAdvancedRecipes(){}
}
