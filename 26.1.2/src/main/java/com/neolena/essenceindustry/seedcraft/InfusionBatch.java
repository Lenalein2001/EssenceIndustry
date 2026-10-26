package com.neolena.essenceindustry.seedcraft;

import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import java.util.*;

/** Capacity-aware bipartite matching: at most 9 ingredients and 81 unit tokens. */
public record InfusionBatch(IInfusionRecipe recipe, CraftingInput input, int[] consumed) {
 public static InfusionBatch match(IInfusionRecipe recipe, List<ItemStack> stacks, Level level) {
  var ingredients=new ArrayList<Ingredient>();ingredients.add(recipe.getAltarIngredient());ingredients.addAll(recipe.getPedestalIngredients());
  if(ingredients.size()>9 || ingredients.size()<2)return null;
  boolean[][] edges=new boolean[ingredients.size()][9];int[] capacities=new int[9];
  for(int slot=0;slot<9;slot++){
   var stack=stacks.get(slot);capacities[slot]=Math.min(9,stack.getCount());
   if(!stack.isEmpty())for(int i=0;i<ingredients.size();i++)edges[i][slot]=ingredients.get(i).test(stack);
  }
  int[] owners=new int[81];Arrays.fill(owners,-1);
  for(int i=0;i<ingredients.size();i++)if(!augment(i,edges,capacities,owners,new boolean[81]))return null;
  var units=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));int[] consumed=new int[9];
  for(int t=0;t<81;t++)if(owners[t]>=0){int slot=t/9;consumed[slot]++;units.set(owners[t],stacks.get(slot).copyWithCount(1));}
  var input=CraftingInput.of(3,3,units);
  return recipe.matches(input,level)?new InfusionBatch(recipe,input,consumed):null;
 }
 private static boolean augment(int ingredient,boolean[][] edges,int[] capacities,int[] owners,boolean[] seen){
  for(int slot=0;slot<9;slot++)if(edges[ingredient][slot])for(int unit=0;unit<capacities[slot];unit++){
   int t=slot*9+unit;if(seen[t])continue;seen[t]=true;
   if(owners[t]<0 || augment(owners[t],edges,capacities,owners,seen)){owners[t]=ingredient;return true;}
  }
  return false;
 }
}
