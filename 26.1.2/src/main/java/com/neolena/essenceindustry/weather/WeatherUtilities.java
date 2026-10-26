package com.neolena.essenceindustry.weather;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;
public final class WeatherUtilities {
 private static final String MOD=EssenceIndustry.MOD_ID;
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD);
 public static final DeferredHolder<Block,CelestialControllerBlock> CONTROLLER=BLOCKS.registerBlock("celestial_controller",CelestialControllerBlock::new);
 public static final DeferredHolder<Block,RainSensorBlock> SENSOR=BLOCKS.registerBlock("rain_sensor",RainSensorBlock::new);
 public static final DeferredHolder<Item,BlockItem> CONTROLLER_ITEM=ITEMS.registerItem("celestial_controller",p->new BlockItem(CONTROLLER.get(),p.useBlockDescriptionPrefix()));
 public static final DeferredHolder<Item,BlockItem> SENSOR_ITEM=ITEMS.registerItem("rain_sensor",p->new BlockItem(SENSOR.get(),p.useBlockDescriptionPrefix()));
 public static final DeferredHolder<MenuType<?>,MenuType<CelestialControllerMenu>> MENU=MENUS.register("celestial_controller",()->IMenuTypeExtension.create((id,inventory,buffer)->new CelestialControllerMenu(id,inventory,buffer)));
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);MENUS.register(bus);bus.addListener(WeatherUtilities::creative);if(FMLEnvironment.getDist().isClient())CelestialControllerScreen.register(bus);}
 private static void creative(BuildCreativeModeTabContentsEvent event){if(event.getTabKey()==CreativeModeTabs.REDSTONE_BLOCKS){event.accept(CONTROLLER_ITEM.get());event.accept(SENSOR_ITEM.get());}}
 private WeatherUtilities(){}
}
