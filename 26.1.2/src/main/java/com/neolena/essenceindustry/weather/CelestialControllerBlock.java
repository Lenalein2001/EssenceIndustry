package com.neolena.essenceindustry.weather;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class CelestialControllerBlock extends Block {
 public static final EnumProperty<CelestialMode> MODE=EnumProperty.create("mode",CelestialMode.class);
 public static final BooleanProperty POWERED=BlockStateProperties.POWERED;
 private static final VoxelShape SHAPE=Block.box(0,0,0,16,13,16);
 public CelestialControllerBlock(Properties properties){super(properties.strength(2.5F).sound(SoundType.METAL));registerDefaultState(defaultBlockState().setValue(MODE,CelestialMode.SUNRISE).setValue(POWERED,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(MODE,POWERED);}
 @Override protected VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
 @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(POWERED,context.getLevel().hasNeighborSignal(context.getClickedPos()));}
 @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,Orientation orientation,boolean moved){if(level instanceof ServerLevel server)updateSignal(server,pos);}
 @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moved){
  // Initialize a direct worldgen/structure placement without executing an action.
  if(!old.is(this) && level instanceof ServerLevel server){boolean powered=server.hasNeighborSignal(pos);if(state.getValue(POWERED)!=powered)server.setBlock(pos,state.setValue(POWERED,powered),Block.UPDATE_CLIENTS);}
 }
 public void updateSignal(ServerLevel level,BlockPos pos){
  var state=level.getBlockState(pos);if(!state.is(this))return;
  boolean powered=level.hasNeighborSignal(pos);if(powered==state.getValue(POWERED))return;
  // Latch before changing global state; duplicate/reentrant notifications cannot repeat it.
  level.setBlock(pos,state.setValue(POWERED,powered),Block.UPDATE_CLIENTS);
  if(powered)apply(level,state.getValue(MODE));
 }
 public static boolean apply(ServerLevel level,CelestialMode mode){
  // Vanilla's clock and weather are shared. Nether/End devices must not alter the Overworld.
  if(level.dimension()!=Level.OVERWORLD)return false;
  if(mode.time()>=0){
   var clock=level.dimensionType().defaultClock();if(clock.isEmpty())return false;
   var manager=level.getServer().clockManager();long current=manager.getTotalTicks(clock.get());
   int delta=Math.floorMod(mode.time()-Math.floorMod(current,24000),24000);
   if(delta==0)return false;
   if(current>Long.MAX_VALUE-delta)return false;
   manager.setTotalTicks(clock.get(),current+delta);return true;
  }
  boolean rain=mode!=CelestialMode.CLEAR,thunder=mode==CelestialMode.THUNDER;
  var data=level.getServer().getWeatherData();if(data.isRaining()==rain && data.isThundering()==thunder)return false;
  int duration=rain?(thunder?ServerLevel.THUNDER_DURATION:ServerLevel.RAIN_DURATION).sample(level.getRandom()):ServerLevel.RAIN_DELAY.sample(level.getRandom());
  level.getServer().setWeatherParameters(rain?0:duration,rain?duration:0,rain,thunder);return true;
 }
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(!player.mayBuild())return InteractionResult.PASS;
  if(player instanceof ServerPlayer server)server.openMenu(new SimpleMenuProvider((id,inventory,ignored)->new CelestialControllerMenu(id,inventory,pos),Component.translatable("block.essenceindustry.celestial_controller")),buffer->buffer.writeBlockPos(pos));
  return InteractionResult.SUCCESS;
 }
}
