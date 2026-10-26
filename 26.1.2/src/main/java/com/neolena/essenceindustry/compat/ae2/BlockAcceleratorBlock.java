package com.neolena.essenceindustry.compat.ae2;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public final class BlockAcceleratorBlock extends AEBaseEntityBlock<BlockAcceleratorBlockEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockAcceleratorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(metalProps(properties));
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult
    ) {
        if (level.getBlockEntity(pos) instanceof BlockAcceleratorBlockEntity accelerator) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                    new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                            new BlockAcceleratorMenu(containerId, inventory, accelerator),
                        Component.translatable("container.essenceindustry.block_accelerator")
                    ),
                    buffer -> {
                        buffer.writeBlockPos(pos);
                        buffer.writeBlockPos(accelerator.getTargetPos());
                    }
                );
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(
        BlockState state,
        BlockAcceleratorBlockEntity blockEntity
    ) {
        return state.setValue(POWERED, blockEntity.isPowered());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
}
