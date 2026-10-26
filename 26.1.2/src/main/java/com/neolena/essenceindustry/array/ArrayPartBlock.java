package com.neolena.essenceindustry.array;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class ArrayPartBlock extends Block {
 private final boolean glass;
 public ArrayPartBlock(Properties p,boolean glass){super(p.noOcclusion().dynamicShape().strength(4).sound(glass?SoundType.GLASS:SoundType.METAL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));this.glass=glass;registerDefaultState(stateDefinition.any().setValue(ArrayLayout.FACING,Direction.NORTH).setValue(ArrayLayout.FORMED,false).setValue(ArrayLayout.PART,0));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ArrayLayout.FACING,ArrayLayout.FORMED,ArrayLayout.PART);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(ArrayLayout.FACING,c.getHorizontalDirection().getOpposite());}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return ArrayHullShapes.shape(s,l,p);}
 @Override protected boolean skipRendering(BlockState state,BlockState adjacent,Direction side){return glass&&adjacent.is(this)||super.skipRendering(state,adjacent,side);}
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){var tile=ArrayTile.owner(level,pos);if(tile!=null&&tile.formed()){tile.open(player);return InteractionResult.SUCCESS;}return InteractionResult.PASS;}
}
