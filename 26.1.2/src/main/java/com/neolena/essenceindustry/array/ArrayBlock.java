package com.neolena.essenceindustry.array;
import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class ArrayBlock extends AEBaseEntityBlock<ArrayTile>{
 public ArrayBlock(Properties p){super(metalProps(p).noOcclusion().dynamicShape().strength(4).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));registerDefaultState(stateDefinition.any().setValue(ArrayLayout.FACING,net.minecraft.core.Direction.NORTH).setValue(ArrayLayout.FORMED,false));}
 @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(ArrayLayout.FACING,ArrayLayout.FORMED);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(ArrayLayout.FACING,c.getHorizontalDirection().getOpposite());}
 @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moving){super.onPlace(state,level,pos,previous,moving);if(!level.isClientSide()&&previous.is(this)&&ArrayLayout.facing(state)!=ArrayLayout.facing(previous)&&level.getBlockEntity(pos) instanceof ArrayTile tile)tile.orientationChanged(ArrayLayout.facing(previous));}
 @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(level.getBlockEntity(pos) instanceof ArrayTile tile)tile.work();}
 @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return ArrayHullShapes.shape(state,level,pos);}
 @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block from,net.minecraft.world.level.redstone.Orientation orientation,boolean moving){super.neighborChanged(state,level,pos,from,orientation,moving);if(level.getBlockEntity(pos) instanceof ArrayTile tile)tile.redstoneChanged();}
 @Override public boolean onDestroyedByPlayer(BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.item.ItemStack tool,boolean harvest,net.minecraft.world.level.material.FluidState fluid){if(level.getBlockEntity(pos) instanceof ArrayTile tile&&(!tile.empty()||!com.neolena.essenceindustry.blueprint.BlueprintStorage.safeToMove(tile))){if(!level.isClientSide())player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("gui.essenceindustry.array.empty_first"));return false;}return super.onDestroyedByPlayer(state,level,pos,player,tool,harvest,fluid);}
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){if(level.getBlockEntity(pos) instanceof ArrayTile tile)tile.open(player);return InteractionResult.SUCCESS;}
}
