package com.neolena.essenceindustry.quantum;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.Identifier;
import com.neolena.essenceindustry.mixin.QuantumAttackTickerAccessor;
import net.minecraft.world.phys.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;


import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemInstance;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Server-side combat modules, with optional attribute-based critical-strike integration. */
public final class QuantumCombat {

    private static final Identifier CRIT_CHANCE=Identifier.parse("apothic_attributes:crit_chance"), CRIT_DAMAGE=Identifier.parse("apothic_attributes:crit_damage");
    public static boolean attributeCriticals(){return BuiltInRegistries.ATTRIBUTE.get(CRIT_CHANCE).isPresent() && BuiltInRegistries.ATTRIBUTE.get(CRIT_DAMAGE).isPresent();}
    public static void addCritAttributes(ItemAttributeModifiers.Builder b,ItemInstance tool){
        if(!attributeCriticals())return;
        int chance=QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_CHANCE), damage=QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_DAMAGE);
        if(chance>0)b.add(BuiltInRegistries.ATTRIBUTE.get(CRIT_CHANCE).orElseThrow(),new AttributeModifier(EssenceIndustry.id("quantum_crit_chance"),0.1*chance,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        if(damage>0)b.add(BuiltInRegistries.ATTRIBUTE.get(CRIT_DAMAGE).orElseThrow(),new AttributeModifier(EssenceIndustry.id("quantum_crit_damage"),0.25*damage,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
    }
    public static double aoeRadius(ItemInstance tool){int n=QuantumLoadout.count(tool,QuantumModule.Kind.SWEEPING);return n==0?0:1+0.5*n;}
    private record AttackContext(ServerPlayer player,Entity target,ItemStack tool,int charge,boolean sprint){}
    private static final ThreadLocal<AttackContext> ATTACK=new ThreadLocal<>();
    private static final ThreadLocal<Boolean> BURST=ThreadLocal.withInitial(()->false);
    private static final ThreadLocal<ServerPlayer> GROUND=new ThreadLocal<>();
    /** Used only by the optional Apothic handler bridge; all normal attacks retain its actual attribute value. */
    public static double groundCriticalChance(LivingEntity owner,net.minecraft.core.Holder<Attribute> attribute,double actual){
        return GROUND.get()==owner && CRIT_CHANCE.equals(BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()))?Math.max(1,actual):actual;
    }
    /** A try/finally scope around native attack avoids retaining players after exceptions or nested mod attacks. */
    public static void attackScope(Player player,Entity target,Runnable original){
        if(BURST.get() || !(player instanceof ServerPlayer p) || aoeRadius(p.getMainHandItem())<=0){original.run();return;}
        var previous=ATTACK.get();var access=(QuantumAttackTickerAccessor)p;
        ATTACK.set(new AttackContext(p,target,p.getMainHandItem(),access.essenceindustry$getAttackTicker(),p.isSprinting()));
        try{original.run();}finally{if(previous==null)ATTACK.remove();else ATTACK.set(previous);}
    }
    public static void areaDamage(LivingDamageEvent.Post e){
        if(BURST.get() || e.getInflictedDamage()<=0 || !e.getSource().is(DamageTypes.PLAYER_ATTACK))return;
        var context=ATTACK.get();if(context==null || e.getSource().getDirectEntity()!=context.player || e.getEntity()!=context.target)return;
        var p=context.player;if(!p.isAlive() || p.getMainHandItem()!=context.tool)return;
        attackArea(p,e.getEntity().position(),e.getEntity(),context.charge,context.sprint,false);
    }
    private static boolean eligible(ServerPlayer p,LivingEntity target,Entity primary,Vec3 center,double radius){
        if(target==p || target==primary || !target.isAlive() || target.isSpectator() || target instanceof net.minecraft.world.entity.decoration.ArmorStand || target.position().distanceToSqr(center)>radius*radius)return false;
        if(p.isAlliedTo(target) || target.isAlliedTo(p) || !p.hasLineOfSight(target))return false;
        if(primary instanceof LivingEntity living && !living.hasLineOfSight(target))return false;
        if(target instanceof OwnableEntity pet && pet.getOwnerReference()!=null)return false;
        return !(target instanceof Player other) || p.canHarmPlayer(other);
    }
    private static void attackArea(ServerPlayer p,Vec3 center,Entity primary,int charge,boolean sprint,boolean ground){
        double radius=aoeRadius(p.getMainHandItem());if(radius<=0)return;
        var level=(ServerLevel)p.level();var access=(QuantumAttackTickerAccessor)p;int consumed=access.essenceindustry$getAttackTicker();boolean afterSprint=p.isSprinting();var held=p.getMainHandItem();
        var targets=level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius+1),t->eligible(p,t,primary,center,radius));
        var previousBurst=BURST.get();var previousGround=GROUND.get();BURST.set(true);if(ground)GROUND.set(p);else GROUND.remove();
        try{
            for(var target:targets){
                if(!p.isAlive() || p.level()!=level || p.getMainHandItem()!=held)break;
                if(target.level()!=level || !eligible(p,target,primary,center,radius))continue;
                access.essenceindustry$setAttackTicker(charge);p.setSprinting(sprint);
                // Every enemy goes through the same weapon, critical, enchantment and protection hooks.
                p.attack(target);
            }
        }finally{
            access.essenceindustry$setAttackTicker(consumed);p.setSprinting(afterSprint);
            BURST.set(previousBurst);if(previousGround==null)GROUND.remove();else GROUND.set(previousGround);
        }
    }
    public static void groundAttack(PlayerInteractEvent.LeftClickBlock e){
        var player=e.getEntity();var tool=player.getMainHandItem();
        if(!(tool.getItem() instanceof QuantumToolItem q)||q.staff||aoeRadius(tool)<=0)return;
        // This weapon gesture must not begin destroying the selected block, including on the client.
        e.setCanceled(true);
        if(player.level().isClientSide()){
            if(e.getAction()==PlayerInteractEvent.LeftClickBlock.Action.START && player.getAttackStrengthScale(0.5f)>=0.9f)player.resetAttackStrengthTicker();
            return;
        }
        if(!(player instanceof ServerPlayer p) || e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START || p.isSpectator() || !p.isAlive() || BURST.get())return;
        var level=(ServerLevel)p.level();var pos=e.getPos();
        if(!p.isWithinBlockInteractionRange(pos,0) || !level.getWorldBorder().isWithinBounds(pos) || !level.mayInteract(p,pos) || level.getBlockState(pos).isAir() || p.getAttackStrengthScale(0.5f)<0.9f)return;
        var face=e.getFace();var end=Vec3.atCenterOf(pos);if(face!=null)end=end.add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.499));
        var hit=level.clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),end,net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK || !hit.getBlockPos().equals(pos))return;
        int charge=((QuantumAttackTickerAccessor)p).essenceindustry$getAttackTicker();boolean sprint=p.isSprinting();
        p.resetAttackStrengthTicker(); // Empty bursts also consume cooldown; repeated START packets cannot spam full criticals.
        attackArea(p,Vec3.atCenterOf(pos).add(0,1,0),null,charge,sprint,true);
    }
    public static int critChance(ItemInstance tool){return 10*QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_CHANCE);}
    public static float critMultiplier(ItemInstance tool){return 1.5f+0.25f*QuantumLoadout.count(tool,QuantumModule.Kind.CRIT_DAMAGE);}
    public static void critical(CriticalHitEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(GROUND.get()==p){
            if(attributeCriticals()){e.setCriticalHit(false);e.setDamageMultiplier(1);return;}
            e.setCriticalHit(true);e.setDamageMultiplier(Math.max(1.5f,e.getDamageMultiplier())+0.25f*QuantumLoadout.count(p.getMainHandItem(),QuantumModule.Kind.CRIT_DAMAGE));return;
        }
        if(attributeCriticals())return;
        var tool=p.getMainHandItem();if(!(tool.getItem() instanceof QuantumToolItem q) || q.staff)return;
        int chance=critChance(tool);
        boolean generated=false;
        if(!e.isCriticalHit() && chance>0 && p.getAttackStrengthScale(0.5f)>=0.9f && (chance>=100 || p.getRandom().nextInt(100)<chance)) {
            e.setCriticalHit(true);generated=true;
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
