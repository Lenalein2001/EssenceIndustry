package com.neolena.essenceindustry.blueprint;
import com.neolena.essenceindustry.EssenceIndustry;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class MultiblockPlanner {
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(EssenceIndustry.MOD_ID);
 private static final DeferredRegister<DataComponentType<?>> DATA=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,EssenceIndustry.MOD_ID);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,EssenceIndustry.MOD_ID);
 private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> data(String n,Codec<T> c){return DATA.register(n,()->DataComponentType.<T>builder().persistent(c).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(c)).build());}
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BlueprintSettings>> SETTINGS=data("multiblock_plan",BlueprintSettings.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<ReactorCoreDesign>> CORE_DESIGN=data("reactor_core_design",ReactorCoreDesign.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<GlobalPos>> TARGET=data("multiblock_target",GlobalPos.CODEC);
 public static final DeferredItem<PlannerItem> TOOL=ITEMS.registerItem("multiblock_planner",p->new PlannerItem(p.stacksTo(1).fireResistant()));
 public static final DeferredHolder<MenuType<?>,MenuType<BlueprintMenu>> MENU=MENUS.register("multiblock_planner",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(BlueprintMenu::new));
 public static void register(IEventBus bus){ITEMS.register(bus);DATA.register(bus);MENUS.register(bus);
  bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.TOOLS_AND_UTILITIES)e.accept(TOOL.get());});
  if(net.neoforged.fml.ModList.get().isLoaded("ae2"))BlueprintNode.register(bus);
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post e)->BlueprintAutomation.tick());
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent e)->BlueprintAutomation.clear());
  if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())BlueprintScreen.register(bus);
 }
 private MultiblockPlanner(){}
}
