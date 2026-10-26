package com.neolena.essenceindustry.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.block.MysticalCropBlock;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.Set;

public final class LightIndependentMysticalCropBlock extends MysticalCropBlock {
    public LightIndependentMysticalCropBlock(Crop crop) {
        super(net.minecraft.resources.Identifier.fromNamespaceAndPath("mysticalagriculture", crop.getNameWithSuffix("crop")), crop);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1) || !canGrow(level, pos)) {
            return;
        }

        int age = getAge(state);
        if (age < getMaxAge()) {
            float speed = getGrowthSpeed(state, level, pos);
            boolean shouldGrow = random.nextInt((int) (25.0F / speed) + 1) == 0;
            if (CommonHooks.canCropGrow(level, pos, state, shouldGrow)) {
                level.setBlock(pos, getStateForAge(age + 1), 2);
                CommonHooks.fireCropGrowPost(level, pos, state);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos farmlandPos = pos.below();
        BlockState farmlandState = level.getBlockState(farmlandPos);
        var soilDecision = farmlandState.canSustainPlant(level, farmlandPos, net.minecraft.core.Direction.UP, state);
        return soilDecision.isDefault()
                ? mayPlaceOn(farmlandState, level, farmlandPos)
                : soilDecision.isTrue();
    }

    private boolean canGrow(Level level, BlockPos pos) {
        Crop crop = getCrop();
        var crux = crop.getCruxBlock();
        if (crux != null && level.getBlockState(pos.below(2)).getBlock() != crux) {
            return false;
        }

        if (ModConfigs.REQUIRES_EFFECTIVE_FARMLAND.get() && crop != ModCrops.INFERIUM) {
            var farmland = level.getBlockState(pos.below()).getBlock();
            if (!crop.getTier().isEffectiveFarmland(farmland)) {
                return false;
            }
        }

        Set<net.minecraft.resources.Identifier> requiredBiomes = crop.getRequiredBiomes();
        if (!requiredBiomes.isEmpty()) {
            var biomeKey = level.getBiome(pos).getKey();
            return biomeKey != null && requiredBiomes.contains(biomeKey.identifier());
        }

        return true;
    }
}


