package com.neolena.essenceindustry.mixin;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/** Permit the native paid training cycle at Faulty tier in native Training mode. */
@Pseudo
@Mixin(targets="dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity",remap=false)
public abstract class HnnTrainingMixin {
 @Inject(method="canUpgrade",at=@At("HEAD"),cancellable=true,remap=false)
 private void extraSeeds$stopAtSelfAware(dev.shadowsoffire.hostilenetworks.data.DataModelInstance model,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir){
  if(((SimChamberTileEntity)(Object)this).getSimMode()==SimChamberTileEntity.SimMode.TRAINING && com.neolena.essenceindustry.compat.hnn.HnnUtilities.complete(model))cir.setReturnValue(false);
 }
 @Redirect(method="serverTick",at=@At(value="INVOKE",target="Ldev/shadowsoffire/hostilenetworks/data/ModelTier;canSim()Z"),remap=false)
 private boolean extraSeeds$allowInitialTraining(ModelTier tier){
  var chamber=(SimChamberTileEntity)(Object)this;
  return tier.canSim() || chamber.getSimMode()==SimChamberTileEntity.SimMode.TRAINING;
 }
}
