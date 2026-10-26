package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class FuelProcessorBlock extends com.neolena.essenceindustry.seedcraft.SeedCrafterBlock {
 public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 public FuelProcessorBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(FACING,net.minecraft.core.Direction.NORTH));}
 @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){builder.add(FACING);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
 @Override protected BlockState rotate(BlockState state,net.minecraft.world.level.block.Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState state,net.minecraft.world.level.block.Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
 @Override protected void neighborChanged(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.level.block.Block block,net.minecraft.world.level.redstone.Orientation orientation,boolean moved){if(level.getBlockEntity(pos) instanceof FuelProcessorTile tile)tile.partChanged(pos);}
 @Override public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){return !level.isClientSide()&&type==NuclearFuels.TILE.get()?(l,p,s,t)->((FuelProcessorTile)t).structureTick():null;}
 @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new FuelProcessorTile(pos,state);}
}
