package com.neolena.essenceindustry.construction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
public final class BuilderItem extends Item {
 public BuilderItem(Properties p){super(p);}
 public static BuilderSettings settings(ItemStack s){return s.getOrDefault(QuantumBuilder.SETTINGS.get(),BuilderSettings.DEFAULT);}
 public static void open(ServerPlayer p,InteractionHand hand){var tool=p.getItemInHand(hand);if(!(tool.getItem() instanceof BuilderItem))return;
  int anchor=hand==InteractionHand.MAIN_HAND?p.getInventory().getSelectedSlot():40;
  p.openMenu(new SimpleMenuProvider((id,inv,pl)->new BuilderMenu(id,inv,anchor),tool.getHoverName()),b->b.writeVarInt(anchor));}
 @Override public InteractionResult use(Level l,Player p,InteractionHand hand){if(p instanceof ServerPlayer sp){var tool=p.getItemInHand(hand);var anchor=tool.get(QuantumBuilder.ANCHOR.get());if(anchor!=null&&!p.isShiftKeyDown())BuilderWork.start(sp,tool,anchor.origin().pos(),anchor.face());else open(sp,hand);}return InteractionResult.SUCCESS;}
 @Override public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();if(p==null)return InteractionResult.FAIL;var tool=c.getItemInHand();
  if(tool.has(QuantumBuilder.RECOVERY.get())){if(!c.getLevel().isClientSide())p.sendOverlayMessage(Component.translatable("gui.essenceindustry.builder.locked"));return InteractionResult.FAIL;}
  if(p instanceof ServerPlayer sp&&BuilderWork.busy(sp))return InteractionResult.FAIL;
  if(p.isShiftKeyDown()){
   if(!c.getLevel().isClientSide()){
    if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&BuilderMe.bind(c))return InteractionResult.SUCCESS;
    var state=BuilderWork.selectedState(c.getLevel().getBlockState(c.getClickedPos()));
    if(BuilderWork.safeMaterial(state)){tool.set(QuantumBuilder.MATERIAL.get(),state);p.sendOverlayMessage(Component.translatable("gui.essenceindustry.builder.sampled",state.getBlock().getName()));}
    else p.sendOverlayMessage(Component.translatable("gui.essenceindustry.builder.unsupported"));
   }return InteractionResult.SUCCESS;
  }
  if(p instanceof ServerPlayer sp)BuilderWork.start(sp,tool,c.getClickedPos(),c.getClickedFace());return InteractionResult.SUCCESS;
 }
 @Override public void appendHoverText(ItemStack stack,TooltipContext c,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> lines,TooltipFlag flag){lines.accept(Component.translatable("tooltip.essenceindustry.quantum_builder"));if(stack.has(QuantumBuilder.RECOVERY.get()))lines.accept(Component.translatable("gui.essenceindustry.builder.locked"));if(stack.has(QuantumBuilder.LINK.get()))lines.accept(Component.translatable("gui.essenceindustry.builder.linked"));}
}
