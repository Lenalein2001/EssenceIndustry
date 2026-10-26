package com.neolena.essenceindustry.essence;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;

public final class EssenceConverter {
    public static java.util.function.Function<EssenceConverterTile, ConverterNetwork> networkFactory;
    private static final String MOD = EssenceIndustry.MOD_ID;
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD);
    private static final DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD);
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.CustomData>> RESERVES = COMPONENTS.register("converter_reserves", () -> net.minecraft.core.component.DataComponentType.<net.minecraft.world.item.component.CustomData>builder()
            .persistent(net.minecraft.world.item.component.CustomData.CODEC).networkSynchronized(net.minecraft.world.item.component.CustomData.STREAM_CODEC).build());
    public static final DeferredHolder<Block, EssenceConverterBlock> BLOCK = BLOCKS.registerBlock("essence_converter", EssenceConverterBlock::new);
    public static final DeferredHolder<Item, BlockItem> ITEM = ITEMS.registerItem("essence_converter", p -> new BlockItem(BLOCK.get(), p.useBlockDescriptionPrefix()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceConverterTile>> TILE = TILES.register("essence_converter", () -> new BlockEntityType<>(EssenceConverterTile::new, BLOCK.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<EssenceConverterMenu>> MENU = MENUS.register("essence_converter", () -> IMenuTypeExtension.create(EssenceConverterMenu::new));
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); TILES.register(bus); MENUS.register(bus); COMPONENTS.register(bus);
        bus.addListener(ConverterReserve::register);
        bus.addListener(EssenceConverter::capabilities);
        bus.addListener(EssenceConverter::creative);
        if (net.neoforged.fml.ModList.get().isLoaded("ae2")) com.neolena.essenceindustry.compat.ae2.ConverterAe2Compat.register(bus);
        if (FMLEnvironment.getDist().isClient()) EssenceConverterScreen.register(bus);
    }
    private static void capabilities(RegisterCapabilitiesEvent event) { event.registerBlockEntity(Capabilities.Item.BLOCK, TILE.get(), (tile, side) -> tile.automation()); }
    private static void creative(BuildCreativeModeTabContentsEvent event) { if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(ITEM.get()); }
    private EssenceConverter() {}
}
