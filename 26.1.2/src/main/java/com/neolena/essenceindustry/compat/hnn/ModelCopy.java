package com.neolena.essenceindustry.compat.hnn;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;
public final class ModelCopy {
 /** null: not a copy recipe; false: valid but blocked; true: committed. */
 public static Boolean craft(ItemStacksResourceHandler inv){
  int source=-1,blank=-1,diamond=-1;
  var framework=BuiltInRegistries.ITEM.getValue(Identifier.parse("hostilenetworks:blank_data_model"));
  if(framework==Items.AIR)return null;
  for(int i=0;i<9;i++){var r=inv.getResource(i);if(r.isEmpty())continue;
   if(HnnUtilities.isModel(r.toStack())){if(source>=0)return null;source=i;}
   else if(r.is(framework)){if(blank>=0)return null;blank=i;}
   else if(r.is(Items.DIAMOND)){if(diamond>=0)return null;diamond=i;}else return null;
  }
  if(source<0 || blank<0 || diamond<0)return null;
  var copy=inv.getResource(source); // Exact immutable resource: model ID, data, iterations and custom components.
  try(var tx=Transaction.openRoot()){
   if(inv.extract(blank,inv.getResource(blank),1,tx)!=1 || inv.extract(diamond,inv.getResource(diamond),1,tx)!=1)return false;
   for(int i=9;i<19;i++)if(inv.insert(i,copy,1,tx)==1){tx.commit();return true;}
   return false;
  }
 }
 private ModelCopy(){}
}
