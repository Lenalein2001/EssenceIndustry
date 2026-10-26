package com.neolena.essenceindustry.seedcraft;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
public final class SeedCrafter {
 private static final String MOD=EssenceIndustry.MOD_ID;
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD);
 private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD);
 public static final DeferredHolder<Block,SeedCrafterBlock> BLOCK=BLOCKS.registerBlock("seed_crafter",SeedCrafterBlock::new);
 public static final DeferredHolder<Item,BlockItem> ITEM=ITEMS.registerItem("seed_crafter",p->new BlockItem(BLOCK.get(),p.useBlockDescriptionPrefix()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SeedCrafterTile>> TILE=TILES.register("seed_crafter",()->new BlockEntityType<>(SeedCrafterTile::new,BLOCK.get()));
 public static final DeferredHolder<MenuType<?>,MenuType<SeedCrafterMenu>> MENU=MENUS.register("seed_crafter",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(SeedCrafterMenu::new));
 public static void register(IEventBus bus){
  BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);MobSeedRecipes.register(bus);
  bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Item.BLOCK,TILE.get(),(tile,side)->tile.automation()));
  bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)e.accept(ITEM.get());});
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent e)->{if(e.getPlayer()==null)SeedCrafterTile.clearCatalog();});
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->SeedCrafterTile.clearCatalog());
  if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())SeedCrafterScreen.register(bus);
 }
 private SeedCrafter(){}
}
