package com.neolena.essenceindustry.item;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class ItemDescriptions {
    public static void register() { NeoForge.EVENT_BUS.addListener(ItemDescriptions::describe); }
    private static void describe(ItemTooltipEvent event) {
        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!id.getNamespace().equals(EssenceIndustry.MOD_ID)) return;
        String path = id.getPath();
        String key = switch (path) {
            case "field_guide" -> "field_guide";
            case "industrial_control_circuit", "induction_winding" -> path;
            case "processing_array", "array_casing", "array_glass", "array_rack", "array_pattern_hatch", "array_output_port" -> path;
            case "quantum_alloy", "quantum_circuit", "quantum_module_chassis", "quantum_core" -> path;
            case "essence_converter" -> "converter";
            case "seed_crafter" -> "seed_crafter";
            case "crop_speed_upgrade", "crop_yield_upgrade", "model_copier", "crop_simulator" -> path;
            case "bulk_disk_manager", "celestial_controller", "rain_sensor" -> path;
            case "essence_pattern_manager" -> "pattern_manager";
            case "seed_library" -> "seed_library";
            case "enhanced_harvesting_pylon" -> "pylon";
            case "vitalizing_fertilizer" -> "fertilizer";
            case "block_accelerator" -> "block_accelerator";
            case "enriched_uranium_fuel", "plutonium_fuel", "mox_fuel", "thorium_fuel", "advanced_nuclear_fuel" -> "prepared_nuclear_fuel";
            case "spent_uranium_fuel", "spent_enriched_uranium_fuel", "spent_plutonium_fuel", "spent_mox_fuel", "spent_thorium_fuel", "spent_advanced_nuclear_fuel" -> "spent_nuclear_fuel";
            case "depleted_uranium", "recovered_plutonium", "recovered_fissile_material", "nuclear_waste",
                    "fuel_enricher", "fuel_reprocessor", "fuel_fabricator" -> path;
            case "turbine_controller", "turbine_shaft", "turbine_blade", "turbine_bearing",
                    "turbine_condenser", "turbine_foot", "turbine_light" -> path;
            case "turbine_coil_copper", "turbine_coil_uranium", "turbine_coil_enriched", "turbine_coil_mox",
                    "turbine_coil_plutonium", "turbine_coil_advanced" -> "turbine_coil";
            default -> path.startsWith("reactor_") ? path
                    : path.startsWith("vitalized_farmland") ? "farmland"
                    : path.startsWith("growth_accelerator_tier_") ? "growth_accelerator" : null;
        };
        if (!path.equals("crop_simulator") && event.getItemStack().has(com.neolena.essenceindustry.library.SeedLibrary.RECOVERY.get()))
            event.getToolTip().add(Component.translatable("tooltip.essenceindustry.storage_recovery").withStyle(ChatFormatting.YELLOW));
        if (key != null) event.getToolTip().add(Math.min(1, event.getToolTip().size()),
                Component.translatable("tooltip.essenceindustry." + key).withStyle(ChatFormatting.GRAY));
    }
    private ItemDescriptions() {}
}
