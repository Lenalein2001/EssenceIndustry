package com.neolena.essenceindustry.construction;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
public record BuilderOpen() implements CustomPacketPayload {
 public static final Type<BuilderOpen> TYPE=new Type<>(EssenceIndustry.id("builder_open"));
 public static final StreamCodec<RegistryFriendlyByteBuf,BuilderOpen> CODEC=StreamCodec.unit(new BuilderOpen());
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(TYPE,CODEC,(packet,ctx)->{if(ctx.player() instanceof ServerPlayer p&&p.mayBuild()&&p.containerMenu==p.inventoryMenu){var hand=p.getMainHandItem().getItem() instanceof BuilderItem?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;BuilderItem.open(p,hand);}});}
}
