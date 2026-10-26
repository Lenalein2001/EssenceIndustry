package com.neolena.essenceindustry.mixin;
import com.neolena.essenceindustry.simulation.SimulationLootContext;
import cy.jdkdigital.productivefarming.util.ExternalCropStats;
import cy.jdkdigital.productivefarming.common.attachment.CropTraitState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="cy.jdkdigital.productivefarming.util.ExternalCropStats",remap=false)
public abstract class SimulationGeneticsMixin {
 @Inject(method="getTrait",at=@At("HEAD"),cancellable=true,remap=false)
 private static void extraSeeds$seedTraits(Level level,BlockPos pos,BlockState state,CallbackInfoReturnable<CropTraitState> cir){var seed=SimulationLootContext.seed(level,pos);if(seed!=null){SimulationLootContext.handled();cir.setReturnValue(ExternalCropStats.fromSeedStack(seed));}}
}
