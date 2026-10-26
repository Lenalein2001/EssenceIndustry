package com.neolena.essenceindustry.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.neolena.essenceindustry.compat.ae2.pattern.PatternManagerTile;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import java.util.*;

/** CPUs retain their planned inputs after ME storage stops advertising the recipe. */
@Pseudo
@Mixin(targets="appeng.me.service.CraftingService",remap=false)
public abstract class EssencePatternExecutionMixin {
 @Shadow @Final private IGrid grid;
 @ModifyReturnValue(method="getProviders",at=@At("RETURN"),remap=false)
 private Iterable<ICraftingProvider> essenceindustry$heldInputProvider(Iterable<ICraftingProvider> providers,IPatternDetails pattern){
  var manager=PatternManagerTile.executionProvider(grid,pattern);
  if(manager==null)return providers;
  for(var provider:providers)if(provider==manager)return providers;
  return ()->new Iterator<>(){
   boolean first=true;final Iterator<ICraftingProvider> rest=providers.iterator();
   public boolean hasNext(){return first||rest.hasNext();}
   public ICraftingProvider next(){if(first){first=false;return manager;}return rest.next();}
  };
 }
}
