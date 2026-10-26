package com.neolena.essenceindustry.library;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class SeedLibraryBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public SeedLibraryBlock(Properties properties) {
        super(properties.strength(2.5F).sound(SoundType.WOOD));
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SeedLibraryTile(pos, state); }
    @Override public boolean onDestroyedByPlayer(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.item.ItemStack tool, boolean harvest, net.minecraft.world.level.material.FluidState fluid) {
        if (com.neolena.essenceindustry.library.StorageBreakSafety.denyPlayerRemoval(level, pos, player)) return false;
        return super.onDestroyedByPlayer(state, level, pos, player, tool, harvest, fluid);
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof SeedLibraryTile tile && tile.safeToMove()
                && (tile.quarantined() || !tile.vault().records().isEmpty())) {
            for (var drop : Block.getDrops(state, (net.minecraft.server.level.ServerLevel)level, pos, tile)) Block.popResource(level, pos, drop);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof SeedLibraryTile tile)
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new SeedLibraryMenu(id, inventory, tile), Component.translatable("block.essenceindustry.seed_library")), buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }
}
