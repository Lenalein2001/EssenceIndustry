package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
/** Fixed, baked parts. Only controllers have a server ticker; ports are capability endpoints. */
public class NuclearPart extends TransparentBlock {
 public static final BooleanProperty POWERED=BlockStateProperties.POWERED,LEFT=BooleanProperty.create("left"),RIGHT=BooleanProperty.create("right"),TOP=BooleanProperty.create("top"),BOTTOM=BooleanProperty.create("bottom");
 public static final BooleanProperty OUTPUT=BooleanProperty.create("output");
 public static final BooleanProperty CONTROLLED=BooleanProperty.create("controlled");
 public static final IntegerProperty SHELL=IntegerProperty.create("shell",0,63),FUEL=IntegerProperty.create("fuel",0,5),FUEL_STATE=IntegerProperty.create("level",0,4);
 public final String kind;
 public NuclearPart(String kind,Properties p){super(p.noOcclusion().strength(3,8).sound(SoundType.METAL));this.kind=kind;var s=defaultBlockState();if(s.hasProperty(POWERED))s=s.setValue(POWERED,false);if(s.hasProperty(CONTROLLED))s=s.setValue(CONTROLLED,false);if(s.hasProperty(OUTPUT))s=s.setValue(OUTPUT,kind.equals("waste_output")||kind.equals("coolant_out"));if(s.hasProperty(BlockStateProperties.HORIZONTAL_FACING))s=s.setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH);if(s.hasProperty(BlockStateProperties.FACING))s=s.setValue(BlockStateProperties.FACING,Direction.NORTH);if(s.hasProperty(TOP))s=s.setValue(TOP,true).setValue(BOTTOM,true);registerDefaultState(s);}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(BlockStateProperties.HORIZONTAL_FACING,POWERED);}
 public static Direction facing(BlockState s){return s.hasProperty(BlockStateProperties.FACING)?s.getValue(BlockStateProperties.FACING):s.hasProperty(BlockStateProperties.HORIZONTAL_FACING)?s.getValue(BlockStateProperties.HORIZONTAL_FACING):Direction.NORTH;}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState();if(s.hasProperty(BlockStateProperties.FACING))return s.setValue(BlockStateProperties.FACING,c.getNearestLookingDirection().getOpposite());if(s.hasProperty(BlockStateProperties.HORIZONTAL_FACING))return s.setValue(BlockStateProperties.HORIZONTAL_FACING,c.getHorizontalDirection().getOpposite());return s;}
 @Override protected BlockState rotate(BlockState s,Rotation r){var before=s;if(s.hasProperty(BlockStateProperties.FACING))s=s.setValue(BlockStateProperties.FACING,r.rotate(facing(s)));else if(s.hasProperty(BlockStateProperties.HORIZONTAL_FACING))s=s.setValue(BlockStateProperties.HORIZONTAL_FACING,r.rotate(facing(s)));if(s.hasProperty(SHELL)){Direction[] dirs={Direction.WEST,Direction.EAST,Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH};int mask=0;for(int i=0;i<6;i++)if((before.getValue(SHELL)&1<<i)!=0){var d=r.rotate(dirs[i]);for(int j=0;j<6;j++)if(dirs[j]==d)mask|=1<<j;}s=s.setValue(SHELL,mask);}for(var d:Direction.values()){var a=PipeBlock.PROPERTY_BY_DIRECTION.get(d);var b=PipeBlock.PROPERTY_BY_DIRECTION.get(r.rotate(d));if(before.hasProperty(a)&&s.hasProperty(b))s=s.setValue(b,before.getValue(a));}return s;}
 @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);if(!l.isClientSide()&&!old.is(s.getBlock())){NuclearOwners.changed(l,p);l.scheduleTick(p,this,1);}}
 @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block source,Orientation o,boolean moving){if(!l.isClientSide())l.scheduleTick(p,this,1);}
 @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){var connected=connected(s,l,p);if(connected!=s)l.setBlock(p,connected,Block.UPDATE_CLIENTS);}
 public static BlockState connected(BlockState s,Level l,BlockPos p){if(!(s.getBlock() instanceof NuclearPart part))return s;
  if(part.kind.equals("cooling_channel")){for(var d:Direction.values()){var q=p.relative(d);var n=l.hasChunkAt(q)?l.getBlockState(q):Blocks.AIR.defaultBlockState();String k=NuclearReactor.kind(n);boolean yes=k.equals("cooling_channel")||((k.equals("coolant_in")||k.equals("coolant_out"))&&facing(n).getAxis()==d.getAxis());s=s.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(d),yes);}}
  if(part.kind.equals("roof_window")){for(var d:Direction.Plane.HORIZONTAL){var q=p.relative(d);s=s.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(d),l.hasChunkAt(q)&&NuclearReactor.kind(l.getBlockState(q)).equals("roof_window"));}}
  if(part.kind.equals("glass")){var f=facing(s);Direction[] dirs=f.getAxis().isVertical()?new Direction[]{Direction.WEST,Direction.EAST,f==Direction.UP?Direction.SOUTH:Direction.NORTH,f==Direction.UP?Direction.NORTH:Direction.SOUTH}:new Direction[]{f.getCounterClockWise(),f.getClockWise(),Direction.UP,Direction.DOWN};var props=new BooleanProperty[]{LEFT,RIGHT,TOP,BOTTOM};for(int i=0;i<4;i++){var q=p.relative(dirs[i]);var n=l.hasChunkAt(q)?l.getBlockState(q):Blocks.AIR.defaultBlockState();s=s.setValue(props[i],NuclearReactor.kind(n).equals("glass")&&facing(n)==f);}}
  if(part.kind.equals("fuel_assembly")||part.kind.equals("centrifuge")||part.kind.equals("treatment_vessel")){s=s.setValue(TOP,!l.getBlockState(p.above()).is(s.getBlock())).setValue(BOTTOM,!l.getBlockState(p.below()).is(s.getBlock()));}
  if(part.kind.equals("fabrication_bed")){var f=facing(s);s=s.setValue(LEFT,l.getBlockState(p.relative(f.getCounterClockWise())).is(s.getBlock())).setValue(RIGHT,l.getBlockState(p.relative(f.getClockWise())).is(s.getBlock()));}
  return s;
 }
 @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){
  var machinery=NuclearShapes.get(kind,state);if(machinery!=null)return machinery;
  return switch(kind){
   case "glass"->switch(facing(state).getAxis()){case X->Block.box(7,0,0,9,16,16);case Y->Block.box(0,7,0,16,9,16);case Z->Block.box(0,0,7,16,16,9);};
   case "roof_window"->Block.box(0,15,0,16,16,16);
   case "centrifuge"->Block.box(3,0,3,13,16,13);
   case "treatment_vessel"->Block.box(2,0,2,14,16,14);
   case "fabrication_bed"->Block.box(0,0,0,16,9,16);
   case "actuator"->net.minecraft.world.phys.shapes.Shapes.or(Block.box(0,0,0,16,16,16),Block.box(3,16,3,13,21,13),Block.box(5,21,5,11,23,11));
   default->super.getShape(state,level,pos,context);
  };
 }
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){var host=NuclearOwners.at(l,p);if(host!=null&&(host.formed()||kind.equals("controller"))){if(!l.isClientSide())host.open(player);return InteractionResult.SUCCESS;}return InteractionResult.PASS;}
 public static class Shell extends NuclearPart{public Shell(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(SHELL);}}
 public static class Frame extends Shell{public Frame(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(BlockStateProperties.AXIS);}}
 public static class Glass extends NuclearPart{public Glass(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(BlockStateProperties.FACING,LEFT,RIGHT,TOP,BOTTOM);}}
 public static class Roof extends NuclearPart{public Roof(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(BlockStateProperties.NORTH,BlockStateProperties.SOUTH,BlockStateProperties.WEST,BlockStateProperties.EAST);}}
 public static class Assembly extends NuclearPart{public Assembly(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(FUEL,FUEL_STATE,TOP,BOTTOM,CONTROLLED);}}
 public static class Vessel extends NuclearPart{public Vessel(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(TOP,BOTTOM);}}
 public static class Conveyor extends NuclearPart{public Conveyor(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(LEFT,RIGHT);}}
 public static class Channel extends NuclearPart{public Channel(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(BlockStateProperties.NORTH,BlockStateProperties.SOUTH,BlockStateProperties.WEST,BlockStateProperties.EAST,BlockStateProperties.UP,BlockStateProperties.DOWN);}}
 public static class Port extends NuclearPart implements EntityBlock{public Port(String k,Properties p){super(k,p);}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(BlockStateProperties.FACING,POWERED,OUTPUT);}@Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new NuclearPortTile(p,s);}@Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player who,BlockHitResult hit){if(l.getBlockEntity(p) instanceof NuclearPortTile t){if(!l.isClientSide())t.open(who);return InteractionResult.SUCCESS;}return InteractionResult.PASS;}}
 public static class Controller extends NuclearPart implements EntityBlock{
  public Controller(String k,Properties p){super(k,p);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ReactorTile(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return !l.isClientSide()&&t==NuclearReactor.TILE.get()?(world,pos,state,tile)->((ReactorTile)tile).tick():null;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(l.getBlockEntity(p) instanceof ReactorTile t){if(!l.isClientSide())t.open(player);return InteractionResult.SUCCESS;}return InteractionResult.PASS;}
  @Override public boolean onDestroyedByPlayer(BlockState s,Level l,BlockPos p,Player player,net.minecraft.world.item.ItemStack tool,boolean harvest,net.minecraft.world.level.material.FluidState fluid){if(l.getBlockEntity(p) instanceof ReactorTile t&&!t.safeToMove())return false;return super.onDestroyedByPlayer(s,l,p,player,tool,harvest,fluid);}
  @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){if(!l.isClientSide()&&player.isCreative()&&l instanceof ServerLevel server&&l.getBlockEntity(p) instanceof ReactorTile t&&t.hasContents()&&t.safeToMove())for(var drop:Block.getDrops(s,server,p,t,player,player.getMainHandItem()))Block.popResource(l,p,drop);return super.playerWillDestroy(l,p,s,player);}
 }
}

