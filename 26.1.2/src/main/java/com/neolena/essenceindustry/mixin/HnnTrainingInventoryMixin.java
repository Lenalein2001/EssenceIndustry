package com.neolena.essenceindustry.mixin;
import com.neolena.essenceindustry.compat.hnn.HnnUtilities;
import dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity$SimItemHandler",remap=false)
public abstract class HnnTrainingInventoryMixin {
 @Shadow @Final private SimChamberTileEntity this$0;
 @Inject(method="insert(ILnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I",at=@At("HEAD"),cancellable=true,remap=false)
 private void extraSeeds$trainingInput(int slot,ItemResource resource,int amount,TransactionContext tx,CallbackInfoReturnable<Integer> cir){
  if(slot==0 && this$0.getSimMode()==SimChamberTileEntity.SimMode.TRAINING && (!HnnUtilities.isModel(resource.toStack()) || HnnUtilities.complete(resource.toStack()))){
   net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource,amount);cir.setReturnValue(0);
  }
 }
 @Inject(method="extract(ILnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I",at=@At("HEAD"),cancellable=true,remap=false)
 private void extraSeeds$finishedOutput(int slot,ItemResource resource,int amount,TransactionContext tx,CallbackInfoReturnable<Integer> cir){
  if(slot==0 && this$0.getSimMode()==SimChamberTileEntity.SimMode.TRAINING){
   net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
   var inv=(InternalItemHandler)(Object)this;
   cir.setReturnValue(this$0.getRuntime()==0 && HnnUtilities.complete(inv.getResource(0).toStack())?inv.extractInternal(0,resource,amount,tx):0);
  }
 }
}
