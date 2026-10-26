package com.neolena.essenceindustry.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LevelChunk.class)
public abstract class ConstructionBlockChangeMixin {
 @Inject(method="setBlockState",at=@At("RETURN"))
 private void constructionChanged(BlockPos pos,BlockState state,int flags,CallbackInfoReturnable<BlockState> cir){var chunk=(LevelChunk)(Object)this;com.neolena.essenceindustry.blueprint.BlueprintAutomation.changed(chunk.getLevel(),pos,cir.getReturnValue(),state);}
}
