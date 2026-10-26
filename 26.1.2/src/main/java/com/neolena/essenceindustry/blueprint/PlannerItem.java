package com.neolena.essenceindustry.blueprint;
import com.neolena.essenceindustry.nuclear.*;
import com.neolena.essenceindustry.array.*;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
public final class PlannerItem extends Item {
 public PlannerItem(Properties p){super(p);}
 public static BlueprintSettings settings(ItemStack s){return s.getOrDefault(MultiblockPlanner.SETTINGS.get(),BlueprintSettings.DEFAULT);}
 public static void open(ServerPlayer p,InteractionHand hand){int slot=hand==InteractionHand.MAIN_HAND?p.getInventory().getSelectedSlot():40;var tool=p.getItemInHand(hand);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new BlueprintMenu(id,inv,slot),tool.getHoverName()),b->b.writeVarInt(slot));}
 @Override public InteractionResult use(Level l,Player p,InteractionHand h){if(p instanceof ServerPlayer sp)open(sp,h);return InteractionResult.SUCCESS;}
 @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext c){return useOn(c);}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer() instanceof ServerPlayer p)open(p,c.getHand());return InteractionResult.SUCCESS;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,net.minecraft.world.item.component.TooltipDisplay d,java.util.function.Consumer<Component> out,TooltipFlag f){out.accept(Component.translatable("tooltip.essenceindustry.multiblock_planner"));var design=s.get(MultiblockPlanner.CORE_DESIGN.get());if(design!=null)out.accept(Component.translatable("gui.essenceindustry.planner.saved_core",design.width()-2,design.depth()-2,settings(s).height()-2));}
}
