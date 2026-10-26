package com.neolena.essenceindustry.construction;
import com.neolena.essenceindustry.EssenceIndustry;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.GlobalPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class QuantumBuilder {
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(EssenceIndustry.MOD_ID);
 private static final DeferredRegister<DataComponentType<?>> DATA=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,EssenceIndustry.MOD_ID);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,EssenceIndustry.MOD_ID);
 private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> data(String name,Codec<T> codec){return DATA.register(name,()->DataComponentType.<T>builder().persistent(codec).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(codec)).build());}
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BuilderSettings>> SETTINGS=data("builder_settings",BuilderSettings.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BlockState>> MATERIAL=data("builder_material",BlockState.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BuilderAnchor>> ANCHOR=data("builder_anchor",BuilderAnchor.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<GlobalPos>> LINK=data("builder_link",GlobalPos.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BuilderStock>> BANK=data("builder_bank",BuilderStock.CODEC);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BuilderJournal>> JOURNAL=DATA.register("builder_journal",()->DataComponentType.<BuilderJournal>builder().persistent(BuilderJournal.CODEC).networkSynchronized(BuilderJournalWire.CODEC).build());
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BuilderHistory>> HISTORY=DATA.register("builder_history",()->DataComponentType.<BuilderHistory>builder().persistent(BuilderHistory.CODEC).networkSynchronized(BuilderHistory.WIRE).build());
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BuilderTransfer>> RECOVERY=data("builder_transfer_recovery",BuilderTransfer.CODEC);
 public static final DeferredItem<BuilderItem> TOOL=ITEMS.registerItem("quantum_builder",p->new BuilderItem(p.stacksTo(1).fireResistant()));
 public static final DeferredHolder<MenuType<?>,MenuType<BuilderMenu>> MENU=MENUS.register("quantum_builder",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(BuilderMenu::new));
 public static void register(IEventBus bus){ITEMS.register(bus);DATA.register(bus);MENUS.register(bus);bus.addListener(BuilderOpen::register);bus.addListener(BuilderAction::register);
 bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.TOOLS_AND_UTILITIES)e.accept(TOOL.get());});
 net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(BuilderWork::tick);
 net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(BuilderWork::stopped);
 if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())BuilderScreen.register(bus);
 }
 private QuantumBuilder(){}
}
