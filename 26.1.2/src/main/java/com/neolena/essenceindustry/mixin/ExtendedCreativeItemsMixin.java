package com.neolena.essenceindustry.mixin;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** The port lists BlockItems once as items and again as blocks. Preserve search visibility. */
@Mixin(targets="com.glodblock.github.extendedae.common.EAERegistryHandler")
abstract class ExtendedCreativeItemsMixin {
 @ModifyVariable(method="lambda$registerTab$1",at=@At("HEAD"),argsOnly=true,require=0)
 private CreativeModeTab.Output essenceindustry$uniqueDisplay(CreativeModeTab.Output original){
  return new CreativeModeTab.Output(){
   final java.util.Set<ItemStack> parents=ItemStackLinkedSet.createTypeAndComponentsSet(),searches=ItemStackLinkedSet.createTypeAndComponentsSet();
   public void accept(ItemStack stack,CreativeModeTab.TabVisibility visibility){
    boolean parent=visibility!=CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY&&parents.add(stack);
    boolean search=visibility!=CreativeModeTab.TabVisibility.PARENT_TAB_ONLY&&searches.add(stack);
    if(parent||search)original.accept(stack,parent&&search?CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS:parent?CreativeModeTab.TabVisibility.PARENT_TAB_ONLY:CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
   }
  };
 }
}
