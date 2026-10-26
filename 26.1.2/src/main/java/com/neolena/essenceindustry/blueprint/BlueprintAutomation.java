package com.neolena.essenceindustry.blueprint;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Loaded controllers own construction jobs; menus are only editors of those jobs. */
public final class BlueprintAutomation {
 private static final Map<BlockEntity,BlueprintWork> JOBS=new LinkedHashMap<>();
 private static final Map<Level,Map<Long,Set<BlueprintWork>>> WATCHES=new WeakHashMap<>();
 private static int cursor;
 public static BlueprintWork open(ServerPlayer player,net.minecraft.world.item.ItemStack bank,BlueprintMenu menu,BlockEntity host){
  var work=JOBS.get(host);
  if(work!=null&&work.attach(player,menu))return work;
  work=new BlueprintWork(player,bank,menu);if(work.claim()){JOBS.put(host,work);work.reopen();}return work;
 }
 public static void loaded(BlockEntity host){
  if(!(host.getLevel() instanceof ServerLevel level)||host.isRemoved()||level.getBlockEntity(host.getBlockPos())!=host)return;
  if(net.neoforged.fml.ModList.get().isLoaded("ae2"))BlueprintNode.start(host);
  if(!host.getPersistentData().contains("constructionPlan")||JOBS.containsKey(host))return;
  var raw=host.getPersistentData().getString("constructionOwner").orElse("");if(raw.isEmpty())return;
  boolean resume=host.getPersistentData().getBoolean("constructionRunning").orElse(false),paused=host.getPersistentData().getBoolean("constructionPaused").orElse(false);
  try{var actor=new net.neoforged.neoforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(UUID.fromString(raw),host.getPersistentData().getString("constructionOwnerName").orElse("Builder")));actor.setPos(host.getBlockPos().getCenter());
   var work=new BlueprintWork(actor,BlueprintStorage.get(host),null);if(work.claim()){JOBS.put(host,work);work.reopen();work.restoreAutomation(resume,paused);}
  }catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BlueprintAutomation.class).error("Cannot resume construction at {}",host.getBlockPos(),ex);}
 }
 public static void unloaded(BlockEntity host){var work=JOBS.remove(host);if(work!=null)work.shutdown();if(net.neoforged.fml.ModList.get().isLoaded("ae2"))BlueprintNode.stop(host);}
 static void watch(BlueprintWork work,Level level,BlockPos min,BlockPos max){unwatch(work);var index=WATCHES.computeIfAbsent(level,k->new HashMap<>());for(int x=min.getX()>>4;x<=max.getX()>>4;x++)for(int z=min.getZ()>>4;z<=max.getZ()>>4;z++)index.computeIfAbsent(ChunkPos.pack(x,z),k->new HashSet<>()).add(work);}
 static void unwatch(BlueprintWork work){for(var index:WATCHES.values()){index.values().removeIf(set->{set.remove(work);return set.isEmpty();});}}
 public static void changed(Level level,BlockPos pos,BlockState before,BlockState after){if(level.isClientSide()||before==null||BlueprintWork.matches(before,after))return;var set=WATCHES.getOrDefault(level,Map.of()).get(ChunkPos.pack(pos));if(set!=null)for(var work:List.copyOf(set))work.worldChanged(pos);}
 public static void tick(){var jobs=List.copyOf(JOBS.values());int checks=2048,blocks=32;for(int n=0;n<jobs.size();n++){var work=jobs.get(Math.floorMod(cursor+n,jobs.size()));var used=work.tick(Math.min(256,checks),Math.min(8,blocks));checks-=used[0];blocks-=used[1];if(used[0]+used[1]>0)work.persist();}if(!jobs.isEmpty())cursor=Math.floorMod(cursor+1,jobs.size());}
 public static void clear(){for(var host:List.copyOf(JOBS.keySet()))unloaded(host);WATCHES.clear();cursor=0;}
 private BlueprintAutomation(){}
}
