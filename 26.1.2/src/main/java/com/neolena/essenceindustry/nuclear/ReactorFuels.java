package com.neolena.essenceindustry.nuclear;

import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

/** Gameplay fuel identities and accounting rates; independent of model or texture variants. */
public final class ReactorFuels {
 public record Rate(long burnUnitsPerSegmentTick,long fePerMb,long heatPerMb){
  public Rate {
   if(burnUnitsPerSegmentTick<1||burnUnitsPerSegmentTick>1_000_000||fePerMb<1||fePerMb>100_000_000_000L||heatPerMb<0||heatPerMb>1_000_000)throw new IllegalArgumentException("Invalid reactor fuel rate");
  }
 }
 public record Definition(String id,String preparedItem,String spentItem,Rate defaults){}
 public record Charge(String fuelId,int millibuckets){
  public Charge {
   if(!known(fuelId)||(millibuckets!=1000&&!(fuelId.equals("uranium")&&millibuckets==9000)))throw new IllegalArgumentException("Invalid solid reactor charge");
  }
  public long units(){return millibuckets*ReactorCore.UNITS_PER_MB;}
 }
 public static final List<Definition> ALL=List.of(
  fuel("uranium","",27778,600_000,500),
  fuel("enriched_uranium","enriched_uranium_fuel",27778,2_400_000,900),
  fuel("plutonium","plutonium_fuel",34722,4_800_000,1600),
  fuel("mox","mox_fuel",25000,3_600_000,1000),
  fuel("thorium","thorium_fuel",16667,900_000,300),
  fuel("advanced_nuclear","advanced_nuclear_fuel",41667,9_600_000,2400));
 private static final Map<String,Definition> BY_ID;
 private static final TagKey<Item> URANIUM_INGOTS=tag("c:ingots/uranium"),URANIUM_BLOCKS=tag("c:storage_blocks/uranium");
 private static final List<TagKey<Item>> REJECTED_FORMS=List.of(tag("c:nuggets"),tag("c:dusts"),tag("c:nuggets/uranium"),tag("c:dusts/uranium"));
 static {var values=new LinkedHashMap<String,Definition>();for(var definition:ALL)values.put(definition.id(),definition);BY_ID=Collections.unmodifiableMap(values);}
 // Preserve power and thermal behavior while making each unit of fuel last 32 times longer.
 public static final int FUEL_EFFICIENCY=32;
 private static Definition fuel(String id,String prepared,long burn,long fe,long heat){
  long efficientBurn=Math.max(1,Math.round(burn/32d));
  long efficientFe=Math.round(burn*(double)fe/efficientBurn);
  long efficientHeat=Math.round(burn*(double)heat/efficientBurn);
  return new Definition(id,prepared.isEmpty()?"":"essenceindustry:"+prepared,"essenceindustry:spent_"+id+"_fuel",new Rate(efficientBurn,efficientFe,efficientHeat));}
 private static TagKey<Item> tag(String id){return TagKey.create(Registries.ITEM,Identifier.parse(id));}
 public static boolean known(String id){return BY_ID.containsKey(id);}
 public static Definition definition(String id){var result=BY_ID.get(id);if(result==null)throw new IllegalArgumentException("Unknown reactor fuel: "+id);return result;}
 public static Map<String,Rate> defaults(){var result=new LinkedHashMap<String,Rate>();for(var definition:ALL)result.put(definition.id(),definition.defaults());return Map.copyOf(result);}
 /** One solid item, regardless of stack count. The caller must consume that item atomically. */
 public static Charge charge(ItemStack stack){
  if(stack.isEmpty()||REJECTED_FORMS.stream().anyMatch(stack::is))return null;
  String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
  for(var definition:ALL)if(id.equals(definition.preparedItem()))return new Charge(definition.id(),1000);
  if(stack.is(URANIUM_BLOCKS))return new Charge("uranium",9000);
  if(stack.is(URANIUM_INGOTS))return new Charge("uranium",1000);
  return null;
 }
 public static ItemStack spentStack(String fuelId,int count){
  if(!known(fuelId)||count<1)return ItemStack.EMPTY;
  return NuclearFuels.stack(definition(fuelId).spentItem().substring("essenceindustry:".length()),count);
 }
 private ReactorFuels(){}
}
