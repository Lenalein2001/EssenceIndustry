package com.neolena.essenceindustry.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import appeng.api.stacks.AEKey;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Collection;
@Pseudo
@Mixin(targets="appeng.parts.automation.ExportBusPart",remap=false)
public abstract class HnnAe2ExportMixin {
 @ModifyExpressionValue(method="doBusWork",at=@At(value="INVOKE",target="Lappeng/api/stacks/KeyCounter;findFuzzy(Lappeng/api/stacks/AEKey;Lappeng/api/config/FuzzyMode;)Ljava/util/Collection;"),remap=false)
 private Collection<Object2LongMap.Entry<AEKey>> extraSeeds$eligibleModels(Collection<Object2LongMap.Entry<AEKey>> keys){
  if(!net.neoforged.fml.ModList.get().isLoaded("hostilenetworks"))return keys;
  return com.neolena.essenceindustry.compat.hnn.HnnAe2ModelRouting.filter(this,keys);
 }
}
