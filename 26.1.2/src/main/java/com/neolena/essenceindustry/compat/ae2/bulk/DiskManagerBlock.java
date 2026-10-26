package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class DiskManagerBlock extends AEBaseEntityBlock<DiskManagerTile> {
 public DiskManagerBlock(Properties p){super(metalProps(p));}
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(player.mayBuild() && player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof DiskManagerTile tile)
   server.openMenu(new SimpleMenuProvider((id,inv,p)->new DiskManagerMenu(id,inv,tile),Component.translatable("block.essenceindustry.bulk_disk_manager")),b->b.writeBlockPos(pos));
  return InteractionResult.SUCCESS;
 }
}
