package com.neolena.essenceindustry.nuclear;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;

/** A complete candidate is validated before any formed visual state changes. */
public final class FuelPlantStructure {
 public record Result(int status,BlockPos issue,String component,int found,int required){public Result(int status,BlockPos issue){this(status,issue,"",0,0);}public boolean valid(){return status==0;}}
 public static Result check(ServerLevel level,BlockPos controller,Direction facing,String machine){
  var tile=level.getBlockEntity(controller);var anchor=tile==null?controller:com.neolena.essenceindustry.blueprint.ControllerPlacement.anchor(tile);
  return checkAt(level,controller,anchor,facing,machine);
 }
 private static Result checkAt(ServerLevel level,BlockPos controller,BlockPos anchor,Direction facing,String machine){
  var layout=FuelPlantLayout.get(machine);var min=layout.minimum(anchor,facing);var max=layout.maximum(anchor,facing);
  if(NuclearOwners.conflict(level,controller,min,max))return new Result(8,controller);
  var required=new HashMap<String,Integer>();
  for(var cell:layout.cells())if(!shellSite(layout,cell))required.merge(cell.kind(),1,Integer::sum);
  var found=new HashMap<String,Integer>();int inputs=0,outputs=0;
  for(int y=0;y<layout.height();y++)for(int z=0;z<layout.depth();z++)for(int x=0;x<layout.width();x++){
   var local=new BlockPos(x,y,z);var pos=layout.position(anchor,facing,local);if(!level.isLoaded(pos))return new Result(7,pos);
   var state=level.getBlockState(pos);String kind=NuclearReactor.kind(state);
   boolean boundary=x==0||x==layout.width()-1||y==0||y==layout.height()-1||z==0||z==layout.depth()-1;
   if(pos.equals(controller)){if(!boundary||state.getBlock()!=FuelPlantLayout.block(machine,"controller"))return new Result(5,pos);continue;}
   if(boundary){
    if(NuclearReactor.PORTS.contains(kind)){String role=NuclearReactor.role(state);if(role.equals("fuel_input"))inputs++;else if(role.equals("waste_output"))outputs++;continue;}
    if(Set.of("casing","frame","pressure_panel","glass","roof_window").contains(kind))continue;
    if(kind.equals("actuator")&&required.containsKey(kind)&&y==layout.height()-1){found.merge(kind,1,Integer::sum);continue;}
    return new Result(5,pos);
   }
   if(state.isAir())continue;
   if(!required.containsKey(kind)||kind.equals("actuator"))return new Result(6,pos);
   found.merge(kind,1,Integer::sum);
  }
  for(var entry:new TreeMap<>(required).entrySet())if(found.getOrDefault(entry.getKey(),0).intValue()!=entry.getValue().intValue())return new Result(13,controller,entry.getKey(),found.getOrDefault(entry.getKey(),0),entry.getValue());
  if(inputs==0)return new Result(10,controller);if(outputs==0)return new Result(11,controller);
  return new Result(0,controller);
 }
 public static boolean shellSite(FuelPlantLayout.Layout l,FuelPlantLayout.Cell c){
  var p=c.local();boolean boundary=p.getX()==0||p.getX()==l.width()-1||p.getY()==0||p.getY()==l.height()-1||p.getZ()==0||p.getZ()==l.depth()-1;
  return boundary&&(Set.of("controller","casing","frame","pressure_panel","glass","roof_window").contains(c.kind())||NuclearReactor.PORTS.contains(c.kind()));
 }
 private static boolean controllerSite(FuelPlantLayout.Layout l,FuelPlantLayout.Cell c){var p=c.local();return shellSite(l,c)&&p.getY()>0&&p.getY()<l.height()-1&&((p.getX()==0||p.getX()==l.width()-1)^(p.getZ()==0||p.getZ()==l.depth()-1));}
 /** Small fixed plants can search every eligible wall panel, testing exact interiors before accepting. */
 public static boolean discover(FuelProcessorTile tile){
  if(!(tile.getLevel() instanceof ServerLevel level))return false;var l=tile.layout();var actual=tile.getBlockPos();
  for(var face:Direction.Plane.HORIZONTAL)for(var c:l.cells())if(c.kind().equals("controller")||controllerSite(l,c)){
   var offset=l.position(BlockPos.ZERO,face,c.local());var anchor=actual.subtract(offset);
   if(checkAt(level,actual,anchor,face,tile.machine()).valid()){com.neolena.essenceindustry.blueprint.ControllerPlacement.set(tile,anchor);com.neolena.essenceindustry.blueprint.ControllerPlacement.setFacing(tile,face);return true;}
  }return false;
 }
 public static void visuals(ServerLevel level,BlockPos controller,Direction facing,String machine,boolean formed,boolean running){
  var layout=FuelPlantLayout.get(machine);
  var tile=level.getBlockEntity(controller);var anchor=tile==null?controller:com.neolena.essenceindustry.blueprint.ControllerPlacement.anchor(tile);
  var min=layout.minimum(anchor,facing);var max=layout.maximum(anchor,facing);
  for(var pos:BlockPos.betweenClosed(min,max)){
   if(pos.equals(controller)||!level.isLoaded(pos))continue;var state=level.getBlockState(pos);
   if(!(state.getBlock() instanceof NuclearPart))continue;
   var next=FuelPlantLayout.property(state,"powered",Boolean.toString(formed&&running));
   if(next.hasProperty(NuclearPart.SHELL)){
    int mask=0;if(formed){if(pos.getX()==min.getX())mask|=1;if(pos.getX()==max.getX())mask|=2;if(pos.getY()==min.getY())mask|=4;if(pos.getY()==max.getY())mask|=8;if(pos.getZ()==min.getZ())mask|=16;if(pos.getZ()==max.getZ())mask|=32;}
    next=next.setValue(NuclearPart.SHELL,mask);
   }
   next=NuclearPart.connected(next,level,pos);
   if(next!=state)level.setBlock(pos,next,2);
  }
 }

 private FuelPlantStructure(){}
}
