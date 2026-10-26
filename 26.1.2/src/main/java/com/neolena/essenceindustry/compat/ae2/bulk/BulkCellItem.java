package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.*;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.util.ConfigInventory;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
public final class BulkCellItem extends Item implements ICellWorkbenchItem {
 public BulkCellItem(Properties properties) { super(properties); }
 @Override public java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> getTooltipImage(ItemStack stack) {
  var data=BulkCells.data(stack);var key=data.key();
  if(!data.valid() || key==null)return java.util.Optional.empty();
  return java.util.Optional.of(new appeng.items.storage.StorageCellTooltipComponent(java.util.List.of(),java.util.List.of(new GenericStack(key,data.count())),false,false));
 }
 @Override public ConfigInventory getConfigInventory(ItemStack stack) { return new SelectionInventory(stack); }
 @Override public FuzzyMode getFuzzyMode(ItemStack stack) { return FuzzyMode.IGNORE_ALL; }
 @Override public void setFuzzyMode(ItemStack stack,FuzzyMode mode) {}
 private static final class SelectionInventory extends ConfigInventory {
  private final ItemStack cell;
  SelectionInventory(ItemStack cell) { super(Set.of(AEKeyType.items()),null,Mode.CONFIG_TYPES,1,null,false);this.cell=cell; }
  @Override public GenericStack getStack(int slot) { if(slot!=0)throw new IndexOutOfBoundsException(slot);var key=BulkCells.data(cell).filter();return key==null?null:new GenericStack(key,0); }
  @Override public AEKey getKey(int slot) { var value=getStack(slot);return value==null?null:value.what(); }
  @Override public long getAmount(int slot) { return 0; }
  @Override public boolean isEmpty() { return getStack(0)==null; }
  @Override public void setStack(int slot,GenericStack value) {
   if(slot!=0)throw new IndexOutOfBoundsException(slot);var current=BulkCells.data(cell);
   // Includes workbench clear/partition/copy and stale configuration handles.
   if(!current.valid() || cell.getCount()!=1)return;
   if(value!=null && !(value.what() instanceof AEItemKey))return;
   var key=value==null?null:(AEItemKey)value.what();
   if(key!=null && StorageCells.isCellHandled(key.toStack()))return;
   var next=current.withFilter(key);
   if(next!=null && !next.equals(current)) { cell.set(BulkCells.CONTENTS.get(),next);onChange(); }
  }
  @Override public void clear() { setStack(0,null); }
 }
}

