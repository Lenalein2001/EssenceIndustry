package com.neolena.essenceindustry.nuclear;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
/** Resumable bounded scan: at most 1024 cells per server tick, no chunk loads. */
public final class ReactorStructure {
 public record Result(boolean valid,String issue,BlockPos position,List<BlockPos> fuel,List<BlockPos> controls,List<BlockPos> ports,List<BlockPos> parts,int moderated,int reflected,boolean actuatorMissing){}
 private static final Set<String> WALL=Set.of("casing","frame","pressure_panel","glass","roof_window","controller","fuel_input","waste_output","energy_out","coolant_in","coolant_out","actuator");
 public static boolean shell(BlockState s){return WALL.contains(NuclearReactor.kind(s))&&!NuclearReactor.kind(s).equals("controller");}
 // Retired standalone rods load as moderator salvage through registry aliases.
 private static final Set<String> INSIDE=Set.of("fuel_assembly","moderator","reflector","cooling_channel");
 private final ReactorTile tile;private final int w,d,h;private final BlockState[] cells;private int cursor,details;private Result result;
 private final List<BlockPos> fuel=new ArrayList<>(),ports=new ArrayList<>(),parts=new ArrayList<>();private int moderated,reflected;private BlockPos missingActuator;private final Set<String> roles=new HashSet<>();
 public ReactorStructure(ReactorTile tile){this.tile=tile;w=tile.width();d=tile.depth();h=tile.height();cells=new BlockState[w*d*h];}
 private int index(int x,int y,int z){return (y*d+z)*w+x;}
 private String kind(int x,int y,int z){return NuclearReactor.kind(cells[index(x,y,z)]);}
 private void fail(String why,BlockPos pos){result=new Result(false,why,pos,List.of(),List.of(),List.of(),List.of(),0,0,false);}
 private void failActuator(BlockPos pos){result=new Result(false,"Place an actuator in the roof directly above this fuel column",pos,List.copyOf(fuel),List.copyOf(fuel),List.copyOf(ports),List.copyOf(parts),moderated,reflected,true);}
 public Result step(Level level){return step(level,1024);}
 public Result step(Level level,int allowance){
  if(result!=null)return result;
  for(int budget=0;budget<allowance&&cursor<cells.length;budget++,cursor++){
   int x=cursor%w,z=cursor/w%d,y=cursor/(w*d);var p=tile.position(x,y,z);if(!level.hasChunkAt(p)){fail("Required chunk is unloaded",p);return result;}var s=level.getBlockState(p);cells[cursor]=s;String k=NuclearReactor.kind(s);int boundary=(x==0||x==w-1?1:0)+(y==0||y==h-1?1:0)+(z==0||z==d-1?1:0);
   if(boundary>0){if(!WALL.contains(k)){if(y==h-1&&x>0&&x<w-1&&z>0&&z<d-1&&kind(x,h-2,z).equals("fuel_assembly"))failActuator(p);else fail("Missing reactor shell",p);return result;}if(boundary>1&&!Set.of("casing","frame","pressure_panel").contains(k)){fail("Use casing or frame on edges",p);return result;}if(k.equals("controller")&&!p.equals(tile.getBlockPos())){fail("Only one controller is allowed",p);return result;}if(k.equals("roof_window")&&y!=h-1){fail("Roof windows belong in the roof",p);return result;}if(k.equals("actuator")&&y!=h-1){fail("Actuators belong in the roof above fuel columns",p);return result;}}
   else if(!s.isAir()&&!INSIDE.contains(k)){fail("Unexpected block inside reactor",p);return result;}
  }
  if(cursor<cells.length)return null;
  if(NuclearOwners.conflict(level,tile.getBlockPos(),tile.minimum(),tile.maximum())){fail("Overlaps another formed multiblock",tile.getBlockPos());return result;}
  for(int budget=0;budget<allowance&&details<cells.length;budget++,details++){int x=details%w,z=details/w%d,y=details/(w*d);String k=kind(x,y,z);var p=tile.position(x,y,z);if(!k.isEmpty())parts.add(p);if(NuclearReactor.PORTS.contains(k)){ports.add(p);roles.add(NuclearReactor.role(cells[details]));}
   if(k.equals("fuel_assembly")){
    if(!kind(x,1,z).equals(k)){fail("Column must start on the floor",tile.position(x,1,z));return result;}
    if(y==1){for(int yy=1;yy<h-1;yy++)if(!kind(x,yy,z).equals(k)){fail("Complete the vertical fuel column",tile.position(x,yy,z));return result;}fuel.add(p);if(missingActuator==null&&!kind(x,h-1,z).equals("actuator"))missingActuator=tile.position(x,h-1,z);}
    if(k.equals("fuel_assembly")){boolean m=false,r=false;for(int[] offset:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){String near=kind(x+offset[0],y,z+offset[1]);m|=near.equals("moderator");r|=near.equals("reflector");}if(m)moderated++;if(r)reflected++;}
   }
  }
  if(details<cells.length)return null;
  if(fuel.isEmpty()){fail("Add full-height fuel columns",tile.position(1,1,1));return result;}
  // Controls are built into each fuel column. Spare old roof actuators have no gameplay effect.
  if(missingActuator!=null){failActuator(missingActuator);return result;}
  for(String role:List.of("fuel_input","waste_output","energy_out"))if(!roles.contains(role)){fail("Add "+(role.equals("energy_out")?"an Energy Port":role.equals("fuel_input")?"an Item Port in Input mode":"an Item Port in Output mode")+" to a shell face",tile.getBlockPos());return result;}
  result=new Result(true,"Structure ready",tile.getBlockPos(),List.copyOf(fuel),List.copyOf(fuel),List.copyOf(ports),List.copyOf(parts),moderated,reflected,false);return result;
 }
}
