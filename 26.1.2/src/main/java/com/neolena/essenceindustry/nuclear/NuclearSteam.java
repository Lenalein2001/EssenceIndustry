package com.neolena.essenceindustry.nuclear;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;

/** Real, transportable steam. It is a tank fluid, never a placeable world block. */
public final class NuclearSteam extends Fluid {
 private static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES,EssenceIndustry.MOD_ID);
 private static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,EssenceIndustry.MOD_ID);
 public static final DeferredHolder<FluidType,FluidType> TYPE=TYPES.register("steam",()->new FluidType(FluidType.Properties.create().descriptionId("fluid.essenceindustry.steam").density(-10).viscosity(100).temperature(400)));
 public static final DeferredHolder<Fluid,NuclearSteam> STEAM=FLUIDS.register("steam",NuclearSteam::new);
 public static void register(net.neoforged.bus.api.IEventBus bus){TYPES.register(bus);FLUIDS.register(bus);}
 @Override public FluidType getFluidType(){return TYPE.get();}
 @Override public Item getBucket(){return Items.AIR;}
 @Override protected boolean canBeReplacedWith(FluidState s,BlockGetter l,BlockPos p,Fluid f,Direction d){return false;}
 @Override protected Vec3 getFlow(BlockGetter l,BlockPos p,FluidState s){return Vec3.ZERO;}
 @Override public int getTickDelay(LevelReader l){return 20;}
 @Override protected float getExplosionResistance(){return 0;}
 @Override public float getHeight(FluidState s,BlockGetter l,BlockPos p){return 0;}
 @Override public float getOwnHeight(FluidState s){return 0;}
 @Override protected BlockState createLegacyBlock(FluidState s){return Blocks.AIR.defaultBlockState();}
 @Override public boolean isSource(FluidState s){return true;}
 @Override public int getAmount(FluidState s){return 0;}
 @Override public VoxelShape getShape(FluidState s,BlockGetter l,BlockPos p){return Shapes.empty();}
}
