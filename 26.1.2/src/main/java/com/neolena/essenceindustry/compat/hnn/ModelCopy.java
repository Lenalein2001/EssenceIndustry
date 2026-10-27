package com.neolena.essenceindustry.compat.hnn;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;
public final class ModelCopy {
 public static Item catalyst(){return com.neolena.essenceindustry.quantum.QuantumTools.CIRCUIT.get();}
 public static boolean isIngredient(ItemStack stack){
  if(stack.isEmpty())return false;
  var framework=BuiltInRegistries.ITEM.getValue(Identifier.parse("hostilenetworks:blank_data_model"));
  return framework!=Items.AIR&&(stack.is(framework)||stack.is(catalyst())||HnnUtilities.isModel(stack));
 }
 /** null: not a copy recipe; false: valid but blocked; true: committed. */
 public static Boolean craft(ItemStacksResourceHandler inv){
  int source=-1,blank=-1,circuit=-1;
  var framework=BuiltInRegistries.ITEM.getValue(Identifier.parse("hostilenetworks:blank_data_model"));
  if(framework==Items.AIR)return null;
  for(int i=0;i<9;i++){var r=inv.getResource(i);if(r.isEmpty())continue;
   if(HnnUtilities.isModel(r.toStack())){if(source>=0)return null;source=i;}
   else if(r.is(framework)){if(blank>=0)return null;blank=i;}
   else if(r.is(catalyst())){if(circuit>=0)return null;circuit=i;}else return null;
  }
  if(source<0 || blank<0 || circuit<0)return null;
  var copy=inv.getResource(source); // Exact immutable resource: model ID, data, iterations and custom components.
  try(var tx=Transaction.openRoot()){
   if(inv.extract(blank,inv.getResource(blank),1,tx)!=1 || inv.extract(circuit,inv.getResource(circuit),1,tx)!=1)return false;
   for(int i=9;i<19;i++)if(inv.insert(i,copy,1,tx)==1){tx.commit();return true;}
   return false;
  }
 }
 private ModelCopy(){}
}
