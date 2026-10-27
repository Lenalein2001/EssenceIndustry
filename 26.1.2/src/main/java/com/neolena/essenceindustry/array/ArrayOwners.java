package com.neolena.essenceindustry.array;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;

/** Chunk-local owner index shared by collision queries and interactions. No world traversal or chunk loads. */
final class ArrayOwners {
 private record Bounds(int width,int depth,int height,net.minecraft.core.Direction face,BlockPos anchor,Set<Long> chunks){}
 private static final Map<Level,Map<Long,Set<ArrayTile>>> INDEX=new WeakHashMap<>();
 private static final Map<ArrayTile,Bounds> BOUNDS=new WeakHashMap<>();
 static synchronized void update(ArrayTile tile){
  var level=tile.getLevel();if(level==null||tile.isRemoved())return;var face=ArrayLayout.facing(tile.getBlockState());var old=BOUNDS.get(tile);
  if(old!=null&&old.width==tile.width()&&old.depth==tile.depth()&&old.height==tile.height()&&old.face==face&&old.anchor.equals(tile.layoutAnchor()))return;
  if(!level.hasChunkAt(tile.getBlockPos())||level.getBlockEntity(tile.getBlockPos())!=tile)return;
  remove(tile);var first=tile.position(0,0,0);var end=tile.position(tile.width()-1,tile.height()-1,tile.depth()-1);var chunks=new HashSet<Long>();
  var index=INDEX.computeIfAbsent(level,l->new HashMap<>());
  for(int z=Math.min(first.getZ(),end.getZ())>>4;z<=(Math.max(first.getZ(),end.getZ())>>4);z++)for(int x=Math.min(first.getX(),end.getX())>>4;x<=(Math.max(first.getX(),end.getX())>>4);x++){
   long key=ChunkPos.pack(x,z);chunks.add(key);index.computeIfAbsent(key,k->Collections.newSetFromMap(new IdentityHashMap<>())).add(tile);
  }
  BOUNDS.put(tile,new Bounds(tile.width(),tile.depth(),tile.height(),face,tile.layoutAnchor(),Set.copyOf(chunks)));
 }
 static synchronized void remove(ArrayTile tile){
  var old=BOUNDS.remove(tile);var index=INDEX.get(tile.getLevel());if(old==null||index==null)return;
  for(long key:old.chunks){var group=index.get(key);if(group!=null){group.remove(tile);if(group.isEmpty())index.remove(key);}}
  if(index.isEmpty())INDEX.remove(tile.getLevel());
 }
 static synchronized ArrayTile owner(Level level,BlockPos pos){
  ArrayTile fallback=null;for(var tile:candidates(level,pos))if(!tile.isRemoved()&&tile.contains(pos)){if(tile.formed())return tile;fallback=tile;}return fallback;
 }
 static synchronized List<ArrayTile> at(Level level,BlockPos pos){return candidates(level,pos).stream().filter(t->!t.isRemoved()&&t.contains(pos)).toList();}
 static synchronized List<ArrayTile> inChunk(Level level,ChunkPos chunk){return List.copyOf(INDEX.getOrDefault(level,Map.of()).getOrDefault(ChunkPos.pack(chunk.x(),chunk.z()),Set.of()));}
 private static Set<ArrayTile> candidates(Level level,BlockPos pos){var index=INDEX.get(level);return index==null?Set.of():index.getOrDefault(ChunkPos.pack(pos),Set.of());}
 static synchronized void clear(){INDEX.clear();BOUNDS.clear();}
 private ArrayOwners(){}
}
