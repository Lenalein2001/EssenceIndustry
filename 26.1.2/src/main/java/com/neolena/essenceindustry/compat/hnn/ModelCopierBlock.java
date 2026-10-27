package com.neolena.essenceindustry.compat.hnn;
import com.neolena.essenceindustry.seedcraft.SeedCrafterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class ModelCopierBlock extends SeedCrafterBlock {
 public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(FACING);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
 @Override public BlockState rotate(BlockState state,net.minecraft.world.level.block.Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
 @Override public BlockState mirror(BlockState state,net.minecraft.world.level.block.Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
 public ModelCopierBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(FACING,net.minecraft.core.Direction.NORTH));}
 @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ModelCopierTile(pos,state);}
}
