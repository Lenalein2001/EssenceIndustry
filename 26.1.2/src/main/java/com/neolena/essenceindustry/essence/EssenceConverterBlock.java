package com.neolena.essenceindustry.essence;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class EssenceConverterBlock extends Block implements EntityBlock {
    public static final BooleanProperty UPGRADE = BooleanProperty.create("upgrade");
    public EssenceConverterBlock(Properties properties) {
        super(properties.strength(2.5F).sound(SoundType.METAL));
        registerDefaultState(defaultBlockState().setValue(UPGRADE, true));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(UPGRADE); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EssenceConverterTile(pos, state); }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof EssenceConverterTile tile) tile.work();
    }
    @Override public boolean onDestroyedByPlayer(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.item.ItemStack tool, boolean harvest, net.minecraft.world.level.material.FluidState fluid) {
        if (com.neolena.essenceindustry.library.StorageBreakSafety.denyPlayerRemoval(level, pos, player)) return false;
        return super.onDestroyedByPlayer(state, level, pos, player, tool, harvest, fluid);
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof EssenceConverterTile tile && tile.safeToMove())
            for (var drop : Block.getDrops(state, (ServerLevel)level, pos, tile)) Block.popResource(level, pos, drop);
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof EssenceConverterTile tile) {
            if (tile.quarantined()) { serverPlayer.sendOverlayMessage(Component.translatable("message.essenceindustry.storage_locked")); return InteractionResult.SUCCESS; }
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new EssenceConverterMenu(id, inventory, tile),
                    Component.translatable("block.essenceindustry.essence_converter")), buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
