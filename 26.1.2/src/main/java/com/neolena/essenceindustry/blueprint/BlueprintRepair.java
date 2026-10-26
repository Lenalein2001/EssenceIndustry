package com.neolena.essenceindustry.blueprint;

import com.neolena.essenceindustry.construction.*;
import com.neolena.essenceindustry.nuclear.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Protected replacement, with drops reserved before changing the world. */
public final class BlueprintRepair {
 public enum Result { REMOVED,BLOCKED,DENIED,FULL,LOCKED }
 private static final String JOURNAL="constructionRepair";
 public static boolean pending(BlockEntity host){return host!=null&&host.getPersistentData().contains(JOURNAL);}
 /** Does not post events or mutate the world. Protection is checked again on Build. */
 public static boolean canRemove(ServerPlayer player,BlockEntity host,BlockPos pos,BlockState state){return canRemove(player,host,BlueprintStorage.get(host),pos,state);}
 public static boolean canRemove(ServerPlayer player,BlockEntity host,ItemStack bank,BlockPos pos,BlockState state){
  if(!insidePlan(host,bank,pos))return false;
  if(!(host.getLevel() instanceof ServerLevel level)||!level.hasChunkAt(pos)||pos.equals(host.getBlockPos())||state.isAir()||state.getDestroySpeed(level,pos)<0||!state.getFluidState().isEmpty()||!level.getFluidState(pos).isEmpty())return false;
  if(!player.mayBuild()||player.isSpectator()||!level.getWorldBorder().isWithinBounds(pos)||!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,Direction.UP,new ItemStack(state.getBlock())))return false;
  var owner=NuclearOwners.at(level,pos);if(owner!=null&&owner!=host)return false;
  if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&AeOwnership.foreign(level,pos,host))return false;
  var entity=level.getBlockEntity(pos);
  if(entity!=null){
   // These ports only forward capabilities; all contents remain in the host.
   // Arbitrary modded block entities, even apparently empty ones, may contain
   // components, fluids or private data and are deliberately left untouched.
   if(!(entity instanceof NuclearPortTile))return false;
   try{var data=entity.saveWithFullMetadata(level.registryAccess());for(String key:List.of("id","x","y","z"))data.remove(key);
    // Minecraft always emits an empty components compound; reading NeoForge's
    // persistent-data accessor can likewise create an empty container. Neither
    // carries state, but nonempty values and unknown fields remain protected.
    for(String key:List.of("components","NeoForgeData"))if(data.getCompound(key).map(CompoundTag::isEmpty).orElse(false))data.remove(key);
    if(!data.isEmpty())return false;
   }catch(RuntimeException ex){return false;}
  }
  return level.getBlockState(pos).equals(state);
 }
 public static Result remove(ServerPlayer player,BlockEntity host,ItemStack bank,BlockPos pos,BlockState expected){
  try{return removeChecked(player,host,bank,pos,expected);}catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BlueprintRepair.class).error("Construction repair validation failed safely at {}",pos,ex);return Result.LOCKED;}
 }
 private static Result removeChecked(ServerPlayer player,BlockEntity host,ItemStack bank,BlockPos pos,BlockState expected){
  if(pending(host)||bank.has(QuantumBuilder.RECOVERY.get())||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return Result.LOCKED;
  if(!(host.getLevel() instanceof ServerLevel level)||level.captureBlockSnapshots||level.restoringBlockSnapshots||!canRemove(player,host,bank,pos,expected))return Result.BLOCKED;
  var event=new BreakBlockEvent(level,pos,expected,player);NeoForge.EVENT_BUS.post(event);
  if(event.isCanceled())return Result.DENIED;if(!canRemove(player,host,bank,pos,expected))return Result.BLOCKED;
  var drops=Block.getDrops(expected,level,pos,level.getBlockEntity(pos),player,player.getMainHandItem()).stream().filter(s->!s.isEmpty()).map(ItemStack::copy).toList();
  // A removal with no recoverable item must be done manually, not silently voided.
  if(drops.isEmpty())return Result.BLOCKED;
  var reservation=bank.copy();for(var drop:drops)if(!BuilderWork.add(reservation,drop,drop.getCount()))return Result.FULL;
  var before=bank.get(QuantumBuilder.BANK.get());var journal=new CompoundTag();journal.putLong("position",pos.asLong());journal.put("state",BlockState.CODEC.encodeStart(NbtOps.INSTANCE,expected).getOrThrow());journal.put("drops",ItemStack.CODEC.listOf().encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE),drops).getOrThrow());
  host.getPersistentData().put(JOURNAL,journal);BlueprintStorage.save(host);host.setChanged();
  var captured=new ArrayList<BlockSnapshot>();boolean removed=false;
  try{
   int start=level.capturedBlockSnapshots.size();level.captureBlockSnapshots=true;
   try{removed=level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
   finally{level.captureBlockSnapshots=false;captured.addAll(level.capturedBlockSnapshots.subList(start,level.capturedBlockSnapshots.size()));level.capturedBlockSnapshots.subList(start,level.capturedBlockSnapshots.size()).clear();}
   if(!removed||captured.size()!=1||!level.getBlockState(pos).isAir()){
    restore(level,captured);if(level.getBlockState(pos).equals(expected)){host.getPersistentData().remove(JOURNAL);BlueprintStorage.save(host);return Result.BLOCKED;}return Result.LOCKED;
   }
   if(!Objects.equals(before,bank.get(QuantumBuilder.BANK.get())))return Result.LOCKED;
   // Commit all drops together, then clear the journal. A full or disconnected
   // ME network is harmless: the next supply pass retries returns from this bank.
   bank.set(QuantumBuilder.BANK.get(),reservation.get(QuantumBuilder.BANK.get()));
   host.getPersistentData().remove(JOURNAL);BlueprintStorage.save(host);host.setChanged();
   var snapshot=captured.getFirst();level.markAndNotifyBlock(pos,level.getChunkAt(pos),expected,level.getBlockState(pos),snapshot.getFlags(),512);
   return Result.REMOVED;
  }catch(RuntimeException ex){
   // If removal did not take effect, there is no refund to replay. Any ambiguous
   // result retains the exact drop journal and stops until explicitly reconciled.
   if(level.getBlockState(pos).equals(expected)){host.getPersistentData().remove(JOURNAL);BlueprintStorage.save(host);host.setChanged();}
   org.slf4j.LoggerFactory.getLogger(BlueprintRepair.class).error("Construction repair stopped at {}; pending drops preserved",pos,ex);return Result.LOCKED;
  }
 }
 private static boolean insidePlan(BlockEntity host,ItemStack bank,BlockPos pos){
  var settings=PlannerItem.settings(bank);Direction face;
  if(host instanceof ReactorTile tile)face=tile.facing();else if(host instanceof TurbineTile tile)face=tile.facing();else if(host instanceof FuelProcessorTile tile)face=tile.facing();else if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&AeOwnership.isArray(host))face=AeOwnership.facing(host);else return false;
  var origin=ControllerPlacement.anchor(host);var a=BlueprintPlan.position(origin,face,settings,BlockPos.ZERO);var b=BlueprintPlan.position(origin,face,settings,new BlockPos(settings.width()-1,settings.height()-1,settings.depth()-1));
  return pos.getX()>=Math.min(a.getX(),b.getX())&&pos.getX()<=Math.max(a.getX(),b.getX())&&pos.getY()>=Math.min(a.getY(),b.getY())&&pos.getY()<=Math.max(a.getY(),b.getY())&&pos.getZ()>=Math.min(a.getZ(),b.getZ())&&pos.getZ()<=Math.max(a.getZ(),b.getZ());
 }
 private static void restore(ServerLevel level,List<BlockSnapshot> snapshots){level.restoringBlockSnapshots=true;try{for(int i=snapshots.size()-1;i>=0;i--)snapshots.get(i).restore(snapshots.get(i).getFlags()|Block.UPDATE_CLIENTS);}finally{level.restoringBlockSnapshots=false;}}
 /** Keep the verifier from resolving ArrayTile's AE2 interfaces in installations without AE2. */
 private static final class AeOwnership {
  static boolean isArray(BlockEntity host){return host instanceof com.neolena.essenceindustry.array.ArrayTile;}
  static Direction facing(BlockEntity host){return com.neolena.essenceindustry.array.ArrayLayout.facing(host.getBlockState());}
  static boolean foreign(ServerLevel level,BlockPos pos,BlockEntity host){var array=com.neolena.essenceindustry.array.ArrayTile.owner(level,pos);return array!=null&&array!=host;}
 }
 private BlueprintRepair(){}
}
