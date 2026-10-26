package com.neolena.essenceindustry.compat.ae2.pattern;

import appeng.api.AECapabilities;
import appeng.block.AEBaseEntityBlock;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;

public final class EssencePatternManager {
    private static final String MOD = EssenceIndustry.MOD_ID;
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD);
    public static final DeferredHolder<Block, PatternManagerBlock> BLOCK = BLOCKS.registerBlock("essence_pattern_manager", PatternManagerBlock::new);
    public static final DeferredHolder<Item, BlockItem> ITEM = ITEMS.registerItem("essence_pattern_manager", p -> new BlockItem(BLOCK.get(), p.stacksTo(1).useBlockDescriptionPrefix()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PatternManagerTile>> TILE = TILES.register("essence_pattern_manager", () -> {
        var type = new BlockEntityType<PatternManagerTile>(PatternManagerTile::new, BLOCK.get());
        BLOCK.get().setBlockEntity(PatternManagerTile.class, type, null, null); return type;
    });
    public static final DeferredHolder<MenuType<?>, MenuType<PatternManagerMenu>> MENU = MENUS.register("essence_pattern_manager", () -> IMenuTypeExtension.create(PatternManagerMenu::new));
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); TILES.register(bus); MENUS.register(bus);
        bus.addListener(PatternManagerSearch::register);
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e) -> e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, TILE.get(), (tile, side) -> tile));
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e) -> { if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(ITEM.get()); });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent e) -> PatternManagerTile.recipesChanged(e.getPlayerList().getServer()));
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e) -> { EssencePatternCatalog.clear(); PatternManagerTile.clearServer(e.getServer()); });
        if (FMLEnvironment.getDist().isClient()) PatternManagerScreen.register(bus);
    }
    private EssencePatternManager() {}
}
