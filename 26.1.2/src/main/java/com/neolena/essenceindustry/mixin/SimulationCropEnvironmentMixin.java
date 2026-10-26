package com.neolena.essenceindustry.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.neolena.essenceindustry.simulation.SimulationLootContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(targets="com.blakebr0.mysticalagriculture.block.MysticalCropBlock",remap=false)
public abstract class SimulationCropEnvironmentMixin {
 @WrapOperation(method="canGrow",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),remap=false)
 private BlockState extraSeeds$environment(Level level,BlockPos pos,Operation<BlockState> original){var virtual=SimulationLootContext.block(level,pos);return virtual==null?original.call(level,pos):virtual;}
 @WrapOperation(method="getDrops",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),remap=false)
 private BlockState extraSeeds$farmland(ServerLevel level,BlockPos pos,Operation<BlockState> original){var virtual=SimulationLootContext.block(level,pos);return virtual==null?original.call(level,pos):virtual;}
}
