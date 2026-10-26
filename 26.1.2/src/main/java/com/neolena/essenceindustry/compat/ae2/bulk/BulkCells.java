package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.api.ids.AECreativeTabIds;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.*;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.*;
public final class BulkCells {
 private static final String MOD=EssenceIndustry.MOD_ID;
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD);
 private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,MOD);
 public static final DeferredHolder<DataComponentType<?>,DataComponentType<BulkCellData>> CONTENTS=COMPONENTS.register("bulk_cell_contents",()->DataComponentType.<BulkCellData>builder().persistent(BulkCellData.CODEC).networkSynchronized(BulkCellData.STREAM_CODEC).build());
 public static final DeferredHolder<Item,BulkCellItem> ITEM=ITEMS.registerItem("bulk_cell",p->new BulkCellItem(p.stacksTo(1)));
 public static BulkCellData data(ItemStack stack) { return stack.getOrDefault(CONTENTS.get(),BulkCellData.EMPTY); }
 public static void register(IEventBus bus) { ITEMS.register(bus);COMPONENTS.register(bus);bus.addListener(BulkCells::setup);bus.addListener(BulkCells::creative);NeoForge.EVENT_BUS.addListener(BulkCells::tooltip); }
 private static void setup(FMLLoadCompleteEvent event) {
  event.enqueueWork(()->StorageCells.addCellHandler(new ICellHandler() {
   @Override public boolean isCell(ItemStack stack) { return stack.is(ITEM.get()); }
   @Override public StorageCell getCellInventory(ItemStack stack,ISaveProvider host) { return isCell(stack)?new BulkCellInventory(stack,host):null; }
  }));
 }
 private static void creative(BuildCreativeModeTabContentsEvent event) { if (event.getTabKey().equals(AECreativeTabIds.MAIN)) event.accept(ITEM.get()); }
 private static void tooltip(ItemTooltipEvent event) {
  if (!event.getItemStack().is(ITEM.get())) return;
  var data=data(event.getItemStack());var lines=event.getToolTip();
  lines.add(1,Component.translatable("tooltip."+MOD+".bulk_cell").withStyle(ChatFormatting.GRAY));
  if (!data.valid()) { lines.add(Component.translatable("tooltip."+MOD+".storage_recovery").withStyle(ChatFormatting.YELLOW));return; }
  lines.add(data.filter()==null?Component.translatable("tooltip."+MOD+".bulk_unconfigured").withStyle(ChatFormatting.YELLOW):Component.translatable("tooltip."+MOD+".bulk_selected",data.filter().getDisplayName()).withStyle(ChatFormatting.AQUA));
  if(data.storedKey()!=null) {
   lines.add(Component.translatable("tooltip."+MOD+".bulk_stored",data.storedKey().getDisplayName(),quantityText(data.quantity())).withStyle(ChatFormatting.GRAY));
   if(data.quantity().compareTo(java.math.BigInteger.valueOf(BulkCellData.CAPACITY))>0)lines.add(Component.translatable("tooltip."+MOD+".bulk_window").withStyle(ChatFormatting.DARK_GRAY));
  }
  if(data.mismatched())lines.add(Component.translatable("tooltip."+MOD+".bulk_mismatch").withStyle(ChatFormatting.YELLOW));
 }
 private static String quantityText(java.math.BigInteger amount) {
  String digits=amount.toString();
  if(digits.length()>48)return digits.substring(0,1)+"."+digits.substring(1,7)+"e"+(digits.length()-1);
  return String.format(java.util.Locale.ROOT,"%,d",amount);
 }
 private BulkCells() {}
}
