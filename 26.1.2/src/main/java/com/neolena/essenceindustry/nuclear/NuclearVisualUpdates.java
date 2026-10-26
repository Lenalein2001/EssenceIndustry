package com.neolena.essenceindustry.nuclear;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
/** Coalesced mesh updates: fixed server budget, latest state wins, never replaces player edits. */
public final class NuclearVisualUpdates {
 private static final Map<Level,LinkedHashMap<BlockPos,BlockState>> PENDING=new WeakHashMap<>();
 public static void register(){NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post e)->{if(!e.getLevel().isClientSide())flush(e.getLevel());});}
 public static BlockState state(Level level,BlockPos pos){var current=level.getBlockState(pos);var queue=PENDING.get(level);var pending=queue==null?null:queue.get(pos);return pending!=null&&pending.is(current.getBlock())?pending:current;}
 public static void set(Level level,BlockPos pos,BlockState state){if(NuclearReactor.PORTS.contains(NuclearReactor.kind(state))){var before=level.getBlockState(pos);if(before!=state){level.setBlock(pos,state,Block.UPDATE_CLIENTS);level.invalidateCapabilities(pos);}return;}PENDING.computeIfAbsent(level,l->new LinkedHashMap<>()).put(pos.immutable(),state);}
 private static void flush(Level level){var queue=PENDING.get(level);if(queue==null)return;var it=queue.entrySet().iterator();int budget=1024;while(budget-->0&&it.hasNext()){var entry=it.next();var pos=entry.getKey();var desired=entry.getValue();it.remove();if(!level.hasChunkAt(pos))continue;var current=level.getBlockState(pos);if(current==desired||!current.is(desired.getBlock()))continue;level.setBlock(pos,desired,Block.UPDATE_CLIENTS);if(NuclearReactor.kind(desired).equals("glass")){level.scheduleTick(pos,desired.getBlock(),1);for(var d:net.minecraft.core.Direction.values()){var p=pos.relative(d);if(level.hasChunkAt(p)&&level.getBlockState(p).is(desired.getBlock()))level.scheduleTick(p,desired.getBlock(),1);}}}if(queue.isEmpty())PENDING.remove(level);}
 private NuclearVisualUpdates(){}
}
