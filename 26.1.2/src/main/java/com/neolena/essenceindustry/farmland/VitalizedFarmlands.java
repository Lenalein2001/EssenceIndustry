package com.neolena.essenceindustry.farmland;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.ICropProvider;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.registries.*;
import java.util.List;

public final class VitalizedFarmlands {
    private static final String MOD = EssenceIndustry.MOD_ID;
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final CropTier SIX = new CropTier(EssenceIndustry.id("vitalized_6"), 6, 0x40005E, ChatFormatting.DARK_PURPLE).setDisplayName(Component.literal("6"));
    public static final List<DeferredBlock<VitalizedFarmlandBlock>> SOILS = List.of(
            soil("vitalized_farmland", CropTier.ONE), soil("vitalized_farmland_tier_2", CropTier.TWO),
            soil("vitalized_farmland_tier_3", CropTier.THREE), soil("vitalized_farmland_tier_4", CropTier.FOUR),
            soil("vitalized_farmland_tier_5", CropTier.FIVE), soil("vitalized_farmland_tier_6", SIX));
    public static final DeferredItem<VitalizingFertilizerItem> FERTILIZER = ITEMS.registerItem("vitalizing_fertilizer", VitalizingFertilizerItem::new);
    private static final ThreadLocal<Boolean> BOOSTING = ThreadLocal.withInitial(() -> false);
    private static DeferredBlock<VitalizedFarmlandBlock> soil(String name, CropTier tier) {
        var block = BLOCKS.registerBlock(name, p -> new VitalizedFarmlandBlock(EssenceIndustry.id(name), tier));
        ITEMS.registerItem(name, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()));
        return block;
    }
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus);
        bus.addListener(VitalizedFarmlands::creative);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VitalizedFarmlands::afterGrowth);
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            SOILS.forEach(block -> event.accept(block.get())); event.accept(FERTILIZER.get());
        }
    }
    private static void afterGrowth(CropGrowEvent.Post event) {
        if (BOOSTING.get() || !(event.getLevel() instanceof ServerLevel level)
                || !(level.getBlockState(event.getPos().below()).getBlock() instanceof VitalizedFarmlandBlock)) return;
        var pos = event.getPos();
        var before = level.getBlockState(pos);
        if (!(before.getBlock() instanceof BonemealableBlock plant) || !plant.isValidBonemealTarget(level, pos, before)) return;
        BOOSTING.set(true);
        try {
            // Match Red Fertilizer's extra bonemeal growth, including MA's own growth restrictions.
            plant.performBonemeal(level, level.getRandom(), pos, before);
            var after = level.getBlockState(pos);
            // Let Productive Farming observe maturation reached by the extra growth as well.
            if (after != before) CommonHooks.fireCropGrowPost(level, pos, before);
        } finally { BOOSTING.remove(); }
    }
    public static void addBonusSeed(BlockState state, LootParams.Builder context, List<ItemStack> drops) {
        // MA already supplies the bonus when its global setting is enabled.
        if (ModConfigs.SECONDARY_SEED_DROPS.get() || !(state.getBlock() instanceof CropBlock crop)
                || !crop.isMaxAge(state) || !(crop instanceof ICropProvider provider)) return;
        var origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) return;
        var level = context.getLevel();
        var soilPos = BlockPos.containing(origin).below();
        var virtual = com.neolena.essenceindustry.simulation.SimulationLootContext.block(level,soilPos);
        var soil = (virtual == null ? level.getBlockState(soilPos) : virtual).getBlock();
        if (!(soil instanceof VitalizedFarmlandBlock)
                || level.getRandom().nextDouble() >= provider.getCrop().getSecondaryChance(soil)) return;
        var seed = provider.getCrop().getSeedsItem();
        // Preserve all native gene components; works regardless of mixin injection order.
        for (var drop : drops) if (drop.is(seed)) { drops.add(drop.copyWithCount(1)); return; }
    }
    private VitalizedFarmlands() {}
}
