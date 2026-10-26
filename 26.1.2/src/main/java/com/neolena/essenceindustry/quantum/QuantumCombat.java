package com.neolena.essenceindustry.quantum;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemInstance;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Native event hooks only: no ticking entities, inventory scans or independent damage strikes. */
public final class QuantumCombat {
    public static int critChance(ItemInstance tool){return 10*QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_CHANCE);}
    public static float critMultiplier(ItemInstance tool){return 1.5f+0.25f*QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_DAMAGE);}
    public static void critical(CriticalHitEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var tool=p.getMainHandItem();if(!(tool.getItem() instanceof QuantumToolItem q) || q.staff)return;
        int chance=critChance(tool);
        boolean generated=false;
        if(!e.isCriticalHit() && chance>0 && p.getAttackStrengthScale(0.5f)>=0.9f && (chance>=100 || p.getRandom().nextInt(100)<chance)) {
            e.setCriticalHit(true);generated=true;
            // A module proc should not silently disable grounded sweeping builds.
            if(!e.isVanillaCritical())e.setDisableSweep(false);
        }
        int damage=QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_DAMAGE);
        if(e.isCriticalHit() && (generated || damage>0))e.setDamageMultiplier((generated?Math.max(1.5f,e.getDamageMultiplier()):e.getDamageMultiplier())+0.25f*damage);
    }
    public static void lifeSteal(LivingDamageEvent.Post e) {
        if(!(e.getSource().getDirectEntity() instanceof ServerPlayer p) || e.getSource().getEntity()!=p || !e.getSource().is(DamageTypes.PLAYER_ATTACK) || e.getEntity()==p || !p.isAlive())return;
        var tool=p.getMainHandItem();if(!(tool.getItem() instanceof QuantumToolItem q) || q.staff)return;
        int n=QuantumLoadout.count(tool,QuantumModule.Kind.LIFE_STEAL);float lost=e.getHealthDamage();
        if(n>0 && Float.isFinite(lost) && lost>0)p.heal(Math.min(4f,lost*0.01f*n));
    }
    public static void experience(BlockDropsEvent e) {
        if(e.isCanceled() || !(e.getTool().getItem() instanceof QuantumToolItem q) || !q.staff)return;
        int count=QuantumLoadout.count(e.getTool(),QuantumModule.Kind.EXPERIENCE), xp=e.getDroppedExperience();
        if(count==0 || xp<=0)return;
        long bonus=Math.min(1000L,(long)xp*count/4);
        e.setDroppedExperience((int)Math.min(Integer.MAX_VALUE,(long)xp+bonus));
    }
    private QuantumCombat() {}
}
