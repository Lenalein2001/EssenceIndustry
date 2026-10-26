package com.neolena.essenceindustry.simulation;
import com.neolena.essenceindustry.EssenceIndustry;
import com.neolena.essenceindustry.simulation.SimulationNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
public final class CropSimulation {
 private static final String MOD=EssenceIndustry.MOD_ID;
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD);
 private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD);
 public static final DeferredHolder<Block,CropSimulationBlock> BLOCK=BLOCKS.registerBlock("crop_simulator",CropSimulationBlock::new);
 public static final DeferredHolder<Item,BlockItem> ITEM=ITEMS.registerItem("crop_simulator",p->new BlockItem(BLOCK.get(),p.useBlockDescriptionPrefix()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CropSimulationTile>> TILE=TILES.register("crop_simulator",()->new BlockEntityType<>(CropSimulationTile::new,BLOCK.get()));
 public static final DeferredHolder<MenuType<?>,MenuType<CropSimulationMenu>> MENU=MENUS.register("crop_simulator",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(CropSimulationMenu::new));
 public static final DeferredHolder<Item,Item> SPEED=ITEMS.registerItem("crop_speed_upgrade",p->new Item(p.stacksTo(8)));
 public static final DeferredHolder<Item,Item> YIELD=ITEMS.registerItem("crop_yield_upgrade",p->new Item(p.stacksTo(8)));
 public static java.util.function.Function<CropSimulationTile,SimulationNetwork> networkFactory;
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent e)->{if(e.getPlayer()==null)CropSimulationTile.invalidate();});
  bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Item.BLOCK,TILE.get(),(tile,side)->tile.automation()));
  bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS){e.accept(ITEM.get());e.accept(SPEED.get());e.accept(YIELD.get());}});
  if(net.neoforged.fml.ModList.get().isLoaded("ae2"))com.neolena.essenceindustry.compat.ae2.CropSimulationAe2Compat.register(bus);
  if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())CropSimulationScreen.register(bus);
 }
 private CropSimulation(){}
}
