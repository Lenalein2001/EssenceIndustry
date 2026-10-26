package com.neolena.essenceindustry.compat.pylons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.IContainerFactory;
import net.permutated.pylons.machines.base.AbstractPylonContainer;
import net.permutated.pylons.machines.base.AbstractPylonTile;
import net.permutated.pylons.machines.harvester.HarvesterPylonBlock;

public final class EnhancedHarvestingPylonBlock extends HarvesterPylonBlock {
    public EnhancedHarvestingPylonBlock(Properties properties) { super(properties); }
    @Override public boolean onDestroyedByPlayer(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.item.ItemStack tool, boolean harvest, net.minecraft.world.level.material.FluidState fluid) {
        if (com.neolena.essenceindustry.library.StorageBreakSafety.denyPlayerRemoval(level, pos, player)) return false;
        return super.onDestroyedByPlayer(state, level, pos, player, tool, harvest, fluid);
    }
    @Override public BlockState playerWillDestroy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof EnhancedHarvestingPylonTile tile && tile.quarantined() && tile.safeToMove())
            for (var drop : net.minecraft.world.level.block.Block.getDrops(state, (net.minecraft.server.level.ServerLevel)level, pos, tile)) net.minecraft.world.level.block.Block.popResource(level, pos, drop);
        return super.playerWillDestroy(level, pos, state, player);
    }
    public static boolean unsafeRecovery(BlockEntity tile) { return tile instanceof EnhancedHarvestingPylonTile pylon && !pylon.safeToMove(); }
    public static boolean protectedRecovery(BlockEntity tile) { return tile instanceof EnhancedHarvestingPylonTile pylon && (pylon.quarantined() || !pylon.safeToMove()); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EnhancedHarvestingPylonTile(pos, state); }
    @Override public BlockEntityType<? extends AbstractPylonTile> getTileType() { return EnhancedHarvestingPylon.TILE.get(); }
    @Override public IContainerFactory<AbstractPylonContainer> containerFactory() { return EnhancedHarvestingPylonMenu::new; }
}
