package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.api.networking.*;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.storage.DriveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
/** Suggestions are cached; a slow AE tick keeps one empty cell in reserve. */
public final class DiskManagerTile extends AENetworkedBlockEntity implements appeng.api.networking.ticking.IGridTickable {
 public record Suggestion(AEItemKey key,long amount){}
 private record Snapshot(long time,List<Suggestion> rows,boolean limited){}
 private static final Map<IGrid,Snapshot> CACHE=new WeakHashMap<>();
 private List<Suggestion> suggestions=List.of();
 private boolean limited,reserved,transferLocked,refilling;
 public boolean reserved(){return reserved;}
 public appeng.api.networking.ticking.TickingRequest getTickingRequest(IGridNode node){return new appeng.api.networking.ticking.TickingRequest(20,100,false);}
 public appeng.api.networking.ticking.TickRateModulation tickingRequest(IGridNode node,int elapsed){refill();return appeng.api.networking.ticking.TickRateModulation.SLOWER;}
 public void refill(){if(reserved||transferLocked||refilling||!getMainNode().isActive()||isRemoved()||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return;
  var service=getMainNode().getGrid().getStorageService();refill(service.getInventory(),service::getCachedInventory);
 }
 private void refill(appeng.api.storage.MEStorage storage,java.util.function.Supplier<appeng.api.stacks.KeyCounter> inventory){
  if(reserved||transferLocked||refilling)return;refilling=true;
  var source=new appeng.me.helpers.MachineSource(this);
  try{
   var key=AEItemKey.of(BulkCells.ITEM.get());long available=storage.extract(key,1,appeng.api.config.Actionable.SIMULATE,source);
   if(available==0){
    // Workbench-cleared cells carry an explicit empty contents component. Do not consume named or configured cells.
    var contents=inventory.get();int visited=0;
    for(var entry:contents.findFuzzy(key,appeng.api.config.FuzzyMode.IGNORE_ALL)){if(++visited>10000)break;if(entry.getLongValue()<=0||!(entry.getKey() instanceof AEItemKey candidate)||candidate.getItem()!=BulkCells.ITEM.get())continue;
     var cell=candidate.toStack();var data=BulkCells.data(cell);cell.remove(BulkCells.CONTENTS.get());
     if(!data.valid()||data.filter()!=null||data.quantity().signum()!=0||!net.minecraft.world.item.ItemStack.isSameItemSameComponents(cell,new net.minecraft.world.item.ItemStack(BulkCells.ITEM.get())))continue;
     long n=storage.extract(candidate,1,appeng.api.config.Actionable.SIMULATE,source);if(n<0||n>1)throw new IllegalStateException("Invalid Bulk Cell simulation");if(n==1){key=candidate;available=1;break;}
    }
   }
   if(available<0||available>1)throw new IllegalStateException("Invalid Bulk Cell simulation");if(available==0||isRemoved())return;
   transferLocked=true;setChanged();long got=storage.extract(key,1,appeng.api.config.Actionable.MODULATE,source);
   if(got<0||got>1)throw new IllegalStateException("Invalid Bulk Cell extraction");reserved=got==1;transferLocked=false;
   if(reserved&&isRemoved()&&level!=null){reserved=false;net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,new net.minecraft.world.item.ItemStack(BulkCells.ITEM.get()));}
   setChanged();
  }catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(getClass()).error("Bulk manager reserve transfer stopped at {}",worldPosition,ex);}
  finally{refilling=false;}
 }
 @Override public void saveAdditional(net.minecraft.world.level.storage.ValueOutput out){super.saveAdditional(out);out.putBoolean("reservedBulkCell",reserved);out.putBoolean("bulkTransferLocked",transferLocked);}
 @Override public void loadTag(net.minecraft.world.level.storage.ValueInput in){super.loadTag(in);reserved=in.getBooleanOr("reservedBulkCell",false);transferLocked=in.getBooleanOr("bulkTransferLocked",false);}
 @Override public void clearContent(){super.clearContent();reserved=false;setChanged();}
 @Override public void addAdditionalDrops(net.minecraft.world.level.Level level,BlockPos pos,java.util.List<net.minecraft.world.item.ItemStack> drops){super.addAdditionalDrops(level,pos,drops);if(reserved)drops.add(new net.minecraft.world.item.ItemStack(BulkCells.ITEM.get()));}

 public DiskManagerTile(BlockPos pos,BlockState state){super(BulkDiskManager.TILE.get(),pos,state);getMainNode().setIdlePowerUsage(1).setFlags(GridFlags.REQUIRE_CHANNEL).addService(appeng.api.networking.ticking.IGridTickable.class,this);}
 public List<Suggestion> suggestions(){return suggestions;}
 public boolean limited(){return limited;}
 public boolean scan(){
  if(!getMainNode().isActive() || level==null || isRemoved()) { suggestions=List.of();return false; }
  var grid=getMainNode().getGrid();long now=level.getGameTime();var previous=CACHE.get(grid);
  if(previous!=null && now>=previous.time && now-previous.time<100){suggestions=previous.rows;limited=previous.limited;return true;}
  var excluded=new HashSet<AEItemKey>();int slots=0;boolean cap=false;
  outer:for(var type:grid.getMachineClasses())if(DriveBlockEntity.class.isAssignableFrom(type))for(var machine:grid.getMachines(type)){
   var drive=(DriveBlockEntity)machine;if(!drive.getMainNode().isActive())continue;
   var inv=drive.getInternalInventory();
   for(int i=0;i<inv.size();i++){
    if(++slots>20000){cap=true;break outer;}
    var cell=inv.getStackInSlot(i);if(!cell.is(BulkCells.ITEM.get()))continue;
    var data=BulkCells.data(cell);if(data.valid()){if(data.filter()!=null)excluded.add(data.filter());if(data.storedKey()!=null)excluded.add(data.storedKey());}
   }
  }
  var top=new PriorityQueue<Suggestion>(Comparator.comparingLong(Suggestion::amount));int keys=0;
  for(var entry:grid.getStorageService().getCachedInventory()){
   if(++keys>100000){cap=true;break;}
   if(!(entry.getKey() instanceof AEItemKey item) || entry.getLongValue()<=0 || excluded.contains(item))continue;
   if(top.size()>=20 && entry.getLongValue()<=top.peek().amount())continue;
   // Only materialize potential top rows, not every key in a large network.
   if(appeng.api.storage.StorageCells.isCellHandled(item.toStack()))continue;
   if(top.size()>=20)top.poll();top.add(new Suggestion(item,entry.getLongValue()));
  }
  var result=new ArrayList<>(top);result.sort(Comparator.comparingLong(Suggestion::amount).reversed().thenComparing(e->e.key().toString()));
  // Incomplete drive enumeration must never recommend a supposedly unconfigured item.
  suggestions=slots>20000?List.of():List.copyOf(result);limited=cap;CACHE.put(grid,new Snapshot(now,suggestions,cap));return true;
 }
 public boolean configure(Inventory inventory,int row){
  if(refilling||!getMainNode().isActive() || isRemoved() || row<0 || row>=suggestions.size())return false;
  var selected=BulkCellData.select(suggestions.get(row).key());if(selected==null||transferLocked)return false;
  refill();
  if(transferLocked||isRemoved())return false;
  if(reserved){int free=-1;for(int slot=0;slot<36;slot++)if(inventory.getItem(slot).isEmpty()){free=slot;break;}if(free>=0){
   var cell=new net.minecraft.world.item.ItemStack(BulkCells.ITEM.get());cell.set(BulkCells.CONTENTS.get(),selected);inventory.setItem(free,cell);reserved=false;setChanged();inventory.setChanged();inventory.player.inventoryMenu.broadcastChanges();getMainNode().ifPresent((grid,node)->grid.getTickManager().alertDevice(node));return true;
  }}
  for(int slot=0;slot<36;slot++){
   var cell=inventory.getItem(slot);if(!cell.is(BulkCells.ITEM.get()) || cell.getCount()!=1)continue;
   var data=BulkCells.data(cell);if(!data.valid() || data.quantity().signum()!=0 || data.filter()!=null)continue;
   // The same inventory slot becomes the result: full inventories cannot lose a cell.
   var configured=cell.copy();configured.set(BulkCells.CONTENTS.get(),selected);inventory.setItem(slot,configured);inventory.setChanged();inventory.player.inventoryMenu.broadcastChanges();return true;
  }
  return false;
 }
}
