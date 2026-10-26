package com.neolena.essenceindustry.compat.pylons;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EnhancedHarvestingPylon {
    public static java.util.function.Function<EnhancedHarvestingPylonTile, PylonNetwork> networkFactory;
    private static final String MOD = EssenceIndustry.MOD_ID;
    private static final DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,MOD);
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.CustomData>> SETTINGS=COMPONENTS.register("pylon_settings",()->net.minecraft.core.component.DataComponentType.<net.minecraft.world.item.component.CustomData>builder().persistent(net.minecraft.world.item.component.CustomData.CODEC).networkSynchronized(net.minecraft.world.item.component.CustomData.STREAM_CODEC).build());
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD);
    public static final DeferredHolder<Block, EnhancedHarvestingPylonBlock> BLOCK = BLOCKS.registerBlock("enhanced_harvesting_pylon", EnhancedHarvestingPylonBlock::new);
    public static final DeferredHolder<Item, BlockItem> ITEM = ITEMS.registerItem("enhanced_harvesting_pylon", p -> new BlockItem(BLOCK.get(), p.useBlockDescriptionPrefix()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnhancedHarvestingPylonTile>> TILE = TILES.register("enhanced_harvesting_pylon", () -> new BlockEntityType<>(EnhancedHarvestingPylonTile::new, BLOCK.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<EnhancedHarvestingPylonMenu>> MENU = MENUS.register("enhanced_harvesting_pylon", () -> IMenuTypeExtension.create(EnhancedHarvestingPylonMenu::new));
    public static final ModConfigSpec.IntValue WORK_DELAY;
    public static final ModConfigSpec.IntValue SCAN_BUDGET;
    public static final ModConfigSpec.IntValue HARVEST_BUDGET;
    private static final ModConfigSpec CONFIG;
    static {
        var builder = new ModConfigSpec.Builder();
        WORK_DELAY = builder.comment("Ticks between work batches. Original Evolution pylon uses 60.").defineInRange("workDelay", 5, 1, 1200);
        SCAN_BUDGET = builder.comment("Maximum block positions checked per batch; unloaded chunks are skipped.").defineInRange("scanBudget", 128, 1, 1089);
        HARVEST_BUDGET = builder.comment("Maximum mature plants harvested per batch.").defineInRange("harvestBudget", 16, 1, 128);
        CONFIG = builder.build();
    }
    public static void register(IEventBus bus) {
        COMPONENTS.register(bus); BLOCKS.register(bus); ITEMS.register(bus); TILES.register(bus); MENUS.register(bus);
        ModContainer container = ModList.get().getModContainerById(MOD).orElseThrow();
        container.registerConfig(ModConfig.Type.SERVER, CONFIG, "essenceindustry-harvesting-server.toml");
        bus.addListener(EnhancedHarvestingPylon::creative);
        if (ModList.get().isLoaded("ae2")) com.neolena.essenceindustry.compat.ae2.PylonAe2Compat.register(bus);
        if (FMLEnvironment.getDist().isClient()) PylonClient.register(bus);
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(ITEM.get());
    }
    private EnhancedHarvestingPylon() {}
}
