package com.neolena.essenceindustry.construction;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** Server-only bounded work. Every unused/refunded item remains in the item's persistent bank. */
public final class BuilderWork {
 public static final int PER_PLAYER=128,GLOBAL=512,BANK_SLOTS=128;
 private static final class Job {
  final ServerPlayer player;final ServerLevel level;final ItemStack tool;final BuilderSettings settings;final BlockState selection,state;final ArrayDeque<BlockPos> positions;
  final boolean undo,paid;final Direction face;final Optional<GlobalPos> link;final List<BuilderJournal.Placed> journal=new ArrayList<>();final ItemStack material;int placed,skipped;
  Job(ServerPlayer p,ItemStack t,List<BlockPos> positions,boolean undo,Direction face){this.face=face;link=Optional.ofNullable(t.get(QuantumBuilder.LINK.get()));player=p;level=p.level();tool=t;settings=BuilderItem.settings(t);selection=t.get(QuantumBuilder.MATERIAL.get());state=selection;this.positions=new ArrayDeque<>(positions);this.undo=undo;paid=!p.isCreative();material=state==null?ItemStack.EMPTY:new ItemStack(state.getBlock());}
 }
 private static final Set<UUID> ACTIVE=new HashSet<>();
 private static final LinkedHashMap<UUID,Job> JOBS=new LinkedHashMap<>();
 public static boolean busy(ServerPlayer p){return JOBS.containsKey(p.getUUID())||ACTIVE.contains(p.getUUID());}
 /** Building Gadget-style selection: keep orientation/shape, reset transient or resource-bearing properties. */
 public static BlockState selectedState(BlockState state){
  var defaults=state.getBlock().defaultBlockState();
  for(var property:state.getProperties()){
   var name=property.getName();
   if(name.startsWith("age")||Set.of("stage","hatch","honey_level","berries","dusted","charges","waterlogged","lit","has_record","has_book","open").contains(name))state=resetProperty(state,defaults,property);
  }
  if(state.hasProperty(BlockStateProperties.CHEST_TYPE))state=state.setValue(BlockStateProperties.CHEST_TYPE,ChestType.SINGLE);
  return state;
 }
 private static <T extends Comparable<T>> BlockState resetProperty(BlockState state,BlockState defaults,Property<T> property){return state.setValue(property,defaults.getValue(property));}
 public static boolean safeMaterial(BlockState s){
  if(s==null||s.isAir()||s.getBlock() instanceof GameMasterBlock||!s.getFluidState().isEmpty()||!(s.getBlock().asItem() instanceof BlockItem)||s.getBlock().defaultDestroyTime()<0)return false;
  if(((BlockItem)s.getBlock().asItem()).getBlock()!=s.getBlock())return false;
  for(var property:s.getProperties()){
   String name=property.getName();
   if((name.startsWith("age")||java.util.Set.of("stage","hatch","honey_level","berries","dusted","charges").contains(name))&&!s.getValue(property).equals(s.getBlock().defaultBlockState().getValue(property)))return false;
  }
  // Multi-part blocks need coordinated placement/removal. Never create half a bed/door or refund two slabs as one.
  if(s.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)||s.hasProperty(BlockStateProperties.BED_PART)||s.hasProperty(BlockStateProperties.WATERLOGGED)&&s.getValue(BlockStateProperties.WATERLOGGED))return false;
  if(s.hasProperty(BlockStateProperties.SLAB_TYPE)&&s.getValue(BlockStateProperties.SLAB_TYPE)==SlabType.DOUBLE)return false;
  if(s.hasProperty(BlockStateProperties.LAYERS)&&s.getValue(BlockStateProperties.LAYERS)>1||s.hasProperty(BlockStateProperties.CANDLES)&&s.getValue(BlockStateProperties.CANDLES)>1||s.hasProperty(BlockStateProperties.EGGS)&&s.getValue(BlockStateProperties.EGGS)>1||s.hasProperty(BlockStateProperties.PICKLES)&&s.getValue(BlockStateProperties.PICKLES)>1)return false;
  return true;
 }
 public static void start(ServerPlayer p,ItemStack tool,BlockPos clicked,Direction face){
  var anchor=tool.get(QuantumBuilder.ANCHOR.get());var heading=p.getDirection();
  if(anchor!=null){if(!anchor.origin().dimension().equals(p.level().dimension())||anchor.origin().pos().distToCenterSqr(p.position())>64*64){message(p,"anchor_far");return;}clicked=anchor.origin().pos();face=anchor.face();heading=anchor.heading();}
  if(busy(p)||tool.has(QuantumBuilder.RECOVERY.get())||!p.mayBuild()||p.isSpectator()||anchor==null&&!p.isWithinBlockInteractionRange(clicked,0)||!p.level().hasChunkAt(clicked))return;
  var state=tool.get(QuantumBuilder.MATERIAL.get());if(state!=null){state=selectedState(state);tool.set(QuantumBuilder.MATERIAL.get(),state);}if(!safeMaterial(state)){message(p,"select");return;}
  var positions=BuilderPlan.create(p.level(),clicked,face,heading,BuilderItem.settings(tool));
  if(positions.isEmpty()){message(p,"plan_large");return;}
  JOBS.put(p.getUUID(),new Job(p,tool,positions,false,face));
 }
 private static List<BuilderJournal> older(ItemStack tool){return tool.getOrDefault(QuantumBuilder.HISTORY.get(),BuilderHistory.EMPTY).operations();}
 public static int undoOperations(ItemStack tool){return older(tool).size()+(tool.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY).blocks().isEmpty()?0:1);}
 public static int undoBlocks(ItemStack tool){return older(tool).stream().mapToInt(j->j.blocks().size()).sum()+tool.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY).blocks().size();}
 private static void rememberCurrent(ItemStack tool){var current=tool.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY);if(current.blocks().isEmpty())return;var old=new ArrayList<>(older(tool));old.addFirst(current);while(old.size()>=BuilderHistory.MAX_OPERATIONS||old.stream().mapToInt(j->j.blocks().size()).sum()>BuilderHistory.MAX_BLOCKS)old.removeLast();tool.set(QuantumBuilder.HISTORY.get(),new BuilderHistory(old));}
 private static void latest(ItemStack tool,BuilderJournal current){var old=new ArrayList<>(older(tool));int total=current.blocks().size()+old.stream().mapToInt(j->j.blocks().size()).sum();while(total>BuilderHistory.MAX_BLOCKS&&!old.isEmpty())total-=old.removeLast().blocks().size();var history=new BuilderHistory(old);if(!history.equals(tool.getOrDefault(QuantumBuilder.HISTORY.get(),BuilderHistory.EMPTY)))tool.set(QuantumBuilder.HISTORY.get(),history);tool.set(QuantumBuilder.JOURNAL.get(),current);}
 private static void promote(ItemStack tool){if(!tool.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY).blocks().isEmpty()||older(tool).isEmpty())return;var old=new ArrayList<>(older(tool));tool.set(QuantumBuilder.JOURNAL.get(),old.removeFirst());tool.set(QuantumBuilder.HISTORY.get(),new BuilderHistory(old));}
 public static void undo(ServerPlayer p,ItemStack tool){
  if(busy(p)||tool.has(QuantumBuilder.RECOVERY.get())||!p.mayBuild()||p.isSpectator())return;promote(tool);var j=tool.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY);
  if(j.origin().isEmpty()||!j.owner().equals(p.getUUID().toString())||j.origin().get().dimension()!=p.level().dimension()){message(p,"no_undo");return;}
  var positions=new ArrayList<BlockPos>();for(var b:j.blocks())positions.add(b.pos());Collections.reverse(positions);
  JOBS.put(p.getUUID(),new Job(p,tool,positions,true,Direction.UP));
 }
 public static void cancel(ServerPlayer p){var j=JOBS.remove(p.getUUID());if(j!=null)finish(j);}
 private static void message(ServerPlayer p,String key,Object... args){p.sendOverlayMessage(Component.translatable("gui.essenceindustry.builder."+key,args));}
 private static BuilderStock stock(ItemStack tool){return tool.getOrDefault(QuantumBuilder.BANK.get(),BuilderStock.EMPTY);}
 public static int buffered(ItemStack tool){return stock(tool).entries().stream().mapToInt(e->e.item().count()).sum();}
 public static boolean canAdd(ItemStack tool,ItemStack material,int amount,Optional<GlobalPos> network){return add(tool.copy(),material,amount,network);}
 public static boolean add(ItemStack tool,ItemStack material,int amount){return add(tool,material,amount,Optional.empty());}
 public static boolean add(ItemStack tool,ItemStack material,int amount,Optional<GlobalPos> network){
  if(amount<=0)return true;if(material.isEmpty())return false;var entries=new ArrayList<>(stock(tool).entries());int room=(BANK_SLOTS-entries.size())*material.getMaxStackSize();
  for(var e:entries)if(e.network().equals(network)&&ItemStack.isSameItemSameComponents(e.item().create(),material))room+=Math.max(0,material.getMaxStackSize()-e.item().count());if(room<amount)return false;
  for(int i=0;i<entries.size()&&amount>0;i++){var e=entries.get(i);if(e.network().equals(network)&&ItemStack.isSameItemSameComponents(e.item().create(),material)){int n=Math.min(amount,Math.max(0,material.getMaxStackSize()-e.item().count()));entries.set(i,new BuilderStock.Entry(e.item().withCount(e.item().count()+n),network));amount-=n;}}
  while(amount>0){int n=Math.min(amount,material.getMaxStackSize());entries.add(new BuilderStock.Entry(ItemStackTemplate.fromNonEmptyStack(material.copyWithCount(n)),network));amount-=n;}
  tool.set(QuantumBuilder.BANK.get(),new BuilderStock(entries));return true;
 }
 public static boolean removeStock(ItemStack tool,ItemStack material,int count,Optional<GlobalPos> network){var entries=new ArrayList<>(stock(tool).entries());int available=0;for(var e:entries)if(e.network().equals(network)&&ItemStack.isSameItemSameComponents(e.item().create(),material))available+=e.item().count();if(available<count)return false;
  for(int i=0;i<entries.size()&&count>0;i++){var e=entries.get(i);if(!e.network().equals(network)||!ItemStack.isSameItemSameComponents(e.item().create(),material))continue;int n=Math.min(count,e.item().count());count-=n;if(n==e.item().count()){entries.remove(i--);}else entries.set(i,new BuilderStock.Entry(e.item().withCount(e.item().count()-n),network));}
  tool.set(QuantumBuilder.BANK.get(),new BuilderStock(entries));return true;
 }
 private static BuilderStock.Entry available(Job j){for(int pass=0;pass<2;pass++){Optional<GlobalPos> origin=pass==0?Optional.empty():j.link;if(pass==0&&j.settings.source()==1||pass==1&&(j.settings.source()==0||origin.isEmpty()))continue;
  for(var e:stock(j.tool).entries())if(e.network().equals(origin)&&ItemStack.isSameItemSameComponents(e.item().create(),j.material))return e;
 }return null;}
 private static boolean supply(Job j){
  if(!j.paid||available(j)!=null)return true;
  if(j.settings.source()!=1){var inv=j.player.getInventory();for(int i=0;i<36;i++){var s=inv.getItem(i);if(!s.isEmpty()&&ItemStack.isSameItemSameComponents(s,j.material)){int n=Math.min(s.getCount(),Math.min(64,j.positions.size()+1));if(!add(j.tool,j.material,n))return false;s.shrink(n);inv.setChanged();return true;}}}
  return j.settings.source()!=0&&net.neoforged.fml.ModList.get().isLoaded("ae2")&&BuilderMe.supply(j.player,j.tool,j.material,Math.min(64,j.positions.size()+1));
 }
 /** Local materials return to the player; ME materials return only to their original network. */
 public static void returnItems(ServerPlayer p,ItemStack tool){if(busy(p)||tool.has(QuantumBuilder.RECOVERY.get()))return;
  if(net.neoforged.fml.ModList.get().isLoaded("ae2"))BuilderMe.returnNetwork(p,tool);
  if(tool.has(QuantumBuilder.RECOVERY.get()))return;var entries=new ArrayList<>(stock(tool).entries());var inv=p.getInventory();
  for(int at=0;at<entries.size();at++){var e=entries.get(at);if(e.network().isPresent())continue;var stack=e.item().create();
   for(int pass=0;pass<2;pass++)for(int i=0;i<36&&!stack.isEmpty();i++){var existing=inv.getItem(i);if(pass==0&&(existing.isEmpty()||!ItemStack.isSameItemSameComponents(existing,stack))||pass==1&&!existing.isEmpty())continue;int n=Math.min(stack.getCount(),Math.max(0,stack.getMaxStackSize()-existing.getCount()));if(n>0){if(existing.isEmpty())inv.setItem(i,stack.copyWithCount(n));else existing.grow(n);stack.shrink(n);}}
   if(stack.isEmpty())entries.remove(at--);else entries.set(at,new BuilderStock.Entry(ItemStackTemplate.fromNonEmptyStack(stack),Optional.empty()));
  }tool.set(QuantumBuilder.BANK.get(),new BuilderStock(entries));inv.setChanged();
 }
 private static boolean position(Job j,BlockPos pos){return !j.level.isOutsideBuildHeight(pos)&&j.level.getWorldBorder().isWithinBounds(pos)&&j.level.hasChunkAt(pos)&&pos.distToCenterSqr(j.player.position())<=64*64&&j.level.mayInteract(j.player,pos)&&j.player.mayUseItemAt(pos,j.face,j.tool);}
 private static boolean build(Job j,BlockPos pos){
  if(j.level.captureBlockSnapshots||j.level.restoringBlockSnapshots)return false;
  if(!position(j,pos)||!j.level.getBlockState(pos).isAir()){j.skipped++;return true;}
  for(var direction:Direction.values())if(!j.level.hasChunkAt(pos.relative(direction))){j.skipped++;return true;}
  var placeState=Block.updateFromNeighbourShapes(j.state,j.level,pos);if(placeState.isAir()||placeState.getBlock()!=j.state.getBlock()){j.skipped++;return true;}
  if(!position(j,pos)||!j.level.getBlockState(pos).isAir()||!placeState.canSurvive(j.level,pos)||!j.level.isUnobstructed(placeState,pos,CollisionContext.of(j.player))){j.skipped++;return true;}
  if(!supply(j)){message(j.player,"missing");return false;}
  var spent=j.paid?available(j):null;
  if(j.paid&&(spent==null||!removeStock(j.tool,j.material,1,spent.network())))return false;
  var snapshot=BlockSnapshot.create(j.level.dimension(),j.level,pos);boolean accepted=false;Optional<String> placementData=Optional.empty();
  try {
   // Use NeoForge snapshot capture to defer onPlace/physics until protection accepts the block.
   int mark=j.level.capturedBlockSnapshots.size();boolean placed;java.util.List<BlockSnapshot> captured;
   j.level.captureBlockSnapshots=true;
   try{placed=j.level.setBlock(pos,placeState,Block.UPDATE_ALL);}
   finally{j.level.captureBlockSnapshots=false;captured=new ArrayList<>(j.level.capturedBlockSnapshots.subList(mark,j.level.capturedBlockSnapshots.size()));j.level.capturedBlockSnapshots.subList(mark,j.level.capturedBlockSnapshots.size()).clear();}
   if(placed&&captured.size()==1){
    snapshot=captured.getFirst();placementData=BuilderBlockData.fingerprint(j.level,pos);var event=new BlockEvent.EntityPlaceEvent(snapshot,j.level.getBlockState(pos.relative(j.face.getOpposite())),j.player);NeoForge.EVENT_BUS.post(event);
    if(event.isCanceled())restore(j.level,captured);else accepted=j.level.getBlockState(pos).equals(placeState);
   }else if(!captured.isEmpty())restore(j.level,captured);
  }finally{
   // On cancellation/failure only refund if nothing remains at the target. Never duplicate a placed block on an exception.
   if(!accepted&&j.level.getBlockState(pos).isAir()){if(j.paid&&!add(j.tool,j.material,1,spent.network()))throw new IllegalStateException("Builder reserved refund capacity lost");}
   else if(j.level.getBlockState(pos).equals(placeState)){
    if(j.journal.isEmpty())rememberCurrent(j.tool);
    j.journal.add(new BuilderJournal.Placed(pos.immutable(),placeState,spent==null?Optional.empty():spent.network(),placementData));latest(j.tool,new BuilderJournal(Optional.of(GlobalPos.of(j.level.dimension(),pos)),j.journal,Optional.of(ItemStackTemplate.fromNonEmptyStack(j.material)),j.player.getUUID().toString(),j.paid));j.placed++;
   }
  }
  if(accepted){var actual=j.level.getBlockState(pos);actual.onPlace(j.level,pos,snapshot.getState(),false);
   actual.getBlock().setPlacedBy(j.level,pos,actual,j.player,j.material.copyWithCount(1));
   j.level.markAndNotifyBlock(pos,j.level.getChunkAt(pos),snapshot.getState(),j.level.getBlockState(pos),snapshot.getFlags(),512);
   // Preserve the pre-callback baseline: items inserted by placement/neighbor hooks must never become Undo-safe.
   if(j.level.getBlockState(pos).getBlock()==placeState.getBlock()&&(placeState.hasBlockEntity()||!j.level.getBlockState(pos).equals(placeState))){for(int n=j.journal.size()-1;n>=0;n--)if(j.journal.get(n).pos().equals(pos)){var entry=j.journal.get(n);j.journal.set(n,new BuilderJournal.Placed(entry.pos(),j.level.getBlockState(pos),entry.network(),entry.data()));break;}
    latest(j.tool,new BuilderJournal(Optional.of(GlobalPos.of(j.level.dimension(),pos)),j.journal,Optional.of(ItemStackTemplate.fromNonEmptyStack(j.material)),j.player.getUUID().toString(),j.paid));}
  }else j.skipped++;return true;
 }
 private static void restore(ServerLevel level,List<BlockSnapshot> snapshots){level.restoringBlockSnapshots=true;try{for(int n=snapshots.size()-1;n>=0;n--)snapshots.get(n).restore(snapshots.get(n).getFlags()|Block.UPDATE_CLIENTS);}finally{level.restoringBlockSnapshots=false;}}
 private static boolean undoOne(Job job,BlockPos pos){
  var record=job.tool.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY);var entry=record.blocks().stream().filter(b->b.pos().equals(pos)).findFirst().orElse(null);if(entry==null)return true;
  if(!position(job,pos)){job.skipped++;return true;}
  var current=job.level.getBlockState(pos);
  if(current.equals(entry.state())){
   if(!BuilderBlockData.unchanged(job.level,entry)){job.skipped++;return true;}
   var event=new net.neoforged.neoforge.event.level.block.BreakBlockEvent(job.level,pos,current,job.player);NeoForge.EVENT_BUS.post(event);if(event.isCanceled()||!job.level.getBlockState(pos).equals(entry.state())||!BuilderBlockData.unchanged(job.level,entry)){job.skipped++;return true;}
   // Keep the refund in a persisted buffer before removing; no world drops or full-storage failure path.
   if(record.paid()&&!add(job.tool,record.materialStack(),1,entry.network())){message(job.player,"buffer_full");return false;}
   boolean removed=false;
   try{removed=job.level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
   finally{
    // A callback can throw either before or after removal. Reconcile actual state, never leave both block and refund.
    if(record.paid()&&!job.level.getBlockState(pos).isAir())removeStock(job.tool,record.materialStack(),1,entry.network());
    if(job.level.getBlockState(pos).isAir()){var left=new ArrayList<>(record.blocks());left.remove(entry);job.tool.set(QuantumBuilder.JOURNAL.get(),new BuilderJournal(record.origin(),left,record.material(),record.owner(),record.paid()));}
   }
   if(!removed)return false;job.placed++;
  }else job.skipped++;
  var left=new ArrayList<>(record.blocks());left.remove(entry);job.tool.set(QuantumBuilder.JOURNAL.get(),new BuilderJournal(record.origin(),left,record.material(),record.owner(),record.paid()));return true;
 }
 private static void finish(Job j){if(j.undo)promote(j.tool);message(j.player,j.undo?"undone":"complete",j.placed,j.skipped);j.player.getInventory().setChanged();}
 public static void tick(ServerTickEvent.Post event){if(net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return;int budget=GLOBAL,visits=JOBS.size();long deadline=System.nanoTime()+4_000_000;
  while(budget>0&&visits-->0&&!JOBS.isEmpty()&&System.nanoTime()<deadline){var first=JOBS.entrySet().iterator().next();var id=first.getKey();var j=first.getValue();JOBS.remove(id);
   var p=j.player;if(p.isRemoved()||!p.isAlive()||p.level().getServer().getPlayerList().getPlayer(p.getUUID())!=p||j.tool.has(QuantumBuilder.RECOVERY.get())||p.level()!=j.level||!p.mayBuild()||p.isSpectator()||p.getMainHandItem()!=j.tool&&p.getOffhandItem()!=j.tool){finish(j);continue;}
   if(!j.undo&&(!BuilderItem.settings(j.tool).equals(j.settings)||!Objects.equals(j.tool.get(QuantumBuilder.MATERIAL.get()),j.selection)||!Optional.ofNullable(j.tool.get(QuantumBuilder.LINK.get())).equals(j.link))){finish(j);continue;}
   int count=PER_PLAYER;boolean keep=true;
   ACTIVE.add(id);try{while(count-->0&&budget-->0&&!j.positions.isEmpty()&&System.nanoTime()<deadline){var pos=j.positions.removeFirst();if(!(j.undo?undoOne(j,pos):build(j,pos))){keep=false;break;}}}
   catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(BuilderWork.class).error("Quantum Builder operation stopped safely",ex);keep=false;}finally{ACTIVE.remove(id);}
   if(j.undo&&!j.tool.has(QuantumBuilder.RECOVERY.get())&&net.neoforged.fml.ModList.get().isLoaded("ae2"))BuilderMe.returnNetwork(p,j.tool);
   if(keep&&!j.positions.isEmpty())JOBS.put(id,j);else finish(j);
  }
 }
 public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){JOBS.clear();ACTIVE.clear();}
 private BuilderWork(){}
}
