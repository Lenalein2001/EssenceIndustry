package com.neolena.essenceindustry.quantum;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Server-wide bounded work; native player destruction owns protection events, loot and XP. */
public final class QuantumMining {
    public static final int GLOBAL_BUDGET=16, PLAYER_BUDGET=8;
    private record Job(ServerPlayer player,ServerLevel level,ItemStack tool,ItemStack snapshot,BlockState original,BlockPos center,ArrayDeque<BlockPos> blocks) {}
    private static final LinkedHashMap<UUID,Job> JOBS=new LinkedHashMap<>();
    private static boolean breaking;
    public static List<BlockPos> area(BlockPos center,Direction.Axis normal,int radius) {
        var result=new ArrayList<BlockPos>();
        for(int a=-radius;a<=radius;a++)for(int b=-radius;b<=radius;b++)if(a!=0||b!=0)result.add(switch(normal){case Y->center.offset(a,0,b);case X->center.offset(0,a,b);case Z->center.offset(a,b,0);});
        result.sort(Comparator.comparingDouble(center::distSqr));return result;
    }
    public static void enqueue(ServerPlayer p,ItemStack tool,BlockPos pos,BlockState state) {
        if(breaking || p.isShiftKeyDown() || !p.gameMode.isSurvival() || p.isSpectator() || !p.mayBuild() || !QuantumToolItem.mineable(state))return;
        int r=QuantumLoadout.count(tool,QuantumModule.Kind.AREA); if(r==0 || state.hasBlockEntity() || state.getDestroySpeed(p.level(),pos)<0 && state.getDestroyProgress(p,p.level(),pos)<=0)return;
        JOBS.put(p.getUUID(),new Job(p,p.level(),tool,tool.copy(),state,pos.immutable(),new ArrayDeque<>(area(pos,p.getNearestViewDirection().getAxis(),r))));
    }
    public static int pending() {return JOBS.values().stream().mapToInt(j->j.blocks.size()).sum();}
    public static void tick(ServerTickEvent.Post event) {
        int remaining=GLOBAL_BUDGET;
        // Round robin: a full queue cannot monopolize the first budget positions every tick.
        int visits=JOBS.size();
        while(remaining>0 && visits-->0 && !JOBS.isEmpty()) {
            var entry=JOBS.entrySet().iterator().next();var id=entry.getKey();var j=entry.getValue();JOBS.remove(id);
            var p=j.player;
            if(p.isRemoved() || !p.isAlive() || p.level()!=j.level || p.getMainHandItem()!=j.tool || !ItemStack.isSameItemSameComponents(j.tool,j.snapshot) || p.isShiftKeyDown() || !p.gameMode.isSurvival() || !p.mayBuild())continue;
            int work=Math.min(PLAYER_BUDGET,remaining);
            while(work-->0 && !j.blocks.isEmpty()) {
                remaining--;var pos=j.blocks.removeFirst();
                if(!j.level.hasChunkAt(pos) || !p.isWithinBlockInteractionRange(pos,3.0) || !j.level.mayInteract(p,pos))continue;
                var state=j.level.getBlockState(pos);float hardness=state.getDestroySpeed(j.level,pos);
                if(state.isAir() || state.hasBlockEntity() || hardness<0 && state.getDestroyProgress(p,j.level,pos)<=0 || hardness>Math.max(1,j.original.getDestroySpeed(j.level,j.center))*4 || !QuantumToolItem.mineable(state) || !j.tool.isCorrectToolForDrops(state))continue;
                breaking=true;try {p.gameMode.destroyBlock(pos);} finally {breaking=false;}
            }
            if(!j.blocks.isEmpty())JOBS.put(id,j);
        }
    }
    public static void stopped(ServerStoppedEvent event) {JOBS.clear();breaking=false;}
    private QuantumMining() {}
}
