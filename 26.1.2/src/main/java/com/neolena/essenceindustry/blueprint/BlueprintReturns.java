package com.neolena.essenceindustry.blueprint;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import com.neolena.essenceindustry.construction.*;
import java.util.*;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Bounded, journaled returns from a controller's construction bank. */
public final class BlueprintReturns {
 private static final Set<BlockEntity> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());
 /** Keep exact plain construction ingredients; component-bearing variants are surplus.
  * Stock linked to another wireless access point belongs to that origin, not this controller. */
 public static int returnExcess(ServerPlayer player,BlockEntity host,ItemStack bank,Map<Item,Integer> keep,int budget){
  if(budget<=0||bank.has(QuantumBuilder.RECOVERY.get())||BlueprintRepair.pending(host)||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null||!ACTIVE.add(host))return 0;
  int total=0;try{
   var grid=BlueprintMeSupply.grid(host);if(grid==null)return 0;
   var storage=grid.getStorageService().getInventory();var source=IActionSource.ofPlayer(player);
   var remaining=new HashMap<>(keep);int calls=0;
   for(var entry:bank.getOrDefault(QuantumBuilder.BANK.get(),BuilderStock.EMPTY).entries()){
    if(entry.network().isPresent())continue;
    var stack=entry.item().create();int reserve=0;
    if(ItemStack.isSameItemSameComponents(stack,new ItemStack(stack.getItem()))){reserve=Math.min(stack.getCount(),Math.max(0,remaining.getOrDefault(stack.getItem(),0)));remaining.computeIfPresent(stack.getItem(),(i,n)->Math.max(0,n-stack.getCount()));}
    int offered=Math.min(stack.getCount()-reserve,budget-total);if(offered<=0)continue;if(calls++>=16)break;
    var key=AEItemKey.of(stack);long simulated=storage.insert(key,offered,Actionable.SIMULATE,source);
    if(simulated<0||simulated>offered)throw new IllegalStateException("Invalid construction return simulation");
    if(simulated==0)continue;offered=(int)simulated;
    // Escrow before calling storage. An ambiguous insertion cannot leave spendable
    // copies in the bank; the saved journal remains available for reconciliation.
    var journal=new BuilderTransfer(ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(offered)),GlobalPos.of(host.getLevel().dimension(),host.getBlockPos()),true);
    if(!BuilderWork.removeStock(bank,stack,offered,entry.network()))throw new IllegalStateException("Construction return stock changed");
    bank.set(QuantumBuilder.RECOVERY.get(),journal);BlueprintStorage.save(host);
    var reserved=bank.get(QuantumBuilder.BANK.get());
    try{
     long accepted=storage.insert(key,offered,Actionable.MODULATE,source);
     if(accepted<0||accepted>offered||!Objects.equals(reserved,bank.get(QuantumBuilder.BANK.get())))throw new IllegalStateException("Construction return changed escrow stock");
     int rejected=offered-(int)accepted;
     if(rejected>0&&!BuilderWork.add(bank,stack,rejected,entry.network()))throw new IllegalStateException("Construction return lost reserved refund space");
     bank.remove(QuantumBuilder.RECOVERY.get());BlueprintStorage.save(host);total+=(int)accepted;
    }catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BlueprintReturns.class).error("Construction return escrow retained at {}",host.getBlockPos(),ex);break;}
   }
   return total;
  }catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BlueprintReturns.class).error("Construction return stopped safely at {}",host.getBlockPos(),ex);return total;}finally{ACTIVE.remove(host);}
 }
 private BlueprintReturns(){}
}
