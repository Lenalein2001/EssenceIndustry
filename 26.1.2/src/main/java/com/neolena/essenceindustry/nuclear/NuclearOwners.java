package com.neolena.essenceindustry.nuclear;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
/** Chunk-local controller index. Queries never load a chunk or retain an unloaded controller. */
public final class NuclearOwners {
 private record Bounds(BlockPos min,BlockPos max){boolean has(BlockPos p){return p.getX()>=min.getX()&&p.getX()<=max.getX()&&p.getY()>=min.getY()&&p.getY()<=max.getY()&&p.getZ()>=min.getZ()&&p.getZ()<=max.getZ();}boolean intersects(Bounds b){return min.getX()<=b.max.getX()&&max.getX()>=b.min.getX()&&min.getY()<=b.max.getY()&&max.getY()>=b.min.getY()&&min.getZ()<=b.max.getZ()&&max.getZ()>=b.min.getZ();}}
 private static final Map<Level,Map<BlockPos,Bounds>> BOUNDS=new WeakHashMap<>();
 private static final Map<Level,Map<Long,Set<BlockPos>>> INDEX=new WeakHashMap<>();
 public static synchronized void register(Level l,BlockPos c,BlockPos min,BlockPos max){var b=new Bounds(min.immutable(),max.immutable());if(b.equals(BOUNDS.getOrDefault(l,Map.of()).get(c)))return;unregister(l,c);BOUNDS.computeIfAbsent(l,k->new HashMap<>()).put(c.immutable(),b);var i=INDEX.computeIfAbsent(l,k->new HashMap<>());for(int x=min.getX()>>4;x<=max.getX()>>4;x++)for(int z=min.getZ()>>4;z<=max.getZ()>>4;z++)i.computeIfAbsent(ChunkPos.pack(x,z),k->new HashSet<>()).add(c.immutable());}
 public static synchronized void unregister(Level l,BlockPos c){var bs=BOUNDS.get(l);var b=bs==null?null:bs.remove(c);if(b==null)return;var i=INDEX.get(l);if(i!=null)for(int x=b.min.getX()>>4;x<=b.max.getX()>>4;x++)for(int z=b.min.getZ()>>4;z<=b.max.getZ()>>4;z++){long key=ChunkPos.pack(x,z);var set=i.get(key);if(set!=null){set.remove(c);if(set.isEmpty())i.remove(key);}}}
 private static NuclearHost host(Level l,BlockPos c){return l.hasChunkAt(c)&&l.getBlockEntity(c) instanceof NuclearHost h?h:null;}
 public static synchronized NuclearHost at(Level l,BlockPos p){NuclearHost fallback=null;for(var c:INDEX.getOrDefault(l,Map.of()).getOrDefault(ChunkPos.pack(p),Set.of())){var b=BOUNDS.get(l).get(c);if(b!=null&&b.has(p)){var h=host(l,c);if(h!=null){if(h.formed())return h;fallback=h;}}}return fallback;}
 public static synchronized void changed(Level l,BlockPos p){for(var c:List.copyOf(INDEX.getOrDefault(l,Map.of()).getOrDefault(ChunkPos.pack(p),Set.of()))){var h=host(l,c);if(h!=null&&h.contains(p))h.partChanged(p);}}
 public static synchronized boolean conflict(Level l,BlockPos c,BlockPos min,BlockPos max){var b=new Bounds(min,max);for(var e:BOUNDS.getOrDefault(l,Map.of()).entrySet())if(!e.getKey().equals(c)&&e.getValue().intersects(b)){var h=host(l,e.getKey());if(h!=null&&h.formed())return true;}return false;}
 private NuclearOwners(){}
}
