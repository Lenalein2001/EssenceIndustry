package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.*;
import java.math.BigInteger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;
/** No ticking; reads the owning item each time, including through stale drive handles. */
public final class BulkCellInventory implements StorageCell {
 private final ItemStack stack;private final ISaveProvider host;private boolean saveWarning;
 public BulkCellInventory(ItemStack stack,ISaveProvider host){this.stack=stack;this.host=host;}
 private boolean live(){return stack.is(BulkCells.ITEM.get()) && stack.getCount()==1;}
 @Override public long insert(AEKey key,long amount,Actionable mode,IActionSource source){
  MEStorage.checkPreconditions(key,amount,mode,source);if(!live() || !(key instanceof AEItemKey item))return 0;
  var data=BulkCells.data(stack);if(!data.accepts(item))return 0;
  long accepted=BulkCellData.MAX_QUANTITY.subtract(data.quantity()).min(BigInteger.valueOf(amount)).longValueExact();
  if(accepted>0 && mode==Actionable.MODULATE)update(data.withQuantity(data.quantity().add(BigInteger.valueOf(accepted))));return accepted;
 }
 @Override public long extract(AEKey key,long amount,Actionable mode,IActionSource source){
  MEStorage.checkPreconditions(key,amount,mode,source);if(!live())return 0;
  var data=BulkCells.data(stack);if(!data.valid() || !key.equals(data.storedKey()))return 0;
  long extracted=data.quantity().min(BigInteger.valueOf(amount)).longValueExact();
  if(extracted>0 && mode==Actionable.MODULATE)update(data.withQuantity(data.quantity().subtract(BigInteger.valueOf(extracted))));return extracted;
 }
 private void update(BulkCellData data){
  stack.set(BulkCells.CONTENTS.get(),data);
  // The transfer already committed. Throwing now would make its caller retry/refund accepted items.
  if(host!=null)try{host.saveChanges();}catch(RuntimeException error){if(!saveWarning){saveWarning=true;LoggerFactory.getLogger(BulkCellInventory.class).error("Bulk cell changed, but its host failed to mark it for saving",error);}}
 }
 @Override public void getAvailableStacks(KeyCounter out){
  if(!live())return;var data=BulkCells.data(stack);if(!data.valid() || data.storedKey()==null)return;
  long existing=out.get(data.storedKey());long room=existing<0?0:Long.MAX_VALUE-existing;
  out.add(data.storedKey(),Math.min(data.listedCount(),room));
 }
 @Override public boolean isPreferredStorageFor(AEKey key,IActionSource source){return live() && key instanceof AEItemKey item && BulkCells.data(stack).accepts(item);}
 @Override public CellState getStatus(){var data=BulkCells.data(stack);if(!live() || !data.valid() || data.filter()==null || data.mismatched() || data.quantity().equals(BulkCellData.MAX_QUANTITY))return CellState.FULL;return data.quantity().signum()==0?CellState.EMPTY:CellState.NOT_EMPTY;}
 @Override public double getIdleDrain(){return 1;}
 @Override public boolean canFitInsideCell(){var data=BulkCells.data(stack);return live()&&data.valid()&&data.quantity().signum()==0;}
 @Override public void persist(){}
 @Override public Component getDescription(){return stack.getHoverName();}
}

