package com.neolena.essenceindustry.compat.ae2;
import appeng.api.parts.PartHelper;
import com.neolena.essenceindustry.EssenceIndustry;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
/** One paid burst per target and game tick, across block and cable variants. */
public final class AcceleratorEngine {
 private static final TagKey<Block> DENIED=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(EssenceIndustry.MOD_ID,"cant_accelerate"));
 private static final ThreadLocal<Boolean> RUNNING=ThreadLocal.withInitial(()->false);
 private static final Map<ServerLevel,Epoch> EPOCHS=Collections.synchronizedMap(new WeakHashMap<>());
 private static final class Epoch {long tick=Long.MIN_VALUE;final Set<BlockPos> used=new HashSet<>();}
 public static List<AcceleratorDevice> neighbors(ServerLevel level,BlockPos target){
  var out=new ArrayList<AcceleratorDevice>();
  for(var direction:Direction.values()){
   var pos=target.relative(direction);if(!level.isLoaded(pos))continue;
   if(level.getBlockEntity(pos) instanceof AcceleratorDevice device&&device.getTargetPos().equals(target))out.add(device);
   if(PartHelper.getPart(level,pos,direction.getOpposite()) instanceof AcceleratorDevice device&&device.getTargetPos().equals(target))out.add(device);
  }
  return out;
 }
 public static void setSpeed(ServerLevel level,AcceleratorDevice device,int speed){
  int value=net.minecraft.util.Mth.clamp(speed,1,256);device.setSpeedDirect(value);
  for(var neighbor:neighbors(level,device.getTargetPos()))neighbor.setSpeedDirect(value);
 }
 @SuppressWarnings({"rawtypes","unchecked"})
 public static void tick(ServerLevel level,AcceleratorDevice device){
  if(RUNNING.get()||device.getSpeed()<=1||!device.isPowered())return;
  var pos=device.getTargetPos();if(!level.isLoaded(pos))return;
  var state=level.getBlockState(pos);var target=level.getBlockEntity(pos);
  // Cable hosts are network infrastructure, never a machine to tick recursively.
  if(target==null||state.is(DENIED)||PartHelper.getPartHost(level,pos)!=null)return;
  BlockEntityTicker ticker=state.getTicker(level,target.getType());if(ticker==null)return;
  var leader=neighbors(level,pos).stream().filter(AcceleratorDevice::isPowered).min(Comparator.comparing(AcceleratorDevice::acceleratorPos)).orElse(device);
  if(leader!=device)return;
  var epoch=EPOCHS.computeIfAbsent(level,l->new Epoch());
  if(epoch.tick!=level.getGameTime()){epoch.tick=level.getGameTime();epoch.used.clear();}
  if(epoch.used.contains(pos)||!device.payOperationEnergy())return;
  epoch.used.add(pos.immutable());RUNNING.set(true);
  try{for(int n=1;n<device.getSpeed();n++){
   if(!level.isLoaded(pos)||target.isRemoved()||level.getBlockEntity(pos)!=target)break;
   var current=level.getBlockState(pos);if(current.getBlock()!=state.getBlock()||current.is(DENIED))break;
   ticker.tick(level,pos,current,target);
  }}finally{RUNNING.set(false);}
 }
 private AcceleratorEngine(){}
}
