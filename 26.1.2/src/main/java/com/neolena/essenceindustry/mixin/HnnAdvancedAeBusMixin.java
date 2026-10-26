package com.neolena.essenceindustry.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.neolena.essenceindustry.compat.hnn.HnnAe2ModelRouting;
import appeng.api.behaviors.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
/** A combined bus must not immediately retrieve the trained model it just delivered. */
@Pseudo
@Mixin(targets="net.pedroksl.advanced_ae.common.parts.ImportExportBusPart",remap=false)
public abstract class HnnAdvancedAeBusMixin {
 @WrapOperation(method="doBusWork",at=@At(value="INVOKE",target="Lappeng/api/behaviors/StackImportStrategy;transfer(Lappeng/api/behaviors/StackTransferContext;)Z"),remap=false)
 private boolean extraSeeds$retainDataCenterModels(StackImportStrategy strategy,StackTransferContext context,Operation<Boolean> original){
  return !HnnAe2ModelRouting.modelDestination(this) && original.call(strategy,context);
 }
}
