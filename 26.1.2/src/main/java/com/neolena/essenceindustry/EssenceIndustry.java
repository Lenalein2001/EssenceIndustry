package com.neolena.essenceindustry;

import com.neolena.essenceindustry.compat.ae2.Ae2GrowthAccelerators;
import com.neolena.essenceindustry.compat.geore.GeoOreLootModifiers;
import com.neolena.essenceindustry.compat.pylons.EnhancedHarvestingPylon;
import com.neolena.essenceindustry.essence.EssenceConverter;
import com.neolena.essenceindustry.farmland.VitalizedFarmlands;
import com.neolena.essenceindustry.item.ItemDescriptions;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(EssenceIndustry.MOD_ID)
public final class EssenceIndustry {
    public static final String MOD_ID = "essenceindustry";

    public EssenceIndustry(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        com.neolena.essenceindustry.migration.LegacyCompatibility.register(modBus);
        com.neolena.essenceindustry.compat.mysticalagriculture.PowahCropSupport.register(modBus);
        com.neolena.essenceindustry.compat.mysticalagriculture.CropTierCondition.register(modBus);
        GeoOreLootModifiers.register(modBus);
        EssenceConverter.register(modBus);
        com.neolena.essenceindustry.nuclear.NuclearFuels.register(modBus);
        com.neolena.essenceindustry.nuclear.NuclearReactor.register(modBus);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,com.neolena.essenceindustry.nuclear.ReactorConfig.SPEC,MOD_ID+"-reactor-server.toml");
        com.neolena.essenceindustry.simulation.CropSimulation.register(modBus);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,com.neolena.essenceindustry.simulation.SimulationConfig.SPEC);
        if (ModList.get().isLoaded("hostilenetworks")) com.neolena.essenceindustry.compat.hnn.HnnUtilities.register(modBus);
        com.neolena.essenceindustry.seedcraft.SeedCrafter.register(modBus);
        com.neolena.essenceindustry.weather.WeatherUtilities.register(modBus);
        com.neolena.essenceindustry.library.SeedLibrary.register(modBus);
        VitalizedFarmlands.register(modBus);
        ItemDescriptions.register();
        com.neolena.essenceindustry.guide.FieldGuide.register(modBus);
        com.neolena.essenceindustry.quantum.QuantumTools.register(modBus);
        com.neolena.essenceindustry.construction.QuantumBuilder.register(modBus);
        com.neolena.essenceindustry.blueprint.MultiblockPlanner.register(modBus);
        if (ModList.get().isLoaded("pylons")) {
            EnhancedHarvestingPylon.register(modBus);
        }
        if (ModList.get().isLoaded("ae2")) {
            container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,com.neolena.essenceindustry.array.ArrayConfig.SPEC,MOD_ID+"-array-server.toml");
            com.neolena.essenceindustry.array.ProcessingArray.register(modBus);
            com.neolena.essenceindustry.construction.QuantumWireless.register(modBus);
            com.neolena.essenceindustry.compat.ae2.pattern.EssencePatternManager.register(modBus);
            Ae2GrowthAccelerators.register(modBus);
            com.neolena.essenceindustry.compat.ae2.bulk.BulkCells.register(modBus);
            com.neolena.essenceindustry.compat.ae2.bulk.BulkDiskManager.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
