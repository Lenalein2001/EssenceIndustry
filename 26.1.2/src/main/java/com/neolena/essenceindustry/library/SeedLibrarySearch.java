package com.neolena.essenceindustry.library;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record SeedLibrarySearch(int menu, String query) implements CustomPacketPayload {
    public static final Type<SeedLibrarySearch> TYPE = new Type<>(EssenceIndustry.id("seed_library_search"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SeedLibrarySearch> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SeedLibrarySearch::menu, ByteBufCodecs.stringUtf8(64), SeedLibrarySearch::query, SeedLibrarySearch::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            var player = context.player();
            if (player.containerMenu instanceof SeedLibraryMenu menu && menu.containerId == payload.menu() && menu.stillValid(player)) menu.filter(payload.query());
        });
    }
}
