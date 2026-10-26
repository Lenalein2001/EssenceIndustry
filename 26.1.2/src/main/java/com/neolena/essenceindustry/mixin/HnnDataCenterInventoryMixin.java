package com.neolena.essenceindustry.mixin;
import com.neolena.essenceindustry.compat.hnn.HnnUtilities;
import dev.shadowsoffire.hostilenetworks.tile.DataCenterTileEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="dev.shadowsoffire.hostilenetworks.tile.DataCenterTileEntity$DataCenterItemHandler",remap=false)
public abstract class HnnDataCenterInventoryMixin {
 @Inject(method="insert(ILnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I",at=@At("HEAD"),cancellable=true,remap=false)
 private void extraSeeds$trainedOnly(int slot,ItemResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx,CallbackInfoReturnable<Integer> cir){
  // External automation only. HNN's manual FilteredSlot accepts all models;
  // changing isValid instead gives it a zero capacity and hangs QUICK_MOVE.
  if(slot>=0 && slot<DataCenterTileEntity.MODEL_SLOTS && !HnnUtilities.complete(resource.toStack())){
   net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource,amount);cir.setReturnValue(0);
  }
 }
}
