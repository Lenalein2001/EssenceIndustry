package com.neolena.essenceindustry.compat.ae2.pattern;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record PatternManagerSearch(int menu, String query) implements CustomPacketPayload {
    public static final Type<PatternManagerSearch> TYPE = new Type<>(EssenceIndustry.id("essence_pattern_search"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternManagerSearch> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PatternManagerSearch::menu, ByteBufCodecs.stringUtf8(64), PatternManagerSearch::query, PatternManagerSearch::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    static void register(RegisterPayloadHandlersEvent event) { event.registrar("1").playToServer(TYPE, CODEC, (p, ctx) -> {
        var player = ctx.player(); if (player.containerMenu instanceof PatternManagerMenu menu && menu.containerId == p.menu && menu.stillValid(player)) menu.filter(p.query);
    }); }
}
