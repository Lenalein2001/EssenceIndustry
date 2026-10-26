package com.neolena.essenceindustry.array;

import appeng.api.stacks.GenericStack;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.EntityBlock;
import rearth.oritech.block.base.entity.MachineBlockEntity;
import rearth.oritech.init.recipes.OritechRecipe;

/** Standard item-processing family. Custom output/world behavior needs its own bridge. */
final class ArrayOritechRecipes {
 private record Profile(RecipeType<?> type,int power){}
 private static final Map<String,Optional<Profile>> PROFILES=new ConcurrentHashMap<>();
 private static Method method(Class<?> type,String name,Class<?>...args)throws NoSuchMethodException{
  for(Class<?> c=type;c!=null;c=c.getSuperclass())try{return c.getDeclaredMethod(name,args);}catch(NoSuchMethodException ignored){}
  throw new NoSuchMethodException(name);
 }
 private static Optional<Profile> discover(String id){
  try{
   var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));if(!(item instanceof BlockItem bi)||!(bi.getBlock() instanceof EntityBlock entity))return Optional.empty();
   var sample=entity.newBlockEntity(BlockPos.ZERO,bi.getBlock().defaultBlockState());if(!(sample instanceof MachineBlockEntity machine))return Optional.empty();
   // Do not bypass specialized output, consumption, input-selection or world logic.
   for(String name:List.of("createCraftingOutputs","removeCraftingInputs"))if(method(sample.getClass(),name,net.neoforged.neoforge.transfer.transaction.Transaction.class).getDeclaringClass()!=MachineBlockEntity.class)return Optional.empty();
   if(method(sample.getClass(),"getRecipeInput").getDeclaringClass()!=MachineBlockEntity.class||method(sample.getClass(),"getCraftingResults",OritechRecipe.class).getDeclaringClass()!=MachineBlockEntity.class||method(sample.getClass(),"findActiveRecipe").getDeclaringClass()!=MachineBlockEntity.class)return Optional.empty();
   for(String name:List.of("calculateEnergyUsage","workTick","getRecipeDuration"))if(method(sample.getClass(),name).getDeclaringClass()!=MachineBlockEntity.class)return Optional.empty();
   if(method(sample.getClass(),"checkCraftingFinished",OritechRecipe.class).getDeclaringClass()!=MachineBlockEntity.class||method(sample.getClass(),"loadRecipeFromInput",net.minecraft.server.level.ServerLevel.class,rearth.oritech.init.recipes.OritechRecipeInput.class,RecipeType.class).getDeclaringClass()!=MachineBlockEntity.class)return Optional.empty();
   var completion=method(sample.getClass(),"onProgressCompleted",net.neoforged.neoforge.transfer.transaction.Transaction.class).getDeclaringClass();if(completion!=MachineBlockEntity.class&&completion!=rearth.oritech.block.base.entity.UpgradableMachineBlockEntity.class)return Optional.empty();
   var getter=method(sample.getClass(),"getOwnRecipeType");if(!getter.trySetAccessible())return Optional.empty();var type=(RecipeType<?>)getter.invoke(sample);
   int power=machine.getEnergyPerTick();return type!=null&&power>0&&power<=100_000_000?Optional.of(new Profile(type,power)):Optional.empty();
  }catch(ReflectiveOperationException|RuntimeException error){org.slf4j.LoggerFactory.getLogger(ArrayOritechRecipes.class).warn("Array could not inspect Oritech machine {}: {}",id,error.toString());return Optional.empty();}
 }
 static boolean machine(ItemStack item){return PROFILES.computeIfAbsent(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),ArrayOritechRecipes::discover).isPresent();}
 static void add(String id,Recipe<?> recipe,Set<String> installed,List<ArrayRecipes.Native> into){
  if(!(recipe instanceof OritechRecipe r)||r.fluidInput().isPresent()||!r.fluidOutputs().isEmpty()||r.itemInputs().isEmpty()||r.itemInputs().size()>32||r.itemResults().isEmpty()||r.time()<1||r.time()>1_000_000)return;
  var inputs=r.itemInputs().stream().map(i->ArrayRecipes.item(i,1)).toList();var outputs=new ArrayList<GenericStack>();for(var result:r.itemResults())outputs.addAll(ArrayRecipes.output(result.create()));if(outputs.isEmpty())return;
  for(String machine:installed){var p=PROFILES.computeIfAbsent(machine,ArrayOritechRecipes::discover).orElse(null);if(p!=null&&p.type()==r.getType())into.add(new ArrayRecipes.Native(id,machine,r.time(),p.power(),inputs,List.copyOf(outputs),List.of()));}
 }
 private ArrayOritechRecipes(){}
}
