package com.neolena.essenceindustry.mixin;

import com.neolena.essenceindustry.farmland.VitalizedFarmlands;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(targets = {"com.blakebr0.mysticalagriculture.block.MysticalCropBlock", "com.blakebr0.mysticalagriculture.block.InferiumCropBlock"}, remap = false)
public abstract class VitalizedSeedDropsMixin {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;", at = @At("RETURN"))
    private void extraSeeds$vitalizedBonus(BlockState state, LootParams.Builder context, CallbackInfoReturnable<List<ItemStack>> result) {
        VitalizedFarmlands.addBonusSeed(state, context, result.getReturnValue());
    }
}
