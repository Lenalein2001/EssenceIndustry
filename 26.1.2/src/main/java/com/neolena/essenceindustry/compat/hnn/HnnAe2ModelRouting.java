package com.neolena.essenceindustry.compat.hnn;
import appeng.parts.automation.ExportBusPart;
import appeng.api.stacks.*;
import dev.shadowsoffire.hostilenetworks.tile.*;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.*;
/** Filter AE2's existing cached fuzzy candidates before they spend bus operations on ineligible model tiers. */
public final class HnnAe2ModelRouting {
 public static boolean modelDestination(Object bus){
  var part=(ExportBusPart)bus;var level=part.getLevel();if(level==null)return false;
  boolean configured=false;for(int i=0;i<part.getConfig().size();i++){var stack=part.getConfig().getStack(i);if(stack!=null && stack.what() instanceof AEItemKey key && key.getItem() instanceof dev.shadowsoffire.hostilenetworks.item.DataModelItem){configured=true;break;}}
  if(!configured)return false;
  var pos=part.getHost().getBlockEntity().getBlockPos().relative(part.getSide());if(!level.isLoaded(pos))return false;
  var target=level.getBlockEntity(pos);
  return target instanceof DataCenterIOPortTileEntity port && port.getMode()==dev.shadowsoffire.hostilenetworks.util.IOPortMode.MODELS && port.resolveOwner()!=null;
 }
 public static Collection<Object2LongMap.Entry<AEKey>> filter(Object bus,Collection<Object2LongMap.Entry<AEKey>> keys){
  if(keys.isEmpty())return keys;var first=keys.iterator().next().getKey();
  if(!(first instanceof AEItemKey item) || !(item.getItem() instanceof dev.shadowsoffire.hostilenetworks.item.DataModelItem))return keys;
  var part=(ExportBusPart)bus;var level=part.getLevel();var pos=part.getHost().getBlockEntity().getBlockPos().relative(part.getSide());
  if(level==null || !level.isLoaded(pos))return keys;var target=level.getBlockEntity(pos);
  boolean complete=target instanceof DataCenterTileEntity;
  if(target instanceof DataCenterIOPortTileEntity port)complete=port.getMode()==dev.shadowsoffire.hostilenetworks.util.IOPortMode.MODELS && port.resolveOwner()!=null;
  if(!complete && !(target instanceof SimChamberTileEntity c && c.getSimMode()==SimChamberTileEntity.SimMode.TRAINING))return keys;
  var result=new ArrayList<Object2LongMap.Entry<AEKey>>();
  for(var entry:keys){if(entry.getKey() instanceof AEItemKey key && key.getItem() instanceof dev.shadowsoffire.hostilenetworks.item.DataModelItem && (!HnnUtilities.isModel(key.toStack()) || HnnUtilities.complete(key.toStack())!=complete))continue;result.add(entry);}
  return result;
 }
 private HnnAe2ModelRouting(){}
}
