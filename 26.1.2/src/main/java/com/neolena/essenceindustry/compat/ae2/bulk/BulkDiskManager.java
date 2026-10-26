package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.api.AECapabilities;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class BulkDiskManager {
 private static final String MOD=EssenceIndustry.MOD_ID;
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD);
 private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD);
 public static final DeferredHolder<Block,DiskManagerBlock> BLOCK=BLOCKS.registerBlock("bulk_disk_manager",DiskManagerBlock::new);
 public static final DeferredHolder<Item,BlockItem> ITEM=ITEMS.registerItem("bulk_disk_manager",p->new BlockItem(BLOCK.get(),p.useBlockDescriptionPrefix()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DiskManagerTile>> TILE=TILES.register("bulk_disk_manager",()->{var t=new BlockEntityType<DiskManagerTile>(DiskManagerTile::new,BLOCK.get());BLOCK.get().setBlockEntity(DiskManagerTile.class,t,null,null);return t;});
 public static final DeferredHolder<MenuType<?>,MenuType<DiskManagerMenu>> MENU=MENUS.register("bulk_disk_manager",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(DiskManagerMenu::new));
 public static void register(IEventBus bus){
  BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);
  bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e)->e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST,TILE.get(),(tile,side)->tile));
  bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)e.accept(ITEM.get());});
  if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())DiskManagerScreen.register(bus);
 }
 private BulkDiskManager(){}
}
