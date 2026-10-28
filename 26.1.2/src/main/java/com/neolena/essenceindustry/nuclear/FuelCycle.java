package com.neolena.essenceindustry.nuclear;
import net.minecraft.world.item.*;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import java.util.*;
/** Intentionally simplified game fuel cycle; all units below are ingot-sized (1,000 mB each). */
public final class FuelCycle {
 public record Input(String ingredient,int count){public boolean matches(ItemStack s){return ingredient.startsWith("#")?s.is(TagKey.create(Registries.ITEM,Identifier.parse(ingredient.substring(1)))):BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals("essenceindustry:"+ingredient);}}
 public record Output(String item,int count){}
 public record Process(String machine,int energy,List<Input> inputs,List<Output> outputs){}
 public static final List<Process> PROCESSES=new ArrayList<>();
 static{
  add("fuel_enricher",64000,List.of(new Input("#c:ingots/uranium",9)),new Output("enriched_uranium_fuel",1),new Output("depleted_uranium",8));
  add("fuel_fabricator",32000,List.of(new Input("recovered_plutonium",1)),new Output("plutonium_fuel",1));
  add("fuel_fabricator",32000,List.of(new Input("recovered_plutonium",1),new Input("depleted_uranium",3)),new Output("mox_fuel",4));
  add("fuel_fabricator",16000,List.of(new Input("#c:ingots/thorium",1)),new Output("thorium_fuel",1));
  add("fuel_fabricator",256000,List.of(new Input("recovered_fissile_material",1),new Input("enriched_uranium_fuel",2),new Input("thorium_fuel",1)),new Output("advanced_nuclear_fuel",4));
  for(String name:List.of("uranium","enriched_uranium","plutonium","mox","thorium","advanced_nuclear"))add("fuel_reprocessor",128000,List.of(new Input("spent_"+name+"_fuel",8)),new Output(name.equals("uranium")||name.equals("enriched_uranium")?"recovered_plutonium":"recovered_fissile_material",1),new Output("nuclear_waste",7));
 }
 /** Presentation availability is evaluated against the current world's tags, never during registration.
  * Existing prepared fuel and queued jobs remain usable when a pack removes a source tag. */
 private static final TagKey<Item> THORIUM_INGOTS=TagKey.create(Registries.ITEM,Identifier.parse("c:ingots/thorium"));
 public static boolean thoriumAvailable(){return BuiltInRegistries.ITEM.get(THORIUM_INGOTS).map(tag->tag.size()>0).orElse(false);}
 public static boolean visible(Process process){
  if(!thoriumAvailable()&&(process.inputs().stream().anyMatch(i->optionalThoriumBranch(i.ingredient()))||process.outputs().stream().anyMatch(o->optionalThoriumBranch(o.item()))))return false;
  return process.inputs().stream().allMatch(input->BuiltInRegistries.ITEM.stream().anyMatch(item->input.matches(new ItemStack(item))));
 }
 private static boolean optionalThoriumBranch(String item){return item.contains("thorium")||item.contains("advanced_nuclear");}
 private static void add(String m,int e,List<Input> i,Output...o){PROCESSES.add(new Process(m,e,List.copyOf(i),List.of(o)));}
 /** Basic uranium accepts tagged ingots and storage blocks, but not nuggets or dusts. */
 public static ItemStack spentFor(ItemStack fuel){
  if(new Input("#c:storage_blocks/uranium",1).matches(fuel))return NuclearFuels.stack("spent_uranium_fuel",9);
  if(new Input("#c:ingots/uranium",1).matches(fuel))return NuclearFuels.stack("spent_uranium_fuel",1);
  for(String name:List.of("enriched_uranium","plutonium","mox","thorium","advanced_nuclear"))if(fuel.is(NuclearFuels.MATERIALS.get(name+"_fuel").get()))return NuclearFuels.stack("spent_"+name+"_fuel",1);
  return ItemStack.EMPTY;
 }
 /** Called only after the reactor has finished a burn. Spent fuel and source removal commit together. */
 public static boolean completeBurn(net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> input,int slot,net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> spent){
  if(input==spent||slot<0||slot>=input.size()||input.getAmountAsLong(slot)<1||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return false;
  var resource=input.getResource(slot);var result=spentFor(resource.toStack());if(result.isEmpty())return false;
  try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){
   if(spent.insert(net.neoforged.neoforge.transfer.item.ItemResource.of(result),result.getCount(),tx)!=result.getCount())return false;
   if(input.extract(slot,resource,1,tx)!=1)return false;
   tx.commit();return true;
  }
 }
 public static int fuelMillibuckets(ItemStack fuel){return spentFor(fuel).getCount()*1000;}
 private FuelCycle(){}
}
