package com.neolena.essenceindustry.compat.geore;

import com.mojang.serialization.MapCodec;
import com.neolena.essenceindustry.EssenceIndustry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class GeoOreLootModifiers {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, EssenceIndustry.MOD_ID);

    static {
        SERIALIZERS.register("geore_budding_drops", () -> GeoOreBuddingDropsModifier.CODEC);
        SERIALIZERS.register("ae2_flawless_budding_drops", () -> com.neolena.essenceindustry.compat.ae2.FlawlessBuddingDropsModifier.CODEC);
    }

    private GeoOreLootModifiers() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
