package com.neolena.essenceindustry.compat.ae2.pattern;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class PatternManagerBlock extends AEBaseEntityBlock<PatternManagerTile> {
    public PatternManagerBlock(Properties p) { super(metalProps(p).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)); }
    @Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.item.ItemStack tool, boolean harvest, net.minecraft.world.level.material.FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof PatternManagerTile tile && !tile.safeToMove()) return false;
        return super.onDestroyedByPlayer(state, level, pos, player, tool, harvest, fluid);
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof PatternManagerTile tile && tile.safeToMove() && (!tile.pending().empty() || tile.quarantined()))
            for (var drop : Block.getDrops(state, (net.minecraft.server.level.ServerLevel)level, pos, tile)) Block.popResource(level, pos, drop);
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof PatternManagerTile tile)
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new PatternManagerMenu(id, inventory, tile), Component.translatable("block.essenceindustry.essence_pattern_manager")), b -> b.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }
}
