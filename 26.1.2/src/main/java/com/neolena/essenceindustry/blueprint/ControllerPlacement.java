package com.neolena.essenceindustry.blueprint;

import java.util.function.Predicate;
import net.minecraft.core.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Physical controllers remain the owners; a relative anchor describes the assembled footprint. */
public final class ControllerPlacement {
 public static BlockPos anchor(BlockEntity tile){var t=tile.getPersistentData();return tile.getBlockPos().offset(t.getIntOr("layoutOffsetX",0),t.getIntOr("layoutOffsetY",0),t.getIntOr("layoutOffsetZ",0));}
 public static void set(BlockEntity tile,BlockPos anchor){var d=anchor.subtract(tile.getBlockPos());var t=tile.getPersistentData();t.putInt("layoutOffsetX",d.getX());t.putInt("layoutOffsetY",d.getY());t.putInt("layoutOffsetZ",d.getZ());tile.setChanged();}
 public static Direction facing(BlockEntity tile,Direction fallback){int id=tile.getPersistentData().getIntOr("layoutFacing",-1);return id>=0&&id<4?Direction.from2DDataValue(id):fallback;}
 public static void setFacing(BlockEntity tile,Direction face){tile.getPersistentData().putInt("layoutFacing",face.get2DDataValue());tile.setChanged();}
 /** Discover only a complete front wall. An unfinished auto-build must never move its own origin. */
 public static boolean discoverWall(BlockEntity tile,Direction face,int width,int height,int canonicalX,Predicate<BlockState> shell){
  var level=tile.getLevel();if(level==null||level.isClientSide())return false;var actual=tile.getBlockPos();var left=face.getCounterClockWise();var right=face.getClockWise();int x=0,y=0;
  while(x<width&&loadedShell(tile,actual.relative(left,x+1),shell))x++;
  while(y<height&&loadedShell(tile,actual.below(y+1),shell))y++;
  if(x>=width||y>=height)return false;var bottom=actual.relative(left,x).below(y);
  for(int row=0;row<height;row++)for(int col=0;col<width;col++){var p=bottom.relative(right,col).above(row);if(!p.equals(actual)&&!loadedShell(tile,p,shell))return false;}
  var candidate=bottom.relative(right,canonicalX).above();if(candidate.equals(anchor(tile)))return false;set(tile,candidate);return true;
 }
 private static boolean loadedShell(BlockEntity tile,BlockPos p,Predicate<BlockState> shell){return tile.getLevel().isLoaded(p)&&shell.test(tile.getLevel().getBlockState(p));}
 private ControllerPlacement(){}
}
