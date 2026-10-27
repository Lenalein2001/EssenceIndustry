package com.neolena.essenceindustry.quantum;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.*;

public final class QuantumTools {
    private static final String MOD = EssenceIndustry.MOD_ID;
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> MODULES = DATA.register("quantum_modules",
        () -> DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).build());
    public static final DeferredItem<QuantumToolItem> SWORD = ITEMS.registerItem("quantum_sword", p -> new QuantumToolItem(p, false));
    public static final DeferredItem<QuantumToolItem> STAFF = ITEMS.registerItem("quantum_staff", p -> new QuantumToolItem(p, true));
    public static final DeferredItem<Item> ALLOY = ITEMS.registerSimpleItem("quantum_alloy");
    public static final DeferredItem<Item> CIRCUIT = ITEMS.registerSimpleItem("quantum_circuit");
    public static final DeferredItem<Item> CHASSIS = ITEMS.registerSimpleItem("quantum_module_chassis");
    public static final DeferredItem<Item> CORE = ITEMS.registerSimpleItem("quantum_core");
    public static final java.util.Map<QuantumModule.Kind, DeferredItem<QuantumModule>> MODULE_ITEMS = new java.util.EnumMap<>(QuantumModule.Kind.class);
    static { for (var kind : QuantumModule.Kind.values()) MODULE_ITEMS.put(kind, ITEMS.registerItem("quantum_" + kind.id() + "_module", p -> new QuantumModule(p, kind))); }
    public static final DeferredHolder<MenuType<?>, MenuType<QuantumToolMenu>> MENU = MENUS.register("quantum_tool", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(QuantumToolMenu::new));
    public static void register(IEventBus bus) {
        ITEMS.register(bus); DATA.register(bus); MENUS.register(bus);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) { e.accept(STAFF.get()); for (var m : MODULE_ITEMS.values()) e.accept(m.get()); }
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) { e.accept(ALLOY.get());e.accept(CIRCUIT.get());e.accept(CHASSIS.get());e.accept(CORE.get()); }
            if (e.getTabKey() == CreativeModeTabs.COMBAT) e.accept(SWORD.get());
        });
        NeoForge.EVENT_BUS.addListener(QuantumMining::tick);
        NeoForge.EVENT_BUS.addListener(QuantumMining::stopped);
        NeoForge.EVENT_BUS.addListener(QuantumModuleSafety::anvil);
        NeoForge.EVENT_BUS.addListener(QuantumModuleSafety::grindstone);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,QuantumCombat::critical);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,QuantumCombat::groundAttack);
        NeoForge.EVENT_BUS.addListener(QuantumCombat::lifeSteal);
        NeoForge.EVENT_BUS.addListener(QuantumCombat::areaDamage);
        NeoForge.EVENT_BUS.addListener(QuantumCombat::experience);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) QuantumToolScreen.register(bus);
    }
    private QuantumTools() {}
}
