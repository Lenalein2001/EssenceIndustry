package com.neolena.essenceindustry.construction;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
/** Small server-validated controls. The client never supplies an anchor coordinate. */
public record BuilderAction(int menuId,int action,int value) implements CustomPacketPayload {
 public static final int RANGE=0,SHRINK=1,GROW=2,ANCHOR=3,ROTATE=4,MODE=5,UNDO=6;
 public static final Type<BuilderAction> TYPE=new Type<>(EssenceIndustry.id("builder_action"));
 public static final StreamCodec<RegistryFriendlyByteBuf,BuilderAction> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BuilderAction::menuId,ByteBufCodecs.VAR_INT,BuilderAction::action,ByteBufCodecs.VAR_INT,BuilderAction::value,BuilderAction::new);
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(TYPE,CODEC,(packet,ctx)->{if(ctx.player() instanceof ServerPlayer p)packet.apply(p);});}
 public boolean apply(ServerPlayer p){
  if(!p.isAlive()||!p.mayBuild()||p.isSpectator()||action<0||action>12||BuilderWork.busy(p))return false;
  ItemStack tool;
  if(menuId>=0){if(!(p.containerMenu instanceof BuilderMenu menu)||menu.containerId!=menuId||!menu.stillValid(p)||action!=RANGE&&action!=ANCHOR)return false;tool=menu.tool();}
  else {if(p.containerMenu!=p.inventoryMenu)return false;tool=p.getMainHandItem().getItem() instanceof BuilderItem?p.getMainHandItem():p.getOffhandItem();}
  if(!(tool.getItem() instanceof BuilderItem)||tool.has(QuantumBuilder.RECOVERY.get()))return false;
  var settings=BuilderItem.settings(tool);
  if(action==RANGE){if(value<1||value>32)return false;tool.set(QuantumBuilder.SETTINGS.get(),settings.range(value));}
  else if(action==SHRINK||action==GROW)tool.set(QuantumBuilder.SETTINGS.get(),settings.resize(action==GROW?1:-1));
  else if(action==ANCHOR){
   if(tool.has(QuantumBuilder.ANCHOR.get()))tool.remove(QuantumBuilder.ANCHOR.get());
   else {if(menuId>=0)return false;var hit=p.pick(p.blockInteractionRange(),1,false);if(!(hit instanceof BlockHitResult block)||hit.getType()!=HitResult.Type.BLOCK||!p.level().hasChunkAt(block.getBlockPos())||!p.level().mayInteract(p,block.getBlockPos()))return false;
    tool.set(QuantumBuilder.ANCHOR.get(),new BuilderAnchor(GlobalPos.of(p.level().dimension(),block.getBlockPos().immutable()),block.getDirection(),p.getDirection()));}
   p.sendOverlayMessage(Component.translatable("gui.essenceindustry.builder."+(tool.has(QuantumBuilder.ANCHOR.get())?"anchored":"unanchored")));
  }else if(action==UNDO)BuilderWork.undo(p,tool);
  else tool.set(QuantumBuilder.SETTINGS.get(),settings.change(action==ROTATE?7:action==MODE?0:action-6));
  p.getInventory().setChanged();p.containerMenu.broadcastChanges();return true;
 }
}
