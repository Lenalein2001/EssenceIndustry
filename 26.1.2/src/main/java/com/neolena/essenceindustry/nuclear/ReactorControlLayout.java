package com.neolena.essenceindustry.nuclear;

import net.minecraft.core.BlockPos;
import java.util.*;

/** Each fuel column contains its own absorber and receives a roof actuator. */
public final class ReactorControlLayout {
 public static boolean isFuel(int x,int z,int width,int depth,int pattern){
  if(x<=0||z<=0||x>=width-1||z>=depth-1)return false;
  return switch(pattern){case 0->x==width/2&&z==depth/2;case 1->x%2==0&&z%2==0;case 2->(x+z)%2==0;default->throw new IllegalArgumentException("Unknown fuel pattern");};
 }

 /** Integrated column bases at local y=1, ordered by Z then X. */
 public static List<BlockPos> plannedControls(int width,int depth,int pattern){
  if(width<5||width>64||depth<5||depth>64||pattern<0||pattern>2)throw new IllegalArgumentException("Invalid reactor control layout");
  var result=new ArrayList<BlockPos>();
  for(int z=1;z<depth-1;z++)for(int x=1;x<width-1;x++)if(isFuel(x,z,width,depth,pattern))result.add(new BlockPos(x,1,z));
  return List.copyOf(result);
 }

 /** Actuator and fuel share an X/Z column; the structure checks their heights. */
 public static boolean covers(BlockPos fuel,BlockPos control){return fuel.getX()==control.getX()&&fuel.getZ()==control.getZ();}

 /** Works in local or world coordinates. Y is ignored; each column is validated separately. */
 public static Optional<BlockPos> firstUncovered(Collection<BlockPos> fuelColumns,Collection<BlockPos> controlColumns){
  var controls=new HashSet<Long>();for(var p:controlColumns)controls.add(key(p.getX(),p.getZ()));
  for(var fuel:fuelColumns)if(!coveredBy(fuel,controls))return Optional.of(fuel);
  return Optional.empty();
 }
 public static int uncoveredCount(Collection<BlockPos> fuelColumns,Collection<BlockPos> controlColumns){
  var controls=new HashSet<Long>();for(var p:controlColumns)controls.add(key(p.getX(),p.getZ()));int count=0;for(var fuel:fuelColumns)if(!coveredBy(fuel,controls))count++;return count;
 }
 private static boolean coveredBy(BlockPos fuel,Set<Long> controls){
  return controls.contains(key(fuel.getX(),fuel.getZ()));
 }

 private static long key(int x,int z){return (long)x<<32|(z&0xffffffffL);}
 private ReactorControlLayout(){}
}
