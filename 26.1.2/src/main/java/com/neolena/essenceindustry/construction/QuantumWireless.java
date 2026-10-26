package com.neolena.essenceindustry.construction;
import com.neolena.essenceindustry.EssenceIndustry;
import appeng.api.features.*;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.GlobalPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class QuantumWireless {
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(EssenceIndustry.MOD_ID);
 public static final DeferredItem<WirelessBoosterItem> CARD=ITEMS.registerItem("quantum_wireless_booster",p->new WirelessBoosterItem(p.stacksTo(1)));
 public static final double DRAIN=128;
 public static boolean upgraded(IWirelessAccessPoint point){return point instanceof WirelessAccessPointBlockEntity ap&&ap.getInternalInventory().getStackInSlot(0).is(CARD.get());}
 public static void register(IEventBus bus){ITEMS.register(bus);
 bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e)->e.enqueueWork(()->GridLinkables.register(QuantumBuilder.TOOL.get(),new IGridLinkableHandler(){
  public boolean canLink(ItemStack s){return s.getItem() instanceof BuilderItem&&!s.has(QuantumBuilder.RECOVERY.get());}
  public void link(ItemStack s,GlobalPos pos){if(s.has(QuantumBuilder.RECOVERY.get()))return;s.set(QuantumBuilder.LINK.get(),pos);}
  public void unlink(ItemStack s){if(s.has(QuantumBuilder.RECOVERY.get()))return;s.remove(QuantumBuilder.LINK.get());}
 })));
 bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.TOOLS_AND_UTILITIES)e.accept(CARD.get());});
 }
 private QuantumWireless(){}
}
