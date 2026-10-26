package com.neolena.essenceindustry.nuclear;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
import java.util.*;
/** Persistent fuel-plant controllers and the solid materials used by the reactor cycle. */
public final class NuclearFuels {
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(EssenceIndustry.MOD_ID);
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(EssenceIndustry.MOD_ID);
 private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,EssenceIndustry.MOD_ID);
 public static final Map<String,DeferredItem<Item>> MATERIALS=new LinkedHashMap<>();
 public static final Map<String,DeferredBlock<FuelProcessorBlock>> MACHINES=new LinkedHashMap<>();
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FuelProcessorTile>> TILE;
 static{
  for(String name:List.of("enriched_uranium_fuel","depleted_uranium","recovered_plutonium","plutonium_fuel","mox_fuel","thorium_fuel","advanced_nuclear_fuel","spent_uranium_fuel","spent_enriched_uranium_fuel","spent_plutonium_fuel","spent_mox_fuel","spent_thorium_fuel","spent_advanced_nuclear_fuel","recovered_fissile_material","nuclear_waste"))MATERIALS.put(name,ITEMS.registerSimpleItem(name));
  for(String name:List.of("fuel_enricher","fuel_reprocessor","fuel_fabricator")){var block=BLOCKS.registerBlock(name,p->new FuelProcessorBlock(p));MACHINES.put(name,block);ITEMS.registerSimpleBlockItem(block);}
  TILE=TILES.register("fuel_processor",()->new BlockEntityType<>(FuelProcessorTile::new,MACHINES.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
 }
 public static ItemStack stack(String name,int count){return new ItemStack(MATERIALS.get(name).get(),count);}
 public static void register(IEventBus bus){ITEMS.register(bus);BLOCKS.register(bus);TILES.register(bus);bus.addListener((RegisterCapabilitiesEvent e)->{e.registerBlockEntity(Capabilities.Item.BLOCK,TILE.get(),(t,s)->t.automation());e.registerBlockEntity(Capabilities.Energy.BLOCK,TILE.get(),(t,s)->t.automationEnergy());});bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS){MACHINES.values().forEach(b->e.accept(b.get()));MATERIALS.values().forEach(i->e.accept(i.get()));}});}
}
