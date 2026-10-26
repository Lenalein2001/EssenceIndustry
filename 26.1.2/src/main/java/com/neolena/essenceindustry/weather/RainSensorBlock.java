package com.neolena.essenceindustry.weather;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
/** Scheduled once per second; no block entity, client tick or per-tick polling. */
public final class RainSensorBlock extends Block {
 public static final BooleanProperty POWERED=BlockStateProperties.POWERED,INVERTED=BlockStateProperties.INVERTED;
 private static final VoxelShape SHAPE=Block.box(0,0,0,16,6,16);
 public RainSensorBlock(Properties properties){super(properties.strength(1.5F).sound(SoundType.METAL));registerDefaultState(defaultBlockState().setValue(POWERED,false).setValue(INVERTED,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(POWERED,INVERTED);}
 @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
 @Override protected boolean isSignalSource(BlockState state){return true;}
 @Override protected int getSignal(BlockState state,BlockGetter level,BlockPos pos,Direction side){return state.getValue(POWERED)?15:0;}
 @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moved){if(!old.is(this) && level instanceof ServerLevel server){refresh(server,pos);if(server.canHaveWeather())server.scheduleTick(pos,this,20);}}
 @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){refresh(level,pos);if(level.canHaveWeather())level.scheduleTick(pos,this,20);}
 public void refresh(ServerLevel level,BlockPos pos){
  var state=level.getBlockState(pos);if(!state.is(this))return;
  // The precipitation heightmap reacts immediately to roofs, without waiting for skylight propagation.
  boolean wet=level.isRaining() && level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,pos.getX(),pos.getZ())<=pos.getY()+1;
  boolean signal=level.canHaveWeather() && (wet!=state.getValue(INVERTED));
  if(signal!=state.getValue(POWERED))level.setBlock(pos,state.setValue(POWERED,signal),Block.UPDATE_ALL);
 }
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(!player.mayBuild())return InteractionResult.PASS;
  if(level instanceof ServerLevel server){server.setBlock(pos,state.cycle(INVERTED),Block.UPDATE_CLIENTS);refresh(server,pos);}
  return InteractionResult.SUCCESS;
 }
}
