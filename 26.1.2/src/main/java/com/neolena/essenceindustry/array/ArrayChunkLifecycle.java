package com.neolena.essenceindustry.array;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Chunk loading may precede FULL promotion; defer all world access to the server tick. */
final class ArrayChunkLifecycle {
 private record Changed(ServerLevel level,ChunkPos chunk){}
 private static final Set<Changed> PENDING=ConcurrentHashMap.newKeySet();
 static void register(){
  NeoForge.EVENT_BUS.addListener((ChunkEvent.Load event)->queue(event));
  NeoForge.EVENT_BUS.addListener((ChunkEvent.Unload event)->queue(event));
  NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event)->{
   for(var change:PENDING.toArray(Changed[]::new))if(change.level().getServer()==event.getServer()&&PENDING.remove(change))ArrayTile.chunkChanged(change.level(),change.chunk());
  });
  NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event)->PENDING.removeIf(change->change.level().getServer()==event.getServer()));
 }
 private static void queue(ChunkEvent<?> event){if(event.getLevel() instanceof ServerLevel level)PENDING.add(new Changed(level,event.getChunk().getPos()));}
 private ArrayChunkLifecycle(){}
}
