package com.neolena.essenceindustry.array;
import dev.ftb.mods.ftbic.recipe.MachineRecipe;
import dev.ftb.mods.ftbic.block.entity.machine.MachineBlockEntity;
import dev.ftb.mods.ftbic.FTBICConfig;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.EntityBlock;
/** Deterministic item-processing recipes. Random/world/chemical behavior is never approximated. */
final class ArrayIndustrialRecipes {
 private record Profile(MachineBlockEntity sample){}
 private static final Map<String,Profile> PROFILES=new java.util.concurrent.ConcurrentHashMap<>();
 private static final Map<String,Boolean> DISCOVERED=new java.util.concurrent.ConcurrentHashMap<>();
 static void prepare(Set<String> installed){for(String machine:installed){var p=profile(machine);if(p!=null)p.sample().initProperties();}}
 private static Profile profile(String machine){
  if(PROFILES.containsKey(machine))return PROFILES.get(machine);
  if(Boolean.FALSE.equals(DISCOVERED.get(machine)))return null;
  try{var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(machine));if(item instanceof BlockItem blockItem&&blockItem.getBlock() instanceof EntityBlock entity&&entity.newBlockEntity(BlockPos.ZERO,blockItem.getBlock().defaultBlockState()) instanceof MachineBlockEntity sample){sample.initProperties();if(sample.recipeType!=null){var profile=new Profile(sample);PROFILES.put(machine,profile);DISCOVERED.put(machine,true);return profile;}}}catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger(ArrayIndustrialRecipes.class).warn("Array could not inspect industrial machine {}: {}",machine,error.toString());}
  DISCOVERED.put(machine,false);return null;
 }
 static boolean machine(ItemStack item){return profile(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())!=null;}
 static void add(String id,Recipe<?> recipe,Set<String> installed,List<ArrayRecipes.Native> into){
  // FTBIC's native SMELTING profile falls back to vanilla cooking recipes.
  if(recipe instanceof net.minecraft.world.item.crafting.AbstractCookingRecipe cooking&&recipe.getType()==net.minecraft.world.item.crafting.RecipeType.SMELTING){
   for(String machine:installed){var profile=PROFILES.get(machine);if(profile==null)continue;var sample=profile.sample();if(sample.recipeType!=dev.ftb.mods.ftbic.recipe.FTBICRecipes.SMELTING)continue;
    double power=sample.energyUse*FTBICConfig.ENERGY.ZAP_TO_FE_CONVERSION_RATE.get();if(!Double.isFinite(power)||power<=0||power>100_000_000)continue;
    int ticks=(int)Math.max(1,Math.min(1_000_000,(double)cooking.cookingTime()/Math.max(1,sample.progressSpeed)));
    var output=cooking.assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(ItemStack.EMPTY));if(output.isEmpty())continue;
    into.add(new ArrayRecipes.Native(id,machine,ticks,(long)Math.ceil(power),List.of(ArrayRecipes.item(cooking.input(),1)),ArrayRecipes.output(output),List.of()));
   }
  }
  if(!(recipe instanceof MachineRecipe r)||!r.soilOptions.isEmpty()||(r.inputs.isEmpty()&&r.inputFluids.isEmpty())||(r.outputs.isEmpty()&&r.outputFluids.isEmpty())||r.outputs.stream().anyMatch(o->o.chance()!=1))return;
  if(!Double.isFinite(r.processingTime)||r.processingTime<=0||r.inputs.size()+r.inputFluids.size()>32)return;
  for(String machine:installed){
   var profile=PROFILES.get(machine);if(profile==null)continue;var sample=profile.sample();if(sample.recipeType!=r.machineType)continue;
   if(!r.inputFluids.isEmpty()||!r.outputFluids.isEmpty()){
    if(!(sample instanceof dev.ftb.mods.ftbic.block.entity.machine.FluidMachineBlockEntity)||r.inputFluids.size()>1||r.outputFluids.size()>1)continue;
    int tank=dev.ftb.mods.ftbic.block.entity.machine.FluidMachineBlockEntity.TANK_CAPACITY;
    if(r.inputFluids.stream().anyMatch(f->f.amount()>tank)||r.outputFluids.stream().anyMatch(f->f.getAmount()>tank))continue;
   }
   double duration=r.processingTime*FTBICConfig.MACHINES.MACHINE_RECIPE_BASE_TICKS.get()/Math.max(1,sample.progressSpeed);if(!Double.isFinite(duration)||duration>1_000_000)continue;
   int ticks=(int)Math.max(1,duration);
   double power=sample.energyUse*FTBICConfig.ENERGY.ZAP_TO_FE_CONVERSION_RATE.get();if(!Double.isFinite(power)||power<=0||power>100_000_000)continue;
   var needs=new ArrayList<ArrayRecipes.Demand>();for(var n:r.inputs)needs.add(ArrayRecipes.item(n.ingredient(),n.count()));
   for(var n:r.inputFluids)needs.add(new ArrayRecipes.Demand(k->k instanceof appeng.api.stacks.AEFluidKey f&&n.ingredient().test(f.toStack(1)),n.amount()));
   var outputs=new ArrayList<appeng.api.stacks.GenericStack>();for(var o:r.outputs)outputs.addAll(ArrayRecipes.output(o.stack()));for(var f:r.outputFluids)if(!f.isEmpty())outputs.add(new appeng.api.stacks.GenericStack(appeng.api.stacks.AEFluidKey.of(f),f.getAmount()));
   into.add(new ArrayRecipes.Native(id,machine,ticks,(long)Math.ceil(power),needs,List.copyOf(outputs),List.of()));
  }
 }
}
