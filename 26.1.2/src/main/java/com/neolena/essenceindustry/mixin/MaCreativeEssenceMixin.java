package com.neolena.essenceindustry.mixin;

import com.blakebr0.cucumber.util.FeatureFlagDisplayItemGenerator;
import com.blakebr0.mysticalagriculture.init.ModCreativeModeTabs;
import com.blakebr0.mysticalagriculture.init.ModItems;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** MA 9.0.10 adds Inferium explicitly, then adds the same item from its crop loop. */
@Mixin(ModCreativeModeTabs.class)
abstract class MaCreativeEssenceMixin {
 @Redirect(method="lambda$static$2",at=@At(value="INVOKE",target="Lcom/blakebr0/cucumber/util/FeatureFlagDisplayItemGenerator$Output;accept(Lnet/minecraft/world/level/ItemLike;)V"),require=0)
 private static void essenceindustry$uniqueEssence(FeatureFlagDisplayItemGenerator.Output output,ItemLike item){
  if(item.asItem()!=ModItems.INFERIUM_ESSENCE.get())output.accept(item);
 }
}
