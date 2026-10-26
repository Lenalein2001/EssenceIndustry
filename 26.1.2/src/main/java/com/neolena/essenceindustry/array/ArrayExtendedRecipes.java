package com.neolena.essenceindustry.array;
import appeng.api.stacks.*;
import com.glodblock.github.extendedae.api.IRecipeMachine;
import com.glodblock.github.extendedae.util.RecipeExecutor;
import com.glodblock.github.glodium.recipe.RecipeSearchContext;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.EntityBlock;
/** Discover the native context and executor instead of listing individual machines. */
final class ArrayExtendedRecipes {
 private record Profile(IRecipeMachine<?,?> machine,RecipeType<?> type,RecipeExecutor<?> executor){}
 private static final Map<String,Optional<Profile>> PROFILES=new ConcurrentHashMap<>();
 private static final Map<Class<?>,Optional<Method>> INPUTS=new ConcurrentHashMap<>();
 private static Optional<Profile> discover(String id){
  try{
   var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
   if(!(item instanceof BlockItem bi)||!(bi.getBlock() instanceof EntityBlock entity))return Optional.empty();
   var sample=entity.newBlockEntity(BlockPos.ZERO,bi.getBlock().defaultBlockState());
   if(!(sample instanceof IRecipeMachine<?,?> machine))return Optional.empty();
   // Inspect the shared framework's field types once. Never attach or tick this sample.
   var type=field(machine.getContext(),RecipeType.class);var executor=field(sample,RecipeExecutor.class);
   if(type==null||executor==null||executor.machine()!=machine||executor.maxTime()<1||executor.maxTime()>1_000_000)return Optional.empty();
   return Optional.of(new Profile(machine,type,executor));
  }catch(ReflectiveOperationException|RuntimeException error){org.slf4j.LoggerFactory.getLogger(ArrayExtendedRecipes.class).warn("Array could not discover recipe machine {}: {}",id,error.toString());return Optional.empty();}
 }
 private static <T>T field(Object object,Class<T> type)throws IllegalAccessException{
  if(object==null)return null;T found=null;
  for(Class<?> c=object.getClass();c!=null;c=c.getSuperclass())for(var f:c.getDeclaredFields()){
   if(Modifier.isStatic(f.getModifiers())||!type.isAssignableFrom(f.getType()))continue;
   if(!f.trySetAccessible())return null;T value=type.cast(f.get(object));if(value==null)continue;
   if(found!=null&&found!=value)return null;found=value;
  }return found;
 }
 private static Profile profile(String id){return PROFILES.computeIfAbsent(id,ArrayExtendedRecipes::discover).orElse(null);}
 static boolean machine(ItemStack item){return profile(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())!=null;}
 @SuppressWarnings({"unchecked","rawtypes"})
 static void add(RecipeHolder<?> holder,Set<String> installed,List<ArrayRecipes.Native> into){
  var recipe=holder.value();for(var id:installed){var profile=profile(id);if(profile==null||recipe.getType()!=profile.type())continue;
   synchronized(profile){var context=(RecipeSearchContext)profile.machine().getContext();var previous=context.currentRecipe;
    try{
     var getter=INPUTS.computeIfAbsent(recipe.getClass(),c->{try{var m=c.getMethod("getSample");return List.class.isAssignableFrom(m.getReturnType())?Optional.of(m):Optional.empty();}catch(NoSuchMethodException e){return Optional.empty();}}).orElse(null);if(getter==null)continue;
     var samples=(List<?>)getter.invoke(recipe);if(samples.isEmpty()||samples.size()>32)continue;
     var needs=new ArrayList<ArrayRecipes.Demand>();boolean valid=true;
     for(var value:samples){
      if(value instanceof IngredientStack.Item ingredient){if(ingredient.isEmpty())continue;var sample=ingredient.sample();needs.add(new ArrayRecipes.Demand(k->k instanceof AEItemKey a&&sample.getIngredient().test(a.toStack()),sample.getAmount()));}
      else if(value instanceof IngredientStack.Fluid ingredient){if(ingredient.isEmpty())continue;var sample=ingredient.sample();needs.add(new ArrayRecipes.Demand(k->k instanceof AEFluidKey f&&sample.getIngredient().test(f.toStack(1)),sample.getAmount()));}
      else{valid=false;break;}
     }
     if(!valid||needs.isEmpty())continue;context.currentRecipe=holder;long energy=profile.machine().getNeededEnergy();if(energy<0)continue;
     var output=(ItemStack)((java.util.function.Function)profile.executor().outputGetter()).apply(recipe);if(output==null||output.isEmpty())continue;
     int ticks=profile.executor().maxTime();double power=appeng.api.config.PowerUnit.AE.convertTo(appeng.api.config.PowerUnit.FE,energy)/(double)ticks;if(!Double.isFinite(power)||power>100_000_000)continue;
     into.add(new ArrayRecipes.Native(holder.id().identifier().toString(),id,ticks,Math.max(1,(long)Math.ceil(power)),List.copyOf(needs),ArrayRecipes.output(output),List.of()));
    }catch(ReflectiveOperationException|RuntimeException error){org.slf4j.LoggerFactory.getLogger(ArrayExtendedRecipes.class).warn("Array cannot describe native recipe {} for {}: {}",holder.id(),id,error.toString());}
    finally{context.currentRecipe=previous;}
   }
  }
 }
 private ArrayExtendedRecipes(){}
}
