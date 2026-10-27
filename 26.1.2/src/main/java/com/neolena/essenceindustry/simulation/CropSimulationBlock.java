package com.neolena.essenceindustry.simulation;
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
public final class CropSimulationBlock extends Block implements EntityBlock {
 public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(FACING);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
 @Override public BlockState rotate(BlockState state,net.minecraft.world.level.block.Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
 @Override public BlockState mirror(BlockState state,net.minecraft.world.level.block.Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
 public CropSimulationBlock(Properties p){super(p.forceSolidOff().lightLevel(state->15).strength(2.5F).sound(SoundType.METAL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));registerDefaultState(defaultBlockState().setValue(FACING,net.minecraft.core.Direction.NORTH));}
 @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new CropSimulationTile(pos,state);}
 @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(level.getBlockEntity(pos) instanceof CropSimulationTile t)t.work();}
 @Override public boolean onDestroyedByPlayer(BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.item.ItemStack tool,boolean harvest,net.minecraft.world.level.material.FluidState fluid){if(com.neolena.essenceindustry.library.StorageBreakSafety.denyPlayerRemoval(level,pos,player))return false;return super.onDestroyedByPlayer(state,level,pos,player,tool,harvest,fluid);}
 @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
  if(!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof CropSimulationTile t && t.safeToMove() && (!t.inventory().state().seed().isEmpty() || t.inventory().state().count()>0 || t.quarantined()))for(var drop:Block.getDrops(state,(ServerLevel)level,pos,t))Block.popResource(level,pos,drop);
  return super.playerWillDestroy(level,pos,state,player);
 }
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(player.mayBuild() && player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof CropSimulationTile tile){
   if(tile.quarantined())server.sendOverlayMessage(Component.translatable("message.essenceindustry.storage_locked"));
   else server.openMenu(new SimpleMenuProvider((id,inv,p)->new CropSimulationMenu(id,inv,tile),Component.translatable("block.essenceindustry.crop_simulator")),b->b.writeBlockPos(pos));
  }
  return InteractionResult.SUCCESS;
 }
}
