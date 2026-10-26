package com.neolena.essenceindustry.quantum;

import com.neolena.essenceindustry.EssenceIndustry;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.*;

public final class QuantumToolItem extends Item {
    public final boolean staff;
    public QuantumToolItem(Properties p,boolean staff) {
        super(properties(p,staff));
        this.staff=staff;
    }
    private static Properties properties(Properties p,boolean staff) {
        p.stacksTo(1).enchantable(15).component(DataComponents.WEAPON,new Weapon(0));
        if(staff) {
            var lookup=net.minecraft.core.registries.BuiltInRegistries.acquireBootstrapRegistrationLookup(net.minecraft.core.registries.BuiltInRegistries.BLOCK);
            var blocks=lookup.getOrThrow(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,EssenceIndustry.id("quantum_mineable")));
            p.component(DataComponents.TOOL,new Tool(java.util.List.of(Tool.Rule.minesAndDrops(blocks,12)),1,0,true));
        }
        return p;
    }
    public static boolean mineable(BlockState s) {
        // Some ores (including AllTheModium's) have negative base hardness but
        // provide their own player mining progress. Do not mistake those for bedrock.
        return !s.isAir() && (s.getBlock().defaultDestroyTime() >= 0 || s.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || s.is(BlockTags.MINEABLE_WITH_AXE) || s.is(BlockTags.MINEABLE_WITH_SHOVEL) || s.is(BlockTags.MINEABLE_WITH_HOE));
    }
    @Override public boolean isCorrectToolForDrops(ItemStack s,BlockState b) { return staff ? mineable(b) : b.is(Blocks.COBWEB); }
    @Override public float getDestroySpeed(ItemStack s,BlockState b) {
        return staff && mineable(b) ? 12f*(1f+0.5f*QuantumLoadout.count(s,QuantumModule.Kind.MINING_SPEED)) : !staff && b.is(Blocks.COBWEB)?15f:1f;
    }
    @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        var b=ItemAttributeModifiers.builder();
        b.add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,(staff?7:28)+4*QuantumLoadout.count(stack,QuantumModule.Kind.DAMAGE)+32*QuantumLoadout.count(stack,QuantumModule.Kind.DAMAGE_AMPLIFIER),AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        b.add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,(staff?-3:-2.4)+0.2*QuantumLoadout.count(stack,QuantumModule.Kind.ATTACK_SPEED),AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        int reach=QuantumLoadout.count(stack,QuantumModule.Kind.REACH);
        if(reach>0)b.add(Attributes.ENTITY_INTERACTION_RANGE,new AttributeModifier(EssenceIndustry.id("quantum_reach"),0.5*reach,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        int knockback=QuantumLoadout.count(stack,QuantumModule.Kind.KNOCKBACK);
        if(knockback>0)b.add(Attributes.ATTACK_KNOCKBACK,new AttributeModifier(EssenceIndustry.id("quantum_knockback"),0.25*knockback,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        return b.build();
    }
    @Override public boolean canPerformAction(ItemInstance s,ItemAbility a) {
        return staff ? ItemAbilities.DEFAULT_AXE_ACTIONS.contains(a)||ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(a) : a==ItemAbilities.SWORD_SWEEP;
    }
    private static int effective(ItemInstance s,Holder<Enchantment> e) {
        int n=s.getTagEnchantments().getLevel(e);
        if(e.is(Enchantments.FORTUNE))n=Math.max(n,2*QuantumLoadout.count(s,QuantumModule.Kind.FORTUNE));
        if(e.is(Enchantments.SILK_TOUCH))n=Math.max(n,QuantumLoadout.count(s,QuantumModule.Kind.SILK_TOUCH));
        if(e.is(Enchantments.SWEEPING_EDGE))n=Math.max(n,QuantumLoadout.count(s,QuantumModule.Kind.SWEEPING));
        if(e.is(Enchantments.LOOTING))n=Math.max(n,QuantumLoadout.count(s,QuantumModule.Kind.LOOTING));
        return n;
    }
    @Override public int getEnchantmentLevel(ItemInstance s,Holder<Enchantment> e) { return effective(s,e); }
    @Override public ItemEnchantments getAllEnchantments(ItemStack s,HolderLookup.RegistryLookup<Enchantment> lookup) {
        var result=new ItemEnchantments.Mutable(s.getTagEnchantments());
        for(var key:java.util.List.of(Enchantments.FORTUNE,Enchantments.SILK_TOUCH,Enchantments.SWEEPING_EDGE,Enchantments.LOOTING)) { var e=lookup.getOrThrow(key);result.set(e,effective(s,e)); }
        return result.toImmutable();
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(!player.isShiftKeyDown())return InteractionResult.PASS;
        var tool=player.getItemInHand(hand);
        if(!QuantumLoadout.valid(tool)) {
            if(!level.isClientSide())player.sendOverlayMessage(Component.translatable("gui.essenceindustry.quantum_tool.locked"));
            return InteractionResult.FAIL;
        }
        if(player instanceof ServerPlayer server) {
            int anchor=hand==InteractionHand.MAIN_HAND?player.getInventory().getSelectedSlot():40;
            server.openMenu(new SimpleMenuProvider((id,inv,p)->new QuantumToolMenu(id,inv,anchor),tool.getHoverName()),buf->buf.writeVarInt(anchor));
        }
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult useOn(UseOnContext c) {
        var p=c.getPlayer(); if(p==null)return InteractionResult.PASS;
        if(p.isShiftKeyDown())return use(c.getLevel(),p,c.getHand());
        if(!staff)return InteractionResult.PASS;
        var l=c.getLevel();var pos=c.getClickedPos();var old=l.getBlockState(pos);
        if(!p.mayBuild() || !l.mayInteract(p,pos))return InteractionResult.FAIL;
        for(var a:java.util.List.of(ItemAbilities.AXE_STRIP,ItemAbilities.AXE_SCRAPE,ItemAbilities.AXE_WAX_OFF,ItemAbilities.SHOVEL_FLATTEN,ItemAbilities.SHOVEL_DOUSE)) {
            if(a==ItemAbilities.SHOVEL_FLATTEN && (c.getClickedFace()==Direction.DOWN || !l.getBlockState(pos.above()).isAir()))continue;
            var next=old.getToolModifiedState(c,a,false); if(next==null)continue;
            if(!l.isClientSide()) {
                l.setBlock(pos,next,11);l.gameEvent(GameEvent.BLOCK_CHANGE,pos,GameEvent.Context.of(p,next));
                SoundEvent sound=a==ItemAbilities.AXE_STRIP?SoundEvents.AXE_STRIP:a==ItemAbilities.AXE_SCRAPE?SoundEvents.AXE_SCRAPE:a==ItemAbilities.AXE_WAX_OFF?SoundEvents.AXE_WAX_OFF:a==ItemAbilities.SHOVEL_DOUSE?SoundEvents.FIRE_EXTINGUISH:SoundEvents.SHOVEL_FLATTEN;
                l.playSound(null,pos,sound,SoundSource.BLOCKS,1,1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    @Override public boolean mineBlock(ItemStack tool,Level l,BlockState state,BlockPos pos,LivingEntity miner) {
        if(staff && miner instanceof ServerPlayer p)QuantumMining.enqueue(p,tool,pos,state);
        return true;
    }
    @Override public void appendHoverText(ItemStack s,TooltipContext c,TooltipDisplay d,Consumer<Component> lines,TooltipFlag f) {
        lines.accept(Component.translatable("tooltip.essenceindustry.quantum_"+(staff?"staff":"sword")).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.essenceindustry.quantum_open").withStyle(ChatFormatting.AQUA));
        if(!QuantumLoadout.valid(s))lines.accept(Component.translatable("gui.essenceindustry.quantum_tool.locked").withStyle(ChatFormatting.YELLOW));
    }
}
