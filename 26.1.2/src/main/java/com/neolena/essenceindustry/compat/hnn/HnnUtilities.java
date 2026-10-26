package com.neolena.essenceindustry.compat.hnn;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.data.DataModelInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
/** Registers the copier. Model routing uses native HNN inventories without another block or ticker. */
public final class HnnUtilities {
 private static final String MOD=com.neolena.essenceindustry.EssenceIndustry.MOD_ID;
 private static final net.neoforged.neoforge.registries.DeferredRegister.Blocks BLOCKS=net.neoforged.neoforge.registries.DeferredRegister.createBlocks(MOD);
 private static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS=net.neoforged.neoforge.registries.DeferredRegister.createItems(MOD);
 private static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> TILES=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,MOD);
 public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.Block,ModelCopierBlock> BLOCK=BLOCKS.registerBlock("model_copier",ModelCopierBlock::new);
 public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.Item,net.minecraft.world.item.BlockItem> ITEM=ITEMS.registerItem("model_copier",p->new net.minecraft.world.item.BlockItem(BLOCK.get(),p.useBlockDescriptionPrefix()));
 public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<ModelCopierTile>> TILE=TILES.register("model_copier",()->new net.minecraft.world.level.block.entity.BlockEntityType<>(ModelCopierTile::new,BLOCK.get()));
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);
  bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e)->e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,TILE.get(),(tile,side)->tile.automation()));
  bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)e.accept(ITEM.get());});
 }
 public static boolean isModel(ItemStack stack){return stack.getItem() instanceof DataModelItem && new DataModelInstance(stack,0).isValid();}
 public static boolean complete(ItemStack stack){
  if(!(stack.getItem() instanceof DataModelItem))return false;
  return complete(new DataModelInstance(stack,0));
 }
 public static boolean complete(DataModelInstance model){
  if(!model.isValid())return false;
  // Self Aware is the routing boundary even when a pack registers higher tiers.
  // Use the model's own requirement: data packs may scale it for individual mobs.
  for(var tier:dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry.getSortedTiers())
   if("self_aware".equals(tier.name()))return model.getData()>=model.getModel().getRequiredData(tier);
  return model.getTier().isMax();
 }
 private HnnUtilities(){}
}
