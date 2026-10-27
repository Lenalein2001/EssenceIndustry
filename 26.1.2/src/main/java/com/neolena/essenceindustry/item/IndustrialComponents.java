package com.neolena.essenceindustry.item;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Shared, mod-independent ingredients for ordinary machinery. */
public final class IndustrialComponents {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EssenceIndustry.MOD_ID);
    public static final DeferredItem<Item> CONTROL_CIRCUIT = ITEMS.registerSimpleItem("industrial_control_circuit");
    public static final DeferredItem<Item> INDUCTION_WINDING = ITEMS.registerSimpleItem("induction_winding");

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                event.accept(CONTROL_CIRCUIT.get());
                event.accept(INDUCTION_WINDING.get());
            }
        });
    }

    private IndustrialComponents() {}
}
