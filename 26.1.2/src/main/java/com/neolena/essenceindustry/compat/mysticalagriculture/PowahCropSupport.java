package com.neolena.essenceindustry.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.LoggerFactory;

/** Restores the existing Powah crops hidden by Evolution's crop configuration. */
public final class PowahCropSupport {
    private static final ModConfigSpec CONFIG;
    private static final ModConfigSpec.BooleanValue RESTORE_DISABLED_SEEDS;

    static {
        var builder = new ModConfigSpec.Builder();
        RESTORE_DISABLED_SEEDS = builder
                .comment("Restore existing Energized Steel, Blazing Crystal, Niotic Crystal, Spirited Crystal and Nitro Crystal crops when Powah is installed.",
                        "Overrides their enabled=false crop settings only; tiers and Nitro's crux stay unchanged.",
                        "Set false to respect the pack's disabled crop settings. Requires a game/server restart.")
                .define("restoreDisabledPowahSeeds", true);
        CONFIG = builder.build();
    }

    public static void register(IEventBus bus) {
        ModList.get().getModContainerById(EssenceIndustry.MOD_ID).orElseThrow()
                .registerConfig(ModConfig.Type.COMMON, CONFIG, "essenceindustry-powah-common.toml");
        // All plugins (including Mystical Customization) must finish applying crop settings first.
        // This still runs before server recipe loading and JEI's client recipe discovery.
        bus.addListener((FMLLoadCompleteEvent event) -> event.enqueueWork(() -> { restore(); }));
    }

    private static void restore() {
        if (!ModList.get().isLoaded("powah") || !RESTORE_DISABLED_SEEDS.get()) return;
        restore("mysticalagriculture:energized_steel", "powah:steel_energized");
        restore("mysticalagriculture:blazing_crystal", "powah:crystal_blazing");
        restore("mysticalagriculture:niotic_crystal", "powah:crystal_niotic");
        restore("mysticalagriculture:spirited_crystal", "powah:crystal_spirited");
        restore("mysticalagradditions:nitro_crystal", "powah:crystal_nitro");
    }

    private static void restore(String cropId, String materialId) {
        // Exact IDs preserve crop ownership and never replace a pack-defined crop with the same name.
        var crop = CropRegistry.getInstance().getCropById(Identifier.parse(cropId));
        if (crop == null || crop.isEnabled()
                || !BuiltInRegistries.ITEM.containsKey(Identifier.parse(materialId))) return;
        crop.setEnabled(true);
        LoggerFactory.getLogger(PowahCropSupport.class).info("Restored existing Powah crop {}", cropId);
    }

    private PowahCropSupport() {}
}
