package com.neolena.essenceindustry.library;

import com.neolena.essenceindustry.EssenceIndustry;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;

public final class SeedLibrary {
    private static final String MOD = EssenceIndustry.MOD_ID;
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<SeedVault.Record>>> CONTENTS = COMPONENTS.register("seed_library_contents", () -> DataComponentType.<List<SeedVault.Record>>builder()
            .persistent(SeedVault.Record.CODEC.listOf()).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SeedVault.Record.CODEC.listOf())).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.world.item.component.CustomData>> RECOVERY = COMPONENTS.register("stored_inventory_recovery", () -> DataComponentType.<net.minecraft.world.item.component.CustomData>builder()
            .persistent(net.minecraft.world.item.component.CustomData.CODEC).networkSynchronized(net.minecraft.world.item.component.CustomData.STREAM_CODEC).build());
    public static final DeferredHolder<Block, SeedLibraryBlock> BLOCK = BLOCKS.registerBlock("seed_library", SeedLibraryBlock::new);
    public static final DeferredHolder<Item, BlockItem> ITEM = ITEMS.registerItem("seed_library", p -> new BlockItem(BLOCK.get(), p.stacksTo(1).useBlockDescriptionPrefix()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeedLibraryTile>> TILE = TILES.register("seed_library", () -> new BlockEntityType<>(SeedLibraryTile::new, BLOCK.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<SeedLibraryMenu>> MENU = MENUS.register("seed_library", () -> IMenuTypeExtension.create(SeedLibraryMenu::new));
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); TILES.register(bus); MENUS.register(bus); COMPONENTS.register(bus);
        bus.addListener(SeedLibrary::capabilities); bus.addListener(SeedLibrary::creative);
        bus.addListener(SeedLibrarySearch::register);
        StorageBreakSafety.register();
        SeedRank.register();
        if (net.neoforged.fml.ModList.get().isLoaded("ae2")) SeedLibraryAe2Compat.register(bus);
        if (FMLEnvironment.getDist().isClient()) SeedLibraryScreen.register(bus);
    }
    private static void capabilities(RegisterCapabilitiesEvent event) { event.registerBlockEntity(Capabilities.Item.BLOCK, TILE.get(), (tile, side) -> tile.automation()); }
    private static void creative(BuildCreativeModeTabContentsEvent event) { if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(ITEM.get()); }
    private SeedLibrary() {}
}
