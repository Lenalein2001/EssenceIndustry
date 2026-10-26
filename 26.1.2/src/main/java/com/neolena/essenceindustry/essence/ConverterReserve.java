package com.neolena.essenceindustry.essence;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record ConverterReserve(int menu, int tier, int amount) implements CustomPacketPayload {
    public static final Type<ConverterReserve> TYPE = new Type<>(EssenceIndustry.id("converter_reserve"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConverterReserve> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConverterReserve::menu, ByteBufCodecs.VAR_INT, ConverterReserve::tier,
            ByteBufCodecs.VAR_INT, ConverterReserve::amount, ConverterReserve::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC, (p, ctx) -> {
            var player = ctx.player();
            if (player.containerMenu instanceof EssenceConverterMenu menu && menu.containerId == p.menu && menu.stillValid(player))
                menu.setReserve(p.tier, p.amount);
        });
    }
}
