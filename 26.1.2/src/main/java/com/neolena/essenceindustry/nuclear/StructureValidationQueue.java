package com.neolena.essenceindustry.nuclear;
import java.util.*;
import net.minecraft.world.level.block.entity.BlockEntity;
/** Coalesce structural edits until the end of a server tick, independently of crafting/build pacing. */
public final class StructureValidationQueue {
 private static final Map<BlockEntity,Runnable> PENDING=new LinkedHashMap<>();
 public static void request(BlockEntity tile,Runnable validation){if(tile.getLevel()!=null&&!tile.getLevel().isClientSide())PENDING.put(tile,validation);}
 public static void drain(){var batch=new LinkedHashMap<>(PENDING);PENDING.clear();for(var e:batch.entrySet()){var t=e.getKey();if(!t.isRemoved()&&t.getLevel()!=null&&t.getLevel().hasChunkAt(t.getBlockPos()))e.getValue().run();}}
 public static void register(){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post e)->drain());net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->PENDING.clear());}
 private StructureValidationQueue(){}
}
