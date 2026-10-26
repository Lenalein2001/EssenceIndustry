package com.neolena.essenceindustry.farmland;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.farmland.IEssenceFarmland;
import com.blakebr0.mysticalagriculture.block.InfusedFarmlandBlock;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import net.minecraft.util.TriState;

public final class VitalizedFarmlandBlock extends InfusedFarmlandBlock {
    public VitalizedFarmlandBlock(Identifier id, CropTier tier) { super(id, tier); }
    @Override public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockState plant) {
        return direction == Direction.UP && plant.getBlock() instanceof CropBlock ? TriState.TRUE : TriState.DEFAULT;
    }
}
