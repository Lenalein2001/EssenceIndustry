package com.neolena.essenceindustry.blueprint;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import com.neolena.essenceindustry.construction.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;
import java.util.concurrent.Future;

/** Optional AE2 integration: bounded pulls and one asynchronous calculation per controller. */
public final class BlueprintMeSupply {
 // Leave room in the 128-stack bank for changes, returned components and unstackable items.
 private static final int WORKING_BLOCKS=2048;
 private Future<ICraftingPlan> calculation;private IGrid calculatedGrid;private Item calculating;
 private boolean transferring;
 private int amount,cursor,status;private long next;
 private final Map<Item,Integer> batches=new HashMap<>();private final Map<Item,Long> retryAt=new HashMap<>();
 public int status(){return status;}
 public void close(){if(calculation!=null)calculation.cancel(true);calculation=null;calculatedGrid=null;calculating=null;}
 public static IGrid grid(BlockEntity host){
  var level=host.getLevel();if(level==null)return null;
  var own=level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST,host.getBlockPos());
  if(own==null)return null;for(var face:Direction.values()){var node=own.getGridNode(face);if(node!=null&&node.isActive()&&node.getGrid().getEnergyService().isNetworkPowered())return node.getGrid();}return null;
 }
 private static boolean inFlight(IGrid grid,AEItemKey key){return grid.getCraftingService().getCpus().stream().anyMatch(cpu->{var job=cpu.getJobStatus();return job!=null&&job.crafting().what().equals(key);});}
 public void tick(ServerPlayer player,BlockEntity host,ItemStack bank,BlueprintWork work){
  if(transferring||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return;
  transferring=true;try{
  long now=player.level().getGameTime();if(now<next)return;next=now+4;
  if(bank.has(QuantumBuilder.RECOVERY.get())){status=6;work.lockSupply();return;}
  var grid=grid(host);if(grid==null){close();status=1;return;}
  var source=IActionSource.ofPlayer(player);
  try{
   // Pull independent materials even while a recipe calculation or CPU job is waiting.
   var targets=work.supplyTargets(WORKING_BLOCKS);var items=List.copyOf(targets.keySet());int budget=2048,types=0;boolean refreshedCalculation=false;var storage=grid.getStorageService().getInventory();
   // Old saves may already have a full bank of materials needed much later in the plan.
   // Safely return that excess before filling the next placement window; rejected returns stay stored.
   BlueprintReturns.returnExcess(player,host,bank,targets,2048);
   if(bank.has(QuantumBuilder.RECOVERY.get())){status=6;work.lockSupply();return;}
   for(int checked=0;checked<items.size()&&budget>0&&types<8;checked++){
    var item=items.get(checked);int want=targets.getOrDefault(item,0)-work.stored(item);if(want<=0)continue;
    var stack=new ItemStack(item);var key=AEItemKey.of(item);int request=Math.min(budget,want);
    while(request>0&&!BuilderWork.canAdd(bank,stack,request,Optional.empty()))request/=2;if(request==0)continue;
    long simulated=storage.extract(key,request,Actionable.SIMULATE,source);if(simulated<0||simulated>request)throw new IllegalStateException("Invalid ME simulation");
    if(simulated==0)continue;int offered=(int)simulated;var before=bank.get(QuantumBuilder.BANK.get());
    bank.set(QuantumBuilder.RECOVERY.get(),new BuilderTransfer(ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(offered)),GlobalPos.of(host.getLevel().dimension(),host.getBlockPos()),false));BlueprintStorage.save(host);
    long got=storage.extract(key,offered,Actionable.MODULATE,source);
    if(got<0||got>offered||!Objects.equals(before,bank.get(QuantumBuilder.BANK.get()))||got>0&&!BuilderWork.add(bank,stack,(int)got))throw new IllegalStateException("ME extraction changed reserved construction space");
    bank.remove(QuantumBuilder.RECOVERY.get());BlueprintStorage.save(host);budget-=(int)got;types++;if(item==calculating&&got>0)refreshedCalculation=true;status=2;
   }
   if(calculation!=null&&refreshedCalculation){close();status=2;return;}
   if(calculation!=null){
    if(grid!=calculatedGrid){close();return;}if(!calculation.isDone()){status=3;return;}
    var plan=calculation.get();calculation=null;var key=AEItemKey.of(calculating);
    if(targets.getOrDefault(calculating,0)-work.stored(calculating)<amount||inFlight(grid,key)){status=4;return;}
    if(!BuilderWork.canAdd(bank,new ItemStack(calculating),amount,Optional.empty())){status=5;return;}
    if(grid.getStorageService().getInventory().extract(key,amount,Actionable.SIMULATE,source)>0){status=2;return;}
    if(Boolean.getBoolean("essenceindustry.debugPlanner"))for(var missing:plan.missingItems())org.slf4j.LoggerFactory.getLogger(getClass()).info("Missing {} x {}",missing.getLongValue(),missing.getKey());var submitted=plan.simulation()?null:grid.getCraftingService().submitJob(plan,null,null,false,source);if(Boolean.getBoolean("essenceindustry.debugPlanner"))org.slf4j.LoggerFactory.getLogger(getClass()).info("ME calculation amount={} simulation={} result={} missing={}",amount,plan.simulation(),submitted==null?"simulation":submitted.errorCode(),plan.missingItems());if(submitted!=null&&submitted.successful()){batches.put(calculating,Math.min(4096,amount*2));status=4;return;}
    batches.put(calculating,Math.max(1,amount/2));retryAt.put(calculating,now+(amount==1?100:4));status=5;return;
   }
   if(items.isEmpty()){status=0;return;}
   for(int checked=0;checked<items.size();checked++){
    var item=items.get(Math.floorMod(cursor++,items.size()));int want=targets.getOrDefault(item,0)-work.stored(item);if(want<=0||retryAt.getOrDefault(item,0L)>now)continue;
    var stack=new ItemStack(item);var key=AEItemKey.of(item);int request=Math.min(4096,want);
    while(request>0&&!BuilderWork.canAdd(bank,stack,request,Optional.empty()))request/=2;
    if(request==0){status=5;continue;}
    if(inFlight(grid,key)){status=4;continue;}
    if(!grid.getCraftingService().isCraftable(key)){status=5;continue;}
    amount=Math.min(request,batches.getOrDefault(item,64));calculating=item;calculatedGrid=grid;
    calculation=grid.getCraftingService().beginCraftingCalculation(player.level(),new ICraftingSimulationRequester(){public IActionSource getActionSource(){return source;}public appeng.api.networking.IGridNode getGridNode(){return grid.getPivot();}},key,amount,CalculationStrategy.REPORT_MISSING_ITEMS);status=3;return;
   }
  }catch(Exception ex){close();status=6;if(bank.has(QuantumBuilder.RECOVERY.get()))work.lockSupply();org.slf4j.LoggerFactory.getLogger(getClass()).error("Construction ME supply stopped at {}",host.getBlockPos(),ex);}
  }finally{transferring=false;}
 }
}
