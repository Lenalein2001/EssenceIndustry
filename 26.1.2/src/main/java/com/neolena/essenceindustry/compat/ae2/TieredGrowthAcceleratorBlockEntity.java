package com.neolena.essenceindustry.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.ids.AETags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.core.AEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class TieredGrowthAcceleratorBlockEntity extends GrowthAcceleratorBlockEntity {
    private static final int BASE_POWER_PER_TICK = 8;

    private final int multiplier;

    public TieredGrowthAcceleratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.multiplier = ((TieredGrowthAcceleratorBlock) state.getBlock()).multiplier();
        setInternalMaxPower(MAX_STORED_POWER * this.multiplier);
        getMainNode()
            .setIdlePowerUsage(BASE_POWER_PER_TICK * this.multiplier)
            .setVisualRepresentation(state.getBlock())
            .addService(IGridTickable.class, new TieredTicker());
    }

    private void tick(int ticksSinceLastCall) {
        boolean powered = isPowered();
        if (powered != getBlockState().getValue(GrowthAcceleratorBlock.POWERED)) {
            markForUpdate();
        }
        if (!powered || !(getLevel() instanceof ServerLevel level)) {
            return;
        }

        // The grid already pays the multiplied AE/t through this node's idle usage.
        // Its energy supply does not fill this block's private FE/crank buffer.
        // Require local operation energy only when the grid is not powering us.
        if (!getMainNode().isPowered()) {
            double cost = (double) BASE_POWER_PER_TICK * this.multiplier * Math.max(1, ticksSinceLastCall);
            if (extractAEPower(cost, Actionable.SIMULATE) < cost
                    || extractAEPower(cost, Actionable.MODULATE) < cost) return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos targetPos = getBlockPos().relative(direction);
            if (!level.isLoaded(targetPos)) continue;
            for (int attempt = 0; attempt < this.multiplier; attempt++) {
                if (!level.isLoaded(targetPos)) break;
                BlockState targetState = level.getBlockState(targetPos);
                if (!targetState.is(AETags.GROWTH_ACCELERATABLE)) {
                    break;
                }
                targetState.randomTick(level, targetPos, level.getRandom());
            }
        }
    }

    private final class TieredTicker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            int speed = AEConfig.instance().getGrowthAcceleratorSpeed();
            return new TickingRequest(speed, speed, false);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            tick(ticksSinceLastCall);
            return TickRateModulation.SAME;
        }
    }
}
