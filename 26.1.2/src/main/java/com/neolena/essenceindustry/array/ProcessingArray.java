package com.neolena.essenceindustry.array;
import com.neolena.essenceindustry.EssenceIndustry;
import appeng.api.AECapabilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
public final class ProcessingArray {
 private static final appeng.api.storage.MEStorage NO_EXTERNAL_MOUNT=()->net.minecraft.network.chat.Component.translatable("block.essenceindustry.processing_array");
 private static final String MOD=EssenceIndustry.MOD_ID;
 private static final java.util.concurrent.atomic.AtomicBoolean LIMITS_CHANGED=new java.util.concurrent.atomic.AtomicBoolean();
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD);
 private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD);
 public static final DeferredHolder<Block,ArrayBlock> CONTROLLER=BLOCKS.registerBlock("processing_array",ArrayBlock::new);
 public static final DeferredHolder<Block,ArrayPartBlock> CASING=part("array_casing"),GLASS=part("array_glass"),RACK=part("array_rack");
 public static final DeferredHolder<Block,ArrayConnectorBlock> HATCH=BLOCKS.registerBlock("array_pattern_hatch",ArrayConnectorBlock::new),PORT=BLOCKS.registerBlock("array_output_port",ArrayConnectorBlock::new);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArrayConnectorTile>> CONNECTOR_TILE=TILES.register("array_connector",()->{var t=new BlockEntityType<ArrayConnectorTile>(ArrayConnectorTile::new,HATCH.get(),PORT.get());HATCH.get().setBlockEntity(ArrayConnectorTile.class,t,null,null);PORT.get().setBlockEntity(ArrayConnectorTile.class,t,null,null);return t;});
 private static DeferredHolder<Block,ArrayPartBlock> part(String id){return BLOCKS.registerBlock(id,p->new ArrayPartBlock(p,id.equals("array_glass")));}
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArrayTile>> TILE=TILES.register("processing_array",()->{var type=new BlockEntityType<ArrayTile>(ArrayTile::new,CONTROLLER.get());CONTROLLER.get().setBlockEntity(ArrayTile.class,type,null,null);return type;});
 public static final DeferredHolder<MenuType<?>,MenuType<ArrayMenu>> MENU=MENUS.register("processing_array",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(ArrayMenu::new));
 static{for(var entry:java.util.List.of(CONTROLLER,CASING,GLASS,RACK,HATCH,PORT)){String path=entry.getId().getPath();ITEMS.registerItem(path,p->new BlockItem(entry.get(),p.useBlockDescriptionPrefix()));}ArrayUpgrades.register(ITEMS);}
 public static void register(IEventBus bus){
  BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);
  bus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Reloading event)->{if(event.getConfig().getSpec()==ArrayConfig.SPEC)LIMITS_CHANGED.set(true);});
  // Config file watchers need not run on the game thread. Touch loaded tiles only here.
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Pre event)->{if(LIMITS_CHANGED.getAndSet(false))ArrayTile.invalidateStructures();ArrayVisualTransition.tick();});
  // Invalidate the position that changed, not every shell block notified nearby.
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent event)->{if(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)ArrayTile.invalidateStructure(level,event.getPos());});
  bus.addListener((RegisterCapabilitiesEvent e)->{
   e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST,TILE.get(),(t,side)->t);
   e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST,CONNECTOR_TILE.get(),(t,side)->t);
   e.registerBlockEntity(AECapabilities.CRAFTING_MACHINE,CONNECTOR_TILE.get(),(t,side)->t.controller());
   e.registerBlockEntity(AECapabilities.CRAFTING_MACHINE,TILE.get(),(t,side)->t);
   e.registerBlockEntity(Capabilities.Energy.BLOCK,TILE.get(),(t,side)->t.energy());
   e.registerBlockEntity(Capabilities.Energy.BLOCK,CONNECTOR_TILE.get(),(t,side)->t.controller()==null?null:t.controller().energy());
   e.registerBlockEntity(Capabilities.Item.BLOCK,TILE.get(),(t,side)->t.outputHandler());
   e.registerBlockEntity(Capabilities.Item.BLOCK,CONNECTOR_TILE.get(),(t,side)->t.getBlockState().is(HATCH.get())?t.rawInput():t.controller()==null?null:t.controller().outputHandler());
   e.registerBlockEntity(Capabilities.Fluid.BLOCK,TILE.get(),(t,side)->t.fluidOutputHandler());
   e.registerBlockEntity(AECapabilities.ME_STORAGE,TILE.get(),(t,side)->NO_EXTERNAL_MOUNT);
   e.registerBlock(Capabilities.Item.BLOCK,(level,pos,state,tile,side)->{var owner=ArrayTile.owner(level,pos);return owner==null?null:owner.outputHandler();},PORT.get());
   e.registerBlock(Capabilities.Fluid.BLOCK,(level,pos,state,tile,side)->{var owner=ArrayTile.owner(level,pos);return owner==null?null:owner.fluidOutputHandler();},PORT.get());
   e.registerBlock(AECapabilities.ME_STORAGE,(level,pos,state,tile,side)->NO_EXTERNAL_MOUNT,PORT.get());
  });
  bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS){for(var b:java.util.List.of(CONTROLLER,CASING,GLASS,RACK,HATCH,PORT))e.accept(b.get().asItem());for(var item:ArrayUpgrades.items())e.accept(item.get());}});
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent e)->{if(e.getPlayer()==null)ArrayTile.invalidateRecipes();});
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->ArrayTile.clearLoaded());
  if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()){ArrayScreen.register(bus);ArrayRenderer.register(bus);ArrayScenePreview.register(bus);}
 }
 private ProcessingArray(){}
}
