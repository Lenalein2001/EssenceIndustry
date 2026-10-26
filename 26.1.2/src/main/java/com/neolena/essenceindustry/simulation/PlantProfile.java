package com.neolena.essenceindustry.simulation;
import com.neolena.essenceindustry.library.SeedTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.transfer.item.ItemResource;
import java.util.*;
/** Native crop state and loot. Work is bounded by sample count, never by item throughput. */
public record PlantProfile(CropBlock block,ItemResource seed){
 public static PlantProfile resolve(ItemResource r){if(r.isEmpty())return null;var ma=CropSimulationTile.crop(r);var block=ma==null?Block.byItem(r.getItem()):ma.getCropBlock();return block instanceof CropBlock crop?new PlantProfile(crop,r):null;}
 public static boolean accepts(ItemResource r){var ma=CropSimulationTile.crop(r);return (ma!=null || !(r.getItem() instanceof com.blakebr0.mysticalagriculture.api.crop.ICropProvider)) && resolve(r)!=null;}
 public boolean environment(ServerLevel level,BlockPos pos){return environment(level,pos,level.getBlockState(pos.below()),level.getBlockState(pos.below(2)));}
 public boolean environment(ServerLevel level,BlockPos pos,net.minecraft.world.level.block.state.BlockState soil,net.minecraft.world.level.block.state.BlockState crux){
  SimulationLootContext.begin(seed.toStack(),level,pos,soil,crux);try{
  var ma=CropSimulationTile.crop(seed);if(ma!=null && !ma.isEnabled())return false;
  if(block instanceof com.blakebr0.mysticalagriculture.block.MysticalCropBlock crop)return crop.isValidBonemealTarget(level,pos,crop.defaultBlockState());
  // Read-only LevelReader view supplies the installed soil to vanilla crop survival checks.
  var reader=(net.minecraft.world.level.LevelReader)java.lang.reflect.Proxy.newProxyInstance(net.minecraft.world.level.LevelReader.class.getClassLoader(),new Class<?>[]{net.minecraft.world.level.LevelReader.class},(proxy,method,args)->{
   if(method.getName().equals("getBlockState") && args!=null && args.length==1 && args[0] instanceof BlockPos p){var virtual=SimulationLootContext.block(level,p);if(virtual!=null)return virtual;}
   try{return method.invoke(level,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
  });
  return block.defaultBlockState().canSurvive(reader,pos);
  }finally{SimulationLootContext.end();}
 }
 public HarvestPlan analyze(ServerLevel level,BlockPos pos,int sampleCount){return analyze(level,pos,sampleCount,level.getBlockState(pos.below()),level.getBlockState(pos.below(2)));}
 public HarvestPlan analyze(ServerLevel level,BlockPos pos,int sampleCount,net.minecraft.world.level.block.state.BlockState soil,net.minecraft.world.level.block.state.BlockState crux){
  var samples=new ArrayList<Map<ItemResource,Long>>();var mutants=new HashSet<ItemResource>();var stats=SeedTraits.read(seed.toStack());
  for(int sample=0;sample<sampleCount;sample++){
   List<ItemStack> drops;boolean handled;var parent=seed.toStack();var offspring=SimulationGenetics.offspring(parent,block.getStateForAge(block.getMaxAge()),level.getRandom());boolean improved=offspring!=parent;
   SimulationLootContext.begin(parent,level,pos,soil,crux);
   try{drops=Block.getDrops(block.getStateForAge(block.getMaxAge()),level,pos,null);handled=SimulationLootContext.genesHandled();}finally{SimulationLootContext.end();}
   var result=new LinkedHashMap<ItemResource,Long>();boolean replanted=false;
   for(var stack:drops){if(stack.isEmpty())continue;long amount=stack.getCount();
    boolean seedDrop=accepts(ItemResource.of(stack)) && !stack.has(net.minecraft.core.component.DataComponents.FOOD);
    if(seedDrop && stack.is(seed.getItem()) && improved){stack=offspring.copyWithCount(stack.getCount());mutants.add(ItemResource.of(stack));}
    if(!seedDrop && !handled && stats.yield()>0)amount=Math.addExact(amount,stats.yield());
    if(stack.is(seed.getItem()) && !replanted && (!seedDrop || !improved)){amount--;replanted=true;}if(amount<=0)continue;
    result.merge(ItemResource.of(stack),amount,Math::addExact);
   }
   if(improved && parent.has(net.minecraft.core.component.DataComponents.FOOD)){var key=ItemResource.of(offspring);mutants.add(key);result.merge(key,1L,Math::addExact);}
   samples.add(Map.copyOf(result));
  }
  return new HarvestPlan(List.copyOf(samples),Set.copyOf(mutants));
 }
 public record HarvestPlan(List<Map<ItemResource,Long>> samples,Set<ItemResource> mutants){
  public Map<ItemResource,Integer> produce(long cycles,int multiplier){
   var result=new LinkedHashMap<ItemResource,Integer>();long total=0;
   for(int sample=0;sample<samples.size();sample++){
    long weight=cycles/samples.size()+(sample<cycles%samples.size()?1:0);if(weight==0)continue;
    for(var entry:samples.get(sample).entrySet()){
     long made=Math.multiplyExact(Math.multiplyExact(entry.getValue(),weight),(mutants.contains(entry.getKey()) || accepts(entry.getKey()) && !entry.getKey().toStack().has(net.minecraft.core.component.DataComponents.FOOD))?1:multiplier);if(made>SimulationStore.LIMIT || (total+=made)>SimulationStore.LIMIT)throw new BatchTooLarge();
     int sum=Math.addExact(result.getOrDefault(entry.getKey(),0),(int)made);if(sum>SimulationStore.LIMIT || !result.containsKey(entry.getKey()) && result.size()>=SimulationStore.OUTPUTS)throw new BatchTooLarge();result.put(entry.getKey(),sum);
    }
   }
   return result;
  }
 }
 public static final class BatchTooLarge extends RuntimeException{}
}
