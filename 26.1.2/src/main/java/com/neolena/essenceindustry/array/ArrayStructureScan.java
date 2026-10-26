package com.neolena.essenceindustry.array;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Resumable structural validation. Never loads chunks and never owns machine inventories. */
final class ArrayStructureScan {
 final int width,depth,height;
 final List<BlockPos> ports=new ArrayList<>(),windows=new ArrayList<>();
 final java.util.BitSet rackMask=new java.util.BitSet();
 int cursor,racks,hatches,x,y,z;
 String problem="";
 private final Level level;
 private final BlockPos controller,origin;
 private final net.minecraft.core.Direction facing;
 ArrayStructureScan(Level level,BlockPos controller,int width,int depth){this(level,controller,width,depth,5);}
 ArrayStructureScan(Level level,BlockPos controller,int width,int depth,int height){
  this.facing=ArrayLayout.facing(level.getBlockState(controller));this.level=level;this.controller=controller;this.origin=level.getBlockEntity(controller) instanceof ArrayTile tile?tile.layoutAnchor():controller;this.width=width;this.depth=depth;this.height=height;
  if(width>ArrayConfig.maxWidth()||depth>ArrayConfig.maxDepth()||height>ArrayConfig.maxHeight())problem="size";
 }
 boolean done(){return !problem.isEmpty()||cursor>=width*depth*height;}
 /** Returns actual visited cells, so failed/short scans refund unused global budget. */
 int advance(int allowance){
  int used=0;
  while(!done()&&used<allowance){
   int index=cursor++;used++;x=index%width;z=(index/width)%depth;y=index/(width*depth);
   var pos=ArrayLayout.position(origin,facing,x,y,z);
   if(!level.isLoaded(pos)){problem="unloaded";break;}
   if(pos.equals(controller))continue;
   var block=level.getBlockState(pos).getBlock();
   if(block==ProcessingArray.PORT.get()||block==ProcessingArray.HATCH.get())ports.add(pos.immutable());
   boolean shell=ArrayLayout.shell(x,y,z,width,depth,height);
   boolean socket=ArrayLayout.socket(x,y,z,width,depth,height);
   if(shell){
    if(block==ProcessingArray.GLASS.get()&&z!=0&&ArrayHullShapes.window(new BlockPos(x,y,z),width,depth,height))windows.add(new BlockPos(x,y,z));
    if(block==ProcessingArray.HATCH.get())hatches++;
    else if(block!=ProcessingArray.CASING.get()&&block!=ProcessingArray.GLASS.get()&&block!=ProcessingArray.PORT.get())problem="shell";
   }else if(socket){
    if(block==ProcessingArray.RACK.get()){
     if(y%2==1){if(level.getBlockState(pos.above()).getBlock()!=ProcessingArray.RACK.get()){problem="rack";y++;}else {racks++;rackMask.set(((y-1)/2)*ArrayBayLayout.columns(width)*ArrayBayLayout.columns(depth)+(z/2-1)*ArrayBayLayout.columns(width)+(x/2-1));}}
     else if(level.getBlockState(pos.below()).getBlock()!=ProcessingArray.RACK.get())problem="rack";
    }else if(!level.getBlockState(pos).isAir())problem="interior";
    else if(y%2==0&&level.getBlockState(pos.below()).getBlock()==ProcessingArray.RACK.get())problem="rack";
   }else if(!level.getBlockState(pos).isAir())problem="interior";
  }
  return used;
 }
}
