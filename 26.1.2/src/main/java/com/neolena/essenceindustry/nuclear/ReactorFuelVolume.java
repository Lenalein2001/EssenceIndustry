package com.neolena.essenceindustry.nuclear;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Volume accounting for the future reactor controller; no burn scheduling or energy generation. */
public final class ReactorFuelVolume {
 public record Snapshot(String spentItem,int fuelMb,int wasteMb){}
 private final int fuelCapacity,wasteCapacity;
 private String spentItem="";
 private int fuel,waste;
 public ReactorFuelVolume(int fuelCapacity,int wasteCapacity){
  if(fuelCapacity<0||wasteCapacity<0)throw new IllegalArgumentException("Negative capacity");
  this.fuelCapacity=fuelCapacity;this.wasteCapacity=wasteCapacity;
 }
 /** Accounts for one already-transferred solid item; caller owns inventory consumption. */
 public boolean load(ItemStack stack){
  if(stack.isEmpty())return false;
  var result=FuelCycle.spentFor(stack);if(result.isEmpty())return false;
  String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()).getPath();
  int amount=result.getCount()*1000;
  if(amount>fuelCapacity-fuel||(!spentItem.isEmpty()&&!spentItem.equals(id)))return false;
  spentItem=id;fuel+=amount;return true;
 }
 public boolean burn(int amount){
  if(amount<=0||amount>fuel||amount>wasteCapacity-waste)return false;
  fuel-=amount;waste+=amount;return true;
 }
 public int ejectWaste(ResourceHandler<ItemResource> output){
  if(waste<1000||Transaction.getCurrentOpenedTransaction()!=null)return 0;
  var item=NuclearFuels.stack(spentItem,1);if(item.isEmpty())return 0;
  int inserted;
  try(var tx=Transaction.openRoot()){
   inserted=(int)output.insert(ItemResource.of(item),waste/1000,tx);
   if(inserted==0)return 0;
   tx.commit();
  }
  waste-=inserted*1000;if(fuel==0&&waste==0)spentItem="";
  return inserted;
 }
 public int fuelMb(){return fuel;}
 public int wasteMb(){return waste;}
 public Snapshot snapshot(){return new Snapshot(spentItem,fuel,waste);}
 public static ReactorFuelVolume restore(int fuelCapacity,int wasteCapacity,Snapshot state){
  var volume=new ReactorFuelVolume(fuelCapacity,wasteCapacity);
  if(state.fuelMb()<0||state.fuelMb()>fuelCapacity||state.wasteMb()<0||state.wasteMb()>wasteCapacity)
   throw new IllegalArgumentException("Invalid stored volume");
  if(state.fuelMb()>0||state.wasteMb()>0){
   if(!java.util.Set.of("spent_uranium_fuel","spent_enriched_uranium_fuel","spent_plutonium_fuel","spent_mox_fuel","spent_thorium_fuel","spent_advanced_nuclear_fuel").contains(state.spentItem()))throw new IllegalArgumentException("Invalid fuel type");
   volume.spentItem=state.spentItem();
  }
  volume.fuel=state.fuelMb();volume.waste=state.wasteMb();return volume;
 }
}
