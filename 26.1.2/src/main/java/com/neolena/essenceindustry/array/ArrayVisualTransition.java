package com.neolena.essenceindustry.array;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
/** Forming/disassembly is bounded globally. No extra ticker is attached to construction blocks. */
final class ArrayVisualTransition {
 private record Footprint(ServerLevel level,BlockPos controller,BlockPos anchor,Direction facing,int width,int depth,int height){ }
 private static final class Change {
  final Footprint footprint;final boolean formed;int cursor;
  Change(Footprint footprint,boolean formed){this.footprint=footprint;this.formed=formed;}
 }
 // Latest target wins for this exact footprint, even if a previous transition was
 // partly applied. Old resized/rotated footprints retain their separate cleanup.
 private static final LinkedHashMap<Footprint,Change> QUEUE=new LinkedHashMap<>();
 private static void enqueue(ArrayTile tile,Direction face,boolean formed){if(tile.getLevel() instanceof ServerLevel level){var key=new Footprint(level,tile.getBlockPos().immutable(),tile.layoutAnchor(),face,tile.width(),tile.depth(),tile.height());QUEUE.put(key,new Change(key,formed));}}
 static void enqueue(ArrayTile tile,boolean formed){enqueue(tile,ArrayLayout.facing(tile.getBlockState()),formed);}
 static void enqueueOldOrientation(ArrayTile tile,Direction oldFace){enqueue(tile,oldFace,false);}
 static void clear(){QUEUE.clear();}
 static void tick(){int budget=512;while(budget>0&&!QUEUE.isEmpty()){
  var iterator=QUEUE.entrySet().iterator();var pending=iterator.next();var task=pending.getValue();var change=task.footprint;iterator.remove();int total=change.width*change.depth*change.height;
  var tile=change.level.isLoaded(change.controller)?change.level.getBlockEntity(change.controller):null;
  boolean form=task.formed&&tile instanceof ArrayTile owner&&owner.formed()&&owner.layoutAnchor().equals(change.anchor)&&owner.width()==change.width&&owner.depth()==change.depth&&owner.height()==change.height&&ArrayLayout.facing(owner.getBlockState())==change.facing;
  // Rotate small slices so a large old cleanup cannot hold up another Array.
  int slice=Math.min(64,budget);
  while(task.cursor<total&&slice-->0){budget--;int i=task.cursor++;int x=i%change.width,z=(i/change.width)%change.depth,y=i/(change.width*change.depth);var pos=ArrayLayout.position(change.anchor,change.facing,x,y,z);
   if(!change.level.isLoaded(pos))continue;var state=change.level.getBlockState(pos);if(!state.hasProperty(ArrayLayout.FORMED))continue;
   // A stale formation task cannot hide a broken or newly invalid structure.
   boolean formed=form;
   // A queued reset must not undo a newer valid structure, including after a resize or rotation.
   if(!formed){var current=ArrayOwners.owner(change.level,pos);if(current!=null&&current.formed())continue;}
   var updated=state.setValue(ArrayLayout.FORMED,formed);
   if(formed&&state.getBlock() instanceof ArrayConnectorBlock)updated=updated.setValue(ArrayConnectorBlock.FACING,ArrayLayout.surface(change.facing,x,y,z,change.width,change.depth,change.height));
   if(state.hasProperty(ArrayLayout.PART))updated=updated.setValue(ArrayLayout.PART,!formed?0:y==0?1:y==change.height-1?2:state.is(ProcessingArray.RACK.get())?(y==1?3:6):ArrayLayout.corner(x,z,change.width,change.depth)?5:4);
   if(state.is(ProcessingArray.RACK.get()))updated=updated.setValue(ArrayLayout.FACING,change.facing);
   if(state.hasProperty(ArrayLayout.FACING)&&ArrayLayout.shell(x,y,z,change.width,change.depth,change.height)&&!pos.equals(change.controller)&&!(state.getBlock() instanceof ArrayConnectorBlock))updated=updated.setValue(ArrayLayout.FACING,ArrayLayout.corner(x,z,change.width,change.depth)?ArrayLayout.cornerFacing(change.facing,x,z,change.width,change.depth):ArrayLayout.outward(change.facing,x,y,z,change.width,change.depth));
   if(updated!=state)change.level.setBlock(pos,updated,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
  }
  // A callback may have queued a newer target while this slice was applied.
  if(task.cursor<total)QUEUE.putIfAbsent(change,task);
 }}
}
