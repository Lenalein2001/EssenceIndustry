package com.neolena.essenceindustry.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neolena.essenceindustry.EssenceIndustry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.*;

/** Only apply pack-crop defaults to the matching owner and actual configured tier. */
public record CropTierCondition(String crop, String owner, int tier) implements ICondition {
    public static final MapCodec<CropTierCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("crop").forGetter(CropTierCondition::crop),
            Codec.STRING.fieldOf("owner").forGetter(CropTierCondition::owner),
            Codec.intRange(1, 6).fieldOf("tier").forGetter(CropTierCondition::tier)).apply(i, CropTierCondition::new));
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, EssenceIndustry.MOD_ID);
    static { CONDITIONS.register("crop_tier", () -> CODEC); }
    public static void register(IEventBus bus) { CONDITIONS.register(bus); }
    @Override public boolean test(IContext context) {
        var found = CropRegistry.getInstance().getCropByName(crop);
        return found != null && found.isEnabled() && found.getModId().equals(owner) && found.getTier().getValue() == tier;
    }
    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
}
