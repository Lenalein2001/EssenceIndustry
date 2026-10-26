package com.neolena.essenceindustry.array;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Construction owns only supplied building materials, never crafting inputs or world drops. */
public final class ArrayBuilder {
 public enum Status { IDLE, CHECKING, READY, BUILDING, MISSING, BLOCKED, UNLOADED, DENIED, PAUSED, COMPLETE, LOCKED }
 private record Cell(BlockPos pos,int kind){}
 private static final int LIMIT=1_000_000;
 private static final class Budget { int tick=-1,checks,blocks; }
 private static final Map<net.minecraft.server.MinecraftServer,Budget> BUDGETS=new WeakHashMap<>();
 private final ArrayTile tile;
 private final int[] stock=new int[5],needs=new int[5];
 private final List<Cell> pending=new ArrayList<>();
 private Dynamic<?> recovery;
 private Status status=Status.IDLE;
 private UUID actor;
 private int cursor,next,hatches,ports;
 private BlockPos issue=BlockPos.ZERO;
 ArrayBuilder(ArrayTile tile){this.tile=tile;}
 public static Block material(int kind){return switch(kind){case 0->ProcessingArray.CASING.get();case 1->ProcessingArray.GLASS.get();case 2->ProcessingArray.RACK.get();case 3->ProcessingArray.HATCH.get();case 4->ProcessingArray.PORT.get();default->throw new IllegalArgumentException();};}
 public void migrateTo(ItemStack target){if(locked())throw new IllegalStateException("Legacy construction bank is locked");pause();for(int k=0;k<5;k++){while(stock[k]>0){int n=Math.min(64,stock[k]);if(!com.neolena.essenceindustry.construction.BuilderWork.add(target,new ItemStack(material(k)),n))break;stock[k]-=n;}}changed();}
 public int stored(){return Arrays.stream(stock).sum();}
 public int stock(int kind){return stock[kind];}
 public int needed(int kind){return needs[kind];}
 public Status status(){return status;}
 public BlockPos issue(){return issue;}
 public boolean locked(){return recovery!=null;}
 public boolean empty(){return !locked()&&Arrays.stream(stock).allMatch(n->n==0);}
 public boolean busy(){return status==Status.CHECKING||status==Status.BUILDING;}
 public int progress(){return status==Status.CHECKING?100*cursor/Math.max(1,volume()):pending.isEmpty()?status==Status.COMPLETE?100:0:100*next/pending.size();}
 private int volume(){return tile.width()*tile.depth()*tile.height();}
 private void changed(){tile.constructionChanged();}
 public void invalidate(){if(locked())return;status=Status.IDLE;pending.clear();Arrays.fill(needs,0);cursor=next=0;changed();}
 public void calculate(){
  if(locked()||status==Status.BUILDING)return;
  pending.clear();Arrays.fill(needs,0);cursor=next=hatches=ports=0;status=Status.CHECKING;changed();
 }
 public void pause(){if(busy()){status=Status.PAUSED;changed();}}
 public boolean start(ServerPlayer player){
  if(locked()||!player.mayBuild()||!tile.ledger().jobs().isEmpty())return false;
  if(status!=Status.READY&&status!=Status.MISSING&&status!=Status.PAUSED)return false;
  // A paused scan has no complete, authoritative material list.
  if(cursor<volume()){calculate();return false;}
  for(int k=0;k<5;k++)if(stock[k]<needs[k]){status=Status.MISSING;changed();return false;}
  actor=player.getUUID();status=Status.BUILDING;changed();return true;
 }
 /** Move only plain required blocks; no inventory clearing, overflow or creative exemption. */
 public void deposit(ServerPlayer player){
  if(locked()||cursor<volume()||!player.mayBuild())return;
  var inv=player.getInventory();boolean moved=false;
  for(int i=0;i<36;i++){
   var stack=inv.getItem(i);if(!ArrayRecipes.plain(stack))continue;
   for(int k=0;k<5;k++)if(stack.is(material(k).asItem())){
    int n=Math.min(stack.getCount(),Math.max(0,needs[k]-stock[k]));
    if(n>0){stock[k]+=n;stack.shrink(n);moved=true;}
    break;
   }
  }
  if(moved){inv.setChanged();changed();}
 }
 /** Full player inventory leaves every unreturned block in this bank. */
 public void withdraw(ServerPlayer player){
  if(locked()||!player.mayBuild())return;pause();boolean moved=false;
  var inv=player.getInventory();
  for(int k=0;k<5;k++){
   var item=new ItemStack(material(k));
   // Inventory.add deliberately voids creative-mode overflow. Use explicit bounded slots.
   for(int pass=0;pass<2;pass++)for(int i=0;i<36&&stock[k]>0;i++){
    var existing=inv.getItem(i);
    if(pass==0&&(!ItemStack.isSameItemSameComponents(existing,item)||existing.isEmpty()))continue;
    if(pass==1&&!existing.isEmpty())continue;
    int n=Math.min(stock[k],Math.max(0,item.getMaxStackSize()-existing.getCount()));
    if(n>0){if(existing.isEmpty())inv.setItem(i,item.copyWithCount(n));else existing.grow(n);stock[k]-=n;moved=true;}
   }
  }
  if(moved){player.getInventory().setChanged();changed();}
 }
 private void stop(Status why,BlockPos pos){status=why;issue=pos.immutable();changed();}
 private static boolean shellBlock(Block block){return block==ProcessingArray.CASING.get()||block==ProcessingArray.GLASS.get()||block==ProcessingArray.PORT.get()||block==ProcessingArray.HATCH.get();}
 private int kind(int x,int y,int z){
  if(!ArrayLayout.shell(x,y,z,tile.width(),tile.depth(),tile.height()))return ArrayLayout.socket(x,y,z,tile.width(),tile.depth(),tile.height())?2:-1;
  // A connected upper window strip keeps the default housing readable and bounds glazing cost.
  return y==tile.height()-2&&(x==0||x==tile.width()-1)&&z>0&&z<tile.depth()-1?1:0;
 }
 private void finishScan(){
  for(int special=3;special<=4;special++)if((special==3?hatches:ports)==0){
   int selected=-1;var preferred=tile.position(special-2,1,0);
   for(int i=0;i<pending.size();i++)if(pending.get(i).kind()<=1){if(selected<0)selected=i;if(pending.get(i).pos().equals(preferred)){selected=i;break;}}
   if(selected<0){stop(Status.BLOCKED,tile.getBlockPos());return;}
   var cell=pending.get(selected);needs[cell.kind()]--;pending.set(selected,new Cell(cell.pos(),special));needs[special]++;
  }
  status=pending.isEmpty()?Status.COMPLETE:Status.READY;changed();
 }
 public void tick(){
  if(!busy()||tile.getLevel()==null||tile.getLevel().isClientSide())return;
  var level=(net.minecraft.server.level.ServerLevel)tile.getLevel();
  if(tile.width()>ArrayConfig.maxWidth()||tile.depth()>ArrayConfig.maxDepth()||tile.height()>ArrayConfig.maxHeight()){stop(Status.BLOCKED,tile.getBlockPos());return;}
  var budget=BUDGETS.computeIfAbsent(level.getServer(),s->new Budget());int now=level.getServer().getTickCount();
  if(budget.tick!=now){budget.tick=now;budget.checks=budget.blocks=0;}
  if(status==Status.CHECKING){
   int count=Math.min(256,Math.max(0,2048-budget.checks));
   while(cursor<volume()&&count-->0){
    int x=cursor%tile.width(),z=cursor/tile.width()%tile.depth(),y=cursor/(tile.width()*tile.depth());
    var pos=tile.position(x,y,z);
    if(level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)){stop(Status.BLOCKED,pos);return;}
    if(!level.isLoaded(pos)){stop(Status.UNLOADED,pos);return;}
    cursor++;budget.checks++;if(pos.equals(tile.getBlockPos()))continue;
    var state=level.getBlockState(pos);int kind=kind(x,y,z);
    if(ArrayLayout.shell(x,y,z,tile.width(),tile.depth(),tile.height())&&shellBlock(state.getBlock())){
     if(state.is(ProcessingArray.HATCH.get()))hatches++;if(state.is(ProcessingArray.PORT.get()))ports++;continue;
    }
    if(kind==2&&state.is(ProcessingArray.RACK.get()))continue;
    if(!state.isAir()||!level.getFluidState(pos).isEmpty()){stop(Status.BLOCKED,pos);return;}
    if(kind>=0){pending.add(new Cell(pos.immutable(),kind));needs[kind]++;}
   }
   if(cursor==volume())finishScan();return;
  }
  var player=actor==null?null:level.getServer().getPlayerList().getPlayer(actor);
  if(player==null||player.level()!=level||!player.mayBuild()){stop(Status.PAUSED,tile.getBlockPos());return;}
  int count=Math.min(8,Math.max(0,32-budget.blocks));
  while(next<pending.size()&&count-->0){
   var cell=pending.get(next);var pos=cell.pos();var block=material(cell.kind());
   if(!level.isLoaded(pos)){stop(Status.UNLOADED,pos);return;}
   var old=level.getBlockState(pos);
   if(old.is(block)){needs[cell.kind()]--;next++;continue;}
   if(!old.isAir()||!level.getFluidState(pos).isEmpty()){stop(Status.BLOCKED,pos);return;}
   var stack=new ItemStack(block);
   if(!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,Direction.UP,stack)){stop(Status.DENIED,pos);return;}
   if(stock[cell.kind()]<1){stop(Status.MISSING,pos);return;}
   var snapshot=BlockSnapshot.create(level.dimension(),level,pos);
   var facing=ArrayLayout.facing(tile.getBlockState());var local=ArrayLayout.local(tile.getBlockPos(),facing,pos);
   var outward=ArrayLayout.surface(facing,local.getX(),local.getY(),local.getZ(),tile.width(),tile.depth(),tile.height());
   var state=block.defaultBlockState();
   if(state.hasProperty(ArrayConnectorBlock.FACING))state=state.setValue(ArrayConnectorBlock.FACING,outward);
   else state=state.setValue(ArrayLayout.FACING,outward.getAxis().isHorizontal()?outward:facing);
   // Reserve ownership before calling world hooks; settle against the actual world on every exit.
   stock[cell.kind()]--;budget.blocks++;boolean accepted=false;
   try{
    if(!level.setBlock(pos,state,Block.UPDATE_ALL)){stop(Status.BLOCKED,pos);return;}
    var event=new BlockEvent.EntityPlaceEvent(snapshot,level.getBlockState(pos.below()),player);NeoForge.EVENT_BUS.post(event);
    if(event.isCanceled()){snapshot.restore();stop(Status.DENIED,pos);return;}
    accepted=level.getBlockState(pos).is(block);
    if(!accepted){stop(Status.BLOCKED,pos);return;}
   }catch(RuntimeException error){
    org.slf4j.LoggerFactory.getLogger(getClass()).error("Array construction stopped at {}",pos,error);stop(Status.BLOCKED,pos);return;
   }finally{
    // A hook throwing after placement must not produce a free block on retry.
    if(!accepted&&!level.getBlockState(pos).is(block))stock[cell.kind()]++;
    changed();
   }
   needs[cell.kind()]--;next++;
  }
  // Re-scan instead of assuming that previously checked cells stayed unchanged.
  if(next==pending.size())calculateAfterBuild();
 }
 private void calculateAfterBuild(){status=Status.READY;calculate();tile.requestStructureCheck();}
 public void save(ValueOutput out){if(recovery!=null)out.store("arrayBuildMaterials",Codec.PASSTHROUGH,recovery);else out.store("arrayBuildMaterials",Codec.INT.listOf(5,5),Arrays.stream(stock).boxed().toList());}
 public void load(ValueInput in){
  Arrays.fill(stock,0);pending.clear();Arrays.fill(needs,0);cursor=next=0;actor=null;status=Status.IDLE;
  recovery=in.read("arrayBuildMaterials",Codec.PASSTHROUGH).orElse(null);if(recovery==null)return;
  try{var parsed=Codec.INT.listOf(5,5).parse(recovery);if(parsed.error().isEmpty()&&parsed.getOrThrow().stream().allMatch(n->n>=0&&n<=LIMIT)){for(int i=0;i<5;i++)stock[i]=parsed.getOrThrow().get(i);recovery=null;}}catch(RuntimeException ignored){}
  if(recovery!=null)status=Status.LOCKED;
 }
}
