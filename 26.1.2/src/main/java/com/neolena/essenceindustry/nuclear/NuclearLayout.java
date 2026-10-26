package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.*;
public final class NuclearLayout {
 public static BlockPos pos(BlockPos controller,Direction facing,int width,int x,int y,int z){return controller.relative(facing.getClockWise(),x-width/2).relative(facing.getOpposite(),z).offset(0,y-1,0);}
 public static BlockPos min(BlockPos a,BlockPos b){return new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));}
 public static BlockPos max(BlockPos a,BlockPos b){return new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));}
 private NuclearLayout(){}
}
