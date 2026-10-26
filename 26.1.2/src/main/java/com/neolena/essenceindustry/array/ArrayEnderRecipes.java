package com.neolena.essenceindustry.array;

import appeng.api.stacks.GenericStack;
import com.enderio.enderio.api.capacitor.CapacitorData;
import com.enderio.enderio.content.machines.alloy.*;
import com.enderio.enderio.content.machines.sag_mill.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.EntityBlock;

/** Base-capacitor processing. Chance products and grinding-ball behavior are not invented. */
final class ArrayEnderRecipes {
 private enum Kind { NONE, ALLOY, SAG }
 private static final Map<String,Kind> MACHINES=new ConcurrentHashMap<>();
 private static Kind discover(String id){
  var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
  if(item instanceof BlockItem bi&&bi.getBlock() instanceof EntityBlock entity){
   var sample=entity.newBlockEntity(BlockPos.ZERO,bi.getBlock().defaultBlockState());
   if(sample instanceof AlloySmelterBlockEntity)return Kind.ALLOY;
   if(sample instanceof SagMillBlockEntity)return Kind.SAG;
  }return Kind.NONE;
 }
 static boolean machine(ItemStack stack){return MACHINES.computeIfAbsent(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),ArrayEnderRecipes::discover)!=Kind.NONE;}
 static void add(String id,Recipe<?> recipe,Set<String> installed,List<ArrayRecipes.Native> into){
  if(recipe instanceof AlloySmeltingRecipe r){
   if(r.inputs().isEmpty()||r.inputs().size()>32||r.output().create().isEmpty())return;
   var needs=r.inputs().stream().map(n->ArrayRecipes.item(n.ingredient(),n.count())).toList();
   for(String machine:installed)if(MACHINES.computeIfAbsent(machine,ArrayEnderRecipes::discover)==Kind.ALLOY)
    append(into,id,machine,r.energy(),AlloySmelterBlockEntity.USAGE.scaleI(()->CapacitorData.simple(1)).get(),needs,ArrayRecipes.output(r.output().create()));
  }else if(recipe instanceof SagMillingRecipe r){
   // Refuse the entire recipe when any product is probabilistic. Never silently omit byproducts.
   if(r.outputs().isEmpty()||r.outputs().stream().anyMatch(o->o.chance()!=1||o.isOptional()||o.getItemStackTemplate().isEmpty()))return;
   var outputs=new ArrayList<GenericStack>();for(var output:r.outputs())outputs.addAll(ArrayRecipes.output(output.getItemStackTemplate().orElseThrow().create()));
   for(String machine:installed)if(MACHINES.computeIfAbsent(machine,ArrayEnderRecipes::discover)==Kind.SAG)
    append(into,id,machine,r.energy(),SagMillBlockEntity.USAGE.scaleI(()->CapacitorData.simple(1)).get(),List.of(ArrayRecipes.item(r.input(),1)),outputs);
  }
 }
 private static void append(List<ArrayRecipes.Native> into,String id,String machine,int energy,int power,List<ArrayRecipes.Demand> inputs,List<GenericStack> outputs){
  if(energy<1||power<1||power>100_000_000||outputs.isEmpty())return;
  long ticks=((long)energy+power-1)/power;if(ticks>1_000_000)return;
  // Native rates run in whole ticks; the Array keeps its own speed/efficiency upgrades.
  into.add(new ArrayRecipes.Native(id,machine,(int)ticks,power,inputs,List.copyOf(outputs),List.of()));
 }
}
