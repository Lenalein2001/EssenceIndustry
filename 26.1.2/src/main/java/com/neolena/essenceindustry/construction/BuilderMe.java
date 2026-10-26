package com.neolena.essenceindustry.construction;
import appeng.api.config.*;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.GlobalPos;
/** Optional AE2 class. Never resolves chunks or caches a grid across network mutations. */
public final class BuilderMe {
 public static boolean bind(UseOnContext c){if(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof IWirelessAccessPoint){c.getItemInHand().set(QuantumBuilder.LINK.get(),GlobalPos.of(c.getLevel().dimension(),c.getClickedPos()));c.getPlayer().sendOverlayMessage(net.minecraft.network.chat.Component.translatable("gui.essenceindustry.builder.linked"));return true;}return false;}
 public static IGrid grid(ServerPlayer p,ItemStack tool){return gridAt(p,tool.get(QuantumBuilder.LINK.get()));}
 public static IGrid gridAt(ServerPlayer p,GlobalPos pos){if(pos==null)return null;var level=p.level().getServer().getLevel(pos.dimension());
  if(level==null||!level.hasChunkAt(pos.pos())||!(level.getBlockEntity(pos.pos()) instanceof IWirelessAccessPoint ap)||!ap.isActive())return null;
  if(!QuantumWireless.upgraded(ap)&&(p.level()!=level||pos.pos().distToCenterSqr(p.position())>=ap.getRange()*ap.getRange()))return null;
  var grid=ap.getGrid();return grid!=null&&grid.getEnergyService().isNetworkPowered()?grid:null;
 }
 public static boolean supply(ServerPlayer p,ItemStack tool,ItemStack material,int requested){if(requested<=0||requested>64||tool.has(QuantumBuilder.RECOVERY.get())||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return false;var origin=tool.get(QuantumBuilder.LINK.get());var grid=gridAt(p,origin);if(grid==null)return false;
  var key=AEItemKey.of(material);var source=IActionSource.ofPlayer(p);var storage=grid.getStorageService().getInventory();
  int n=(int)Math.min(requested,storage.extract(key,requested,Actionable.SIMULATE,source));if(n<=0)return false;
  if(!BuilderWork.canAdd(tool,material,n,java.util.Optional.of(origin)))return false;
  var bank=tool.get(QuantumBuilder.BANK.get());tool.set(QuantumBuilder.RECOVERY.get(),new BuilderTransfer(ItemStackTemplate.fromNonEmptyStack(material.copyWithCount(n)),origin,false));
  try {
   long got=storage.extract(key,n,Actionable.MODULATE,source);
   if(got<0||got>n||!java.util.Objects.equals(bank,tool.get(QuantumBuilder.BANK.get())))throw new IllegalStateException("ME extraction violated reserved capacity");
   if(got>0){tool.set(QuantumBuilder.RECOVERY.get(),new BuilderTransfer(ItemStackTemplate.fromNonEmptyStack(material.copyWithCount((int)got)),origin,false));if(!BuilderWork.add(tool,material,(int)got,java.util.Optional.of(origin)))throw new IllegalStateException("Reserved space changed");}
   tool.remove(QuantumBuilder.RECOVERY.get());return got>0;
  }catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BuilderMe.class).error("Quantum Builder ME request retained for reconciliation after ambiguous extraction",ex);return false;}
 }
 public static int returnNetwork(ServerPlayer p,ItemStack tool){
  if(tool.has(QuantumBuilder.RECOVERY.get())||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return 0;int calls=0,total=0;
  for(var e:tool.getOrDefault(QuantumBuilder.BANK.get(),BuilderStock.EMPTY).entries()){
   if(e.network().isEmpty())continue;if(calls++>=4)break;var grid=gridAt(p,e.network().get());if(grid==null)continue;
   var material=e.item().create();var before=tool.get(QuantumBuilder.BANK.get());tool.set(QuantumBuilder.RECOVERY.get(),new BuilderTransfer(e.item(),e.network().get(),true));
   try{
    long accepted=grid.getStorageService().getInventory().insert(AEItemKey.of(material),material.getCount(),Actionable.MODULATE,IActionSource.ofPlayer(p));
    if(accepted<0||accepted>material.getCount()||!java.util.Objects.equals(before,tool.get(QuantumBuilder.BANK.get())))throw new IllegalStateException("ME refund changed reserved stock");
    if(accepted>0&&!BuilderWork.removeStock(tool,material,(int)accepted,e.network()))throw new IllegalStateException("ME refund reservation missing");
    tool.remove(QuantumBuilder.RECOVERY.get());total+=(int)accepted;if(accepted==0)break;
   }catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BuilderMe.class).error("Quantum Builder refund retained for reconciliation after ambiguous insertion",ex);break;}
  }return total;
 }
 private BuilderMe(){}
}
