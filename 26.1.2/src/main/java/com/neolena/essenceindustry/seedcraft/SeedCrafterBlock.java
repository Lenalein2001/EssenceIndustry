package com.neolena.essenceindustry.seedcraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public class SeedCrafterBlock extends Block implements EntityBlock {
 public SeedCrafterBlock(Properties p){super(p.strength(2.5F).sound(SoundType.METAL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));}
 @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new SeedCrafterTile(pos,state);}
 @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(level.getBlockEntity(pos) instanceof SeedCrafterTile t)t.work();}
 @Override public boolean onDestroyedByPlayer(BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.item.ItemStack tool,boolean harvest,net.minecraft.world.level.material.FluidState fluid){if(com.neolena.essenceindustry.library.StorageBreakSafety.denyPlayerRemoval(level,pos,player))return false;return super.onDestroyedByPlayer(state,level,pos,player,tool,harvest,fluid);}
 @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
  if(!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof SeedCrafterTile t && t.quarantined() && t.safeToMove())for(var drop:Block.getDrops(state,(ServerLevel)level,pos,t))Block.popResource(level,pos,drop);
  return super.playerWillDestroy(level,pos,state,player);
 }
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(player.mayBuild() && player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof SeedCrafterTile tile){
   if(tile.quarantined())server.sendOverlayMessage(Component.translatable("message.essenceindustry.storage_locked"));
   else server.openMenu(new SimpleMenuProvider((id,inv,p)->new SeedCrafterMenu(id,inv,tile),Component.translatable(getDescriptionId())),b->{b.writeBlockPos(pos);b.writeVarInt(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getId(state.getBlock()));});
  }
  return InteractionResult.SUCCESS;
 }
}
