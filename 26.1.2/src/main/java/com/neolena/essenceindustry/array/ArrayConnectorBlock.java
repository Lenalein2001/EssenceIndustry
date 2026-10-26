package com.neolena.essenceindustry.array;
import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
/** Only network endpoints need extra block entities; they have no server ticker. */
public final class ArrayConnectorBlock extends AEBaseEntityBlock<ArrayConnectorTile>{
 public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
 public ArrayConnectorBlock(Properties p){super(metalProps(p).noOcclusion().dynamicShape().strength(4).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ArrayLayout.FORMED,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FACING,ArrayLayout.FORMED);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getNearestLookingDirection().getOpposite());}
 @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return ArrayHullShapes.shape(state,level,pos);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){var owner=ArrayTile.owner(level,p);if(owner!=null&&owner.formed()){owner.open(player);return InteractionResult.SUCCESS;}return InteractionResult.PASS;}
}
