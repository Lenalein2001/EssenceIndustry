package com.neolena.essenceindustry.blueprint;
import com.neolena.essenceindustry.nuclear.*;
import com.neolena.essenceindustry.array.*;
import com.neolena.essenceindustry.construction.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import java.util.*;
/** Bounded work with one writer per controller; checked plans and supplies persist. */
public final class BlueprintWork {
 private static final int SCAN_VERSION=5;
 private static final Map<BlockEntity,java.lang.ref.WeakReference<BlueprintWork>> OWNERS=new WeakHashMap<>();
 public enum Status { IDLE,CHECKING,READY,BUILDING,MISSING,BLOCKED,UNLOADED,DENIED,PAUSED,COMPLETE,LOCKED,PREVIEW }
 private ServerPlayer player;private final ItemStack tool;private BlueprintMenu menu;
 private BlockEntity controller;private ServerLevel level;private BlockPos anchor,layoutAnchor;private Direction facing;private BlueprintSettings settings;private ReactorCoreDesign design;
 private List<BlueprintPlan.Cell> cells=List.of();private final List<BlueprintPlan.Cell> pending=new ArrayList<>();
 private final LinkedHashMap<Item,Integer> needs=new LinkedHashMap<>();private int cursor,next;private boolean configure;
 private boolean resumeAfterCheck,buildRequested,closed,scanned,checkedForSupply,worldDirty,placing;
 private BlockPos watchedMin,watchedMax;private boolean controllerMode;
 private BlueprintMeSupply meSupply;
 private int originalWidth,originalDepth,originalHeight;
 private Status status=Status.IDLE;private BlockPos issue=BlockPos.ZERO;private boolean manualReview;
 public boolean manualReview(){return manualReview;}
 public void verifyManual(){check();if(status!=Status.CHECKING)return;if(!configure()){stop(Status.LOCKED,anchor);return;}rememberSize();status=Status.IDLE;manualReview=true;notifyController();}
 public BlueprintWork(ServerPlayer p,ItemStack t,BlueprintMenu m){player=p;tool=t;menu=m;controllerMode=m==null||m.controllerMode();}
 public boolean attach(ServerPlayer p,BlueprintMenu m){if(closed||menu!=null&&menu.stillValid(player)&&player!=p)return false;player=p;menu=m;return true;}
 public void detach(BlueprintMenu m){if(menu!=m)return;if(!controllerMode||!owns()){shutdown();return;}checkpoint();persist();menu=null;player=new net.neoforged.neoforge.common.util.FakePlayer(player.level(),player.getGameProfile());player.setPos(controller.getBlockPos().getCenter());}
 public void persist(){if(controller!=null&&owns())BlueprintStorage.save(controller);}
 private void rememberOperator(){if(controller!=null){controller.getPersistentData().putString("constructionOwner",player.getUUID().toString());controller.getPersistentData().putString("constructionOwnerName",player.getGameProfile().name());controller.setChanged();}}
 public void restoreAutomation(boolean resume,boolean paused){if(controller==null)return;if(resume){if(status!=Status.CHECKING)check();resumeAfterCheck=status==Status.CHECKING;buildRequested=resumeAfterCheck;controller.getPersistentData().putBoolean("constructionRunning",buildRequested);}else if(paused){
  // A paused migration may unload before its replacement scan finishes. Rehydrate
  // the plan before pausing again, so Build can resume even without a saved scan.
  if(settings==null||status==Status.IDLE)check();pause();
 }}
 public void worldChanged(BlockPos p){if(!placing&&watchedMin!=null&&p.getX()>=watchedMin.getX()&&p.getX()<=watchedMax.getX()&&p.getY()>=watchedMin.getY()&&p.getY()<=watchedMax.getY()&&p.getZ()>=watchedMin.getZ()&&p.getZ()<=watchedMax.getZ())worldDirty=true;}
 public void shutdown(){checkpoint();persist();if(meSupply!=null)meSupply.close();release();BlueprintAutomation.unwatch(this);closed=true;}
 public boolean claim(){
  if(closed)return false;var target=tool.get(MultiblockPlanner.TARGET.get());
  if(target==null||!target.dimension().equals(player.level().dimension())||!player.level().isLoaded(target.pos()))return false;
  var host=player.level().getBlockEntity(target.pos());if(host==null||menu!=null&&!menu.stillValid(player))return false;
  var reference=OWNERS.get(host);var owner=reference==null?null:reference.get();
  if(owner!=null&&owner!=this&&!owner.closed){
   if(owner.menu!=null&&owner.player!=player&&owner.menu.stillValid(owner.player)){stop(Status.LOCKED,target.pos());return false;}
   owner.shutdown();
  }
  if(controller!=host){release();controller=host;}OWNERS.put(host,new java.lang.ref.WeakReference<>(this));return true;
 }
 private boolean owns(){var ref=OWNERS.get(controller);return !closed&&ref!=null&&ref.get()==this;}
 private void release(){if(owns())OWNERS.remove(controller);}
 public void reopen(){var target=tool.get(MultiblockPlanner.TARGET.get());if(target==null||!player.level().isLoaded(target.pos()))return;var host=player.level().getBlockEntity(target.pos());if(host==null)return;var saved=host.getPersistentData().getCompound("constructionScan").orElse(null);if(saved==null)return;check();if(status!=Status.CHECKING)return;
  if(!saved.getString("settings").orElse("").equals(settings.toString())||saved.getLong("anchor").orElse(Long.MIN_VALUE)!=layoutAnchor.asLong()||saved.getInt("facing").orElse(-1)!=facing.ordinal()||!savedDesignMatches(saved))return;
  int version=saved.getInt("version").orElse(0);
  // Reactor v5 integrates controls into every fuel column; v4 used separate controls.
  // Re-scan its old checkpoint; settings, supplied stock and existing world blocks stay intact.
  if(version<1||version>SCAN_VERSION||settings.kind()==0&&version<SCAN_VERSION)return;var indices=saved.getIntArray("pending").orElse(null);if(indices==null)return;var seen=new HashSet<Integer>();for(int i:indices)if(i<0||i>=cells.size()||!seen.add(i))return;
  for(int i:indices){var c=cells.get(i);pending.add(c);if(!c.state().isAir())needs.merge(c.state().getBlock().asItem(),1,Integer::sum);}
  cursor=cells.size();scanned=true;status=pending.isEmpty()&&configured()?Status.COMPLETE:Status.READY;
 }
 private boolean savedDesignMatches(net.minecraft.nbt.CompoundTag saved){var raw=saved.get("coreDesign");return design==null?raw==null:raw!=null&&ReactorCoreDesign.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,raw).result().filter(design::equals).isPresent();}
 public void checkpoint(){if(!owns()||!scanned||!settings.equals(PlannerItem.settings(tool))||!Objects.equals(design,tool.get(MultiblockPlanner.CORE_DESIGN.get())))return;var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("version",SCAN_VERSION);tag.putString("settings",settings.toString());if(design!=null)tag.put("coreDesign",ReactorCoreDesign.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,design).getOrThrow());tag.putLong("anchor",layoutAnchor.asLong());tag.putInt("facing",facing.ordinal());var indexes=new HashMap<BlueprintPlan.Cell,Integer>();for(int i=0;i<cells.size();i++)indexes.put(cells.get(i),i);tag.putIntArray("pending",pending.subList(Math.min(next,pending.size()),pending.size()).stream().mapToInt(c->indexes.get(c)).toArray());controller.getPersistentData().put("constructionScan",tag);controller.setChanged();}
 public boolean meEnabled(){return controller!=null&&controller.getPersistentData().getBoolean("constructionME").orElse(false);}
 public int meStatus(){return meSupply==null?0:meSupply.status();}
 public void toggleMe(){if(controller==null||!net.neoforged.fml.ModList.get().isLoaded("ae2"))return;rememberOperator();controller.getPersistentData().putBoolean("constructionME",!meEnabled());controller.setChanged();if(!meEnabled()&&meSupply!=null)meSupply.close();}
 public void close(){if(menu!=null)detach(menu);else shutdown();}
 private void supplyMe(){if(!meEnabled()||!net.neoforged.fml.ModList.get().isLoaded("ae2")||!(status==Status.READY||status==Status.MISSING||status==Status.BUILDING||status==Status.COMPLETE)||!live())return;if(!checkedForSupply){check();return;}if(meSupply==null)meSupply=new BlueprintMeSupply();meSupply.tick(player,controller,tool,this);if(buildRequested&&status==Status.MISSING&&next<pending.size()){var nextCell=pending.get(next);if(nextCell.state().isAir()||stored(nextCell.state().getBlock().asItem())>0||!level.getBlockState(position(nextCell)).isAir())status=Status.BUILDING;}}
 public void lockSupply(){stop(Status.LOCKED,anchor);}

 public Status status(){return status;}public BlockPos issue(){return issue;}public boolean busy(){return status==Status.CHECKING||status==Status.BUILDING;}
 public int progress(){return status==Status.CHECKING?cursor*100/Math.max(1,cells.size()):status==Status.BUILDING?next*100/Math.max(1,pending.size()):status==Status.COMPLETE?100:0;}
 public List<Item> materials(){return List.copyOf(needs.keySet());}public int needed(Item i){return needs.getOrDefault(i,0);}
 public Item nextMaterial(){for(int i=next;i<pending.size();i++){var state=pending.get(i).state();if(!state.isAir())return state.getBlock().asItem();}return Items.AIR;}
 /** A bounded working set in placement order, leaving half the bank free even for low-stack-limit blocks. */
 public Map<Item,Integer> supplyTargets(int blockLimit){
  var targets=new LinkedHashMap<Item,Integer>();var stackSizes=new HashMap<Item,Integer>();int stacks=0,remaining=Math.max(0,blockLimit),checks=0;
  for(int i=next;i<pending.size()&&remaining>0&&checks++<8192;i++){
   var state=pending.get(i).state();if(state.isAir())continue;var item=state.getBlock().asItem();int count=targets.getOrDefault(item,0),size=stackSizes.computeIfAbsent(item,key->Math.max(1,new ItemStack(key).getMaxStackSize()));
   if(count%size==0&&++stacks>BuilderWork.BANK_SLOTS/2)break;targets.put(item,count+1);remaining--;
  }
  return targets;
 }
 public int stored(Item i){return tool.getOrDefault(QuantumBuilder.BANK.get(),BuilderStock.EMPTY).entries().stream().filter(e->e.network().isEmpty()&&ItemStack.isSameItemSameComponents(e.item().create(),new ItemStack(i))).mapToInt(e->e.item().count()).sum();}
 public int inventory(Item i){int count=0;for(int n=0;n<36;n++){var s=player.getInventory().getItem(n);if(plain(s,i))count+=s.getCount();}return count;}
 private static boolean plain(ItemStack s,Item item){return !s.isEmpty()&&ItemStack.isSameItemSameComponents(s,new ItemStack(item));}
 public void invalidate(){scanned=false;checkedForSupply=false;worldDirty=false;resumeAfterCheck=false;buildRequested=false;if(meSupply!=null)meSupply.close();if(owns()){controller.getPersistentData().remove("constructionScan");controller.getPersistentData().putBoolean("constructionRunning",false);controller.getPersistentData().putBoolean("constructionPaused",false);controller.setChanged();}manualReview=false;status=Status.IDLE;needs.clear();pending.clear();cells=List.of();cursor=next=0;}
 public void pause(){buildRequested=false;resumeAfterCheck=false;if(meSupply!=null)meSupply.close();if(owns()){rememberOperator();controller.getPersistentData().putBoolean("constructionRunning",false);controller.getPersistentData().putBoolean("constructionPaused",true);controller.setChanged();}if(busy()||status==Status.READY||status==Status.MISSING){checkpoint();status=Status.PAUSED;}}
 private void stop(Status s,BlockPos p){status=s;issue=p.immutable();}
 private boolean idleController(){
  if(controller instanceof TurbineTile t)return t.available()&&!t.enabled()&&t.core().kinetic()<.01;
  if(controller instanceof ReactorTile r)return r.available()&&!r.core().enabled()&&!r.running();
  if(controller instanceof FuelProcessorTile f)return f.available()&&!f.hasJob()&&!f.running();
  if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile a)return a.ledger().jobs().isEmpty()&&!a.builder().busy()&&!a.diagnostic().equals("locked");return false;
 }
 private int width(){return controller instanceof TurbineTile t?t.width():controller instanceof ReactorTile r?r.width():net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile a?a.width():settings.width();}
 private int depth(){return controller instanceof TurbineTile t?t.depth():controller instanceof ReactorTile r?r.depth():net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile a?a.depth():settings.depth();}
 private int height(){return controller instanceof TurbineTile t?t.height():controller instanceof ReactorTile r?r.height():net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile a?a.height():settings.height();}
 private void rememberSize(){originalWidth=width();originalDepth=depth();originalHeight=height();}
 private boolean configured(){return width()==settings.width()&&depth()==settings.depth()&&height()==settings.height()&&(!(controller instanceof ReactorTile r)||r.activeCooling()==(settings.steam()==1));}
 private boolean live(){return owns()&&settings!=null&&settings.equals(PlannerItem.settings(tool))&&Objects.equals(design,tool.get(MultiblockPlanner.CORE_DESIGN.get()))&&ControllerPlacement.anchor(controller).equals(layoutAnchor)&&originalWidth==width()&&originalDepth==depth()&&originalHeight==height()&&(controllerMode||menu!=null&&menu.stillValid(player))&&player.isAlive()&&!player.isSpectator()&&player.level()==level&&level.isLoaded(anchor)&&level.getBlockEntity(anchor)==controller&&!tool.has(QuantumBuilder.RECOVERY.get())&&!BlueprintRepair.pending(controller)&&idleController()&&face(controller)==facing&&!BuilderWork.busy(player);}
 private static Direction face(BlockEntity b){if(b instanceof TurbineTile t)return t.facing();if(b instanceof ReactorTile r)return r.facing();if(b instanceof FuelProcessorTile f)return f.facing();return ArrayLayout.facing(b.getBlockState());}
 public void check(){
  if(closed)return;
  var claimTarget=tool.get(MultiblockPlanner.TARGET.get());if(claimTarget!=null&&claimTarget.dimension().equals(player.level().dimension())&&player.level().isLoaded(claimTarget.pos())&&!claim())return;
  invalidate();settings=PlannerItem.settings(tool);design=tool.get(MultiblockPlanner.CORE_DESIGN.get());
  var target=tool.get(MultiblockPlanner.TARGET.get());if(target==null||!target.dimension().equals(player.level().dimension())){status=Status.LOCKED;return;}
  level=player.level();anchor=target.pos();if(!level.isLoaded(anchor)){stop(Status.UNLOADED,anchor);return;}controller=level.getBlockEntity(anchor);
  boolean correct=settings.kind()==5&&controller instanceof TurbineTile||settings.kind()==0&&controller instanceof ReactorTile||settings.fixed()&&controller instanceof FuelProcessorTile f&&f.machine().equals(BlueprintPlan.machine(settings.kind()))||settings.kind()==1&&net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile;
  if(!correct){stop(Status.LOCKED,anchor);return;}facing=face(controller);layoutAnchor=ControllerPlacement.anchor(controller);rememberSize();if(!live()){stop(Status.LOCKED,anchor);return;}
  try{cells=BlueprintPlan.create(settings,facing,design);if(!layoutAnchor.equals(anchor)){var adjusted=new ArrayList<BlueprintPlan.Cell>();for(var c:cells){if(c.state().getBlock()==controller.getBlockState().getBlock()&&!BlueprintPlan.position(layoutAnchor,facing,settings,c.local()).equals(anchor)){var casing=settings.kind()==1?ProcessingArray.CASING.get():NuclearReactor.block(settings.fixed()?"pressure_panel":"casing");adjusted.add(new BlueprintPlan.Cell(c.local(),casing.defaultBlockState()));}else adjusted.add(c);}cells=List.copyOf(adjusted);}}catch(IllegalArgumentException ex){stop(Status.LOCKED,anchor);return;}
  if(design!=null&&!design.validationIssue().isEmpty()){stop(Status.BLOCKED,anchor);return;}
  var a=BlueprintPlan.position(layoutAnchor,facing,settings,BlockPos.ZERO);var b=BlueprintPlan.position(layoutAnchor,facing,settings,new BlockPos(settings.width()-1,settings.height()-1,settings.depth()-1));
  var min=new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));var max=new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));if(NuclearOwners.conflict(level,anchor,min,max)){stop(Status.LOCKED,anchor);return;}
  for(var other:level.getServer().getPlayerList().getPlayers())if(other!=player&&other.containerMenu instanceof BlueprintMenu m&&m.work()!=null&&m.work().busy()&&m.work().controller==controller){stop(Status.LOCKED,anchor);return;}
  watchedMin=min;watchedMax=max;BlueprintAutomation.watch(this,level,min,max);configure=true;status=Status.CHECKING;
 }
 private BlockPos position(BlueprintPlan.Cell c){return BlueprintPlan.position(layoutAnchor,facing,settings,c.local());}
 public static boolean matches(BlockState actual,BlockState expected){if(actual.isAir())return expected.isAir();if(actual.getBlock()!=expected.getBlock())return false;
  // Array shell and rack facing is assigned by formation solely for rendering.
  boolean visualFacing=net.neoforged.fml.ModList.get().isLoaded("ae2")&&(expected.is(com.neolena.essenceindustry.array.ProcessingArray.CASING.get())||expected.is(com.neolena.essenceindustry.array.ProcessingArray.GLASS.get())||expected.is(com.neolena.essenceindustry.array.ProcessingArray.RACK.get()));
  for(var p:expected.getProperties())if((!visualFacing&&p.getName().equals("facing")||p.getName().equals("axis")||p.getName().equals("output"))&&!actual.getValue(p).equals(expected.getValue(p)))return false;return true;
 }
 private boolean safe(BlockPos p){if(p.getY()<level.getMinY()||p.getY()>=level.getMaxY()||!level.getWorldBorder().isWithinBounds(p)){stop(Status.DENIED,p);return false;}if(!level.isLoaded(p)){stop(Status.UNLOADED,p);return false;}return true;}
 private boolean configure(){
  if(controller instanceof TurbineTile t)return t.configure(settings);
  if(controller instanceof ReactorTile r){for(int axis=0;axis<3;axis++){int desired=axis==0?settings.width():axis==1?settings.depth():settings.height();for(int j=0;j<64;j++){int current=axis==0?r.width():axis==1?r.depth():r.height();if(current==desired)break;r.adjustSize(axis,Integer.signum(desired-current));}if((axis==0?r.width():axis==1?r.depth():r.height())!=desired)return false;}if(r.activeCooling()!=(settings.steam()==1))r.control(5);return r.activeCooling()==(settings.steam()==1);}
  if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile a)return a.configureBuildSize(settings.width(),settings.depth(),settings.height());
  return true;
 }
 public void supply(){if(status!=Status.READY&&status!=Status.MISSING)return;if(!live()){stop(Status.LOCKED,anchor);return;}for(var entry:needs.entrySet()){int want=Math.max(0,entry.getValue()-stored(entry.getKey()));for(int n=0;n<36&&want>0;n++){var stack=player.getInventory().getItem(n);if(!plain(stack,entry.getKey()))continue;int amount=Math.min(want,stack.getCount());while(amount>0&&!BuilderWork.canAdd(tool,stack,amount,Optional.empty()))amount--;if(amount>0&&BuilderWork.add(tool,stack,amount)){stack.shrink(amount);want-=amount;}}}player.getInventory().setChanged();status=Status.READY;}
 public void start(){if(status!=Status.READY&&status!=Status.MISSING&&status!=Status.PAUSED)return;if(!live()){stop(Status.LOCKED,anchor);return;}if(configure&&!configure()){stop(Status.LOCKED,anchor);return;}rememberSize();configure=false;rememberOperator();check();resumeAfterCheck=status==Status.CHECKING;buildRequested=resumeAfterCheck;controller.getPersistentData().putBoolean("constructionRunning",buildRequested);controller.getPersistentData().putBoolean("constructionPaused",false);controller.setChanged();}
 public void returnItems(){if(controllerMode&&!claim())return;pause();if(meEnabled()&&net.neoforged.fml.ModList.get().isLoaded("ae2")){BlueprintReturns.returnExcess(player,controller,tool,Map.of(),8192);persist();}else BuilderWork.returnItems(player,tool);}
 public int[] tick(int checkBudget,int placeBudget){int checked=0,placed=0;if(closed)return new int[]{0,0};if(menu==null&&controller!=null)player.setPos(controller.getBlockPos().getCenter());if((worldDirty&&(scanned||busy()||buildRequested)||status==Status.UNLOADED&&level!=null&&level.getGameTime()%20==0&&areaLoaded())&&status!=Status.PAUSED){boolean resume=buildRequested;check();resumeAfterCheck=resume&&status==Status.CHECKING;buildRequested=resumeAfterCheck;if(controller!=null)controller.getPersistentData().putBoolean("constructionRunning",buildRequested);}supplyMe();if(!busy())return new int[]{0,0};if(!live()){stop(Status.LOCKED,anchor);return new int[]{0,0};}
  if(status==Status.CHECKING){while(cursor<cells.size()&&checked++<checkBudget){var c=cells.get(cursor++);var p=position(c);if(!safe(p))break;if(p.equals(anchor))continue;var actual=level.getBlockState(p);if(matches(actual,c.state()))continue;if(!level.getFluidState(p).isEmpty()||!actual.isAir()&&!BlueprintRepair.canRemove(player,controller,tool,p,actual)){stop(Status.BLOCKED,p);break;}pending.add(c);if(!c.state().isAir())needs.merge(c.state().getBlock().asItem(),1,Integer::sum);}
   if(status==Status.CHECKING&&cursor==cells.size()){scanned=true;checkedForSupply=true;status=pending.isEmpty()&&configured()?Status.COMPLETE:Status.READY;if(status==Status.COMPLETE)notifyController();if(resumeAfterCheck&&status==Status.READY){resumeAfterCheck=false;configure=false;status=Status.BUILDING;}checkpoint();}return new int[]{Math.min(checked,checkBudget),0};}
  while(next<pending.size()&&placed<placeBudget){var c=pending.get(next);var p=position(c);if(!safe(p))break;var actual=level.getBlockState(p);if(matches(actual,c.state())){decrement(c);next++;continue;}
   if(!actual.isAir()){
    BlueprintRepair.Result repaired;try{placing=true;repaired=BlueprintRepair.remove(player,controller,tool,p,actual);}finally{placing=false;}
    if(repaired!=BlueprintRepair.Result.REMOVED){stop(switch(repaired){case DENIED->Status.DENIED;case FULL->Status.MISSING;case LOCKED->Status.LOCKED;default->Status.BLOCKED;},p);break;}
    placed++;if(c.state().isAir()){decrement(c);next++;continue;}if(placed>=placeBudget)break;
   }
   if(!level.getFluidState(p).isEmpty()){stop(Status.BLOCKED,p);break;}if(c.state().isAir()){decrement(c);next++;continue;}var material=new ItemStack(c.state().getBlock());
   if(!level.mayInteract(player,p)||!player.mayUseItemAt(p,Direction.UP,material)){stop(Status.DENIED,p);break;}
   if(!level.isUnobstructed(c.state(),p,net.minecraft.world.phys.shapes.CollisionContext.empty())){stop(Status.BLOCKED,p);break;}
   if(!BuilderWork.removeStock(tool,material,1,Optional.empty())){stop(Status.MISSING,p);break;}
   var snapshot=BlockSnapshot.create(level.dimension(),level,p);placed++;boolean accepted=false;
   try{placing=true;if(!level.setBlock(p,c.state(),Block.UPDATE_ALL)){stop(Status.BLOCKED,p);break;}var event=new BlockEvent.EntityPlaceEvent(snapshot,level.getBlockState(p.below()),player);NeoForge.EVENT_BUS.post(event);if(event.isCanceled()){snapshot.restore();stop(Status.DENIED,p);break;}accepted=level.getBlockState(p).is(c.state().getBlock());if(!accepted){stop(Status.BLOCKED,p);break;}}
   catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(getClass()).error("Planner stopped at {}",p,ex);stop(Status.BLOCKED,p);break;}
   finally{placing=false;if(!accepted&&!level.getBlockState(p).is(c.state().getBlock()))BuilderWork.add(tool,material,1);player.getInventory().setChanged();}
   decrement(c);next++;
  }
  if(status==Status.BUILDING&&next==pending.size()){notifyController();check();}return new int[]{0,placed};
 }
 private boolean areaLoaded(){if(level==null||watchedMin==null)return false;for(int x=watchedMin.getX()>>4;x<=watchedMax.getX()>>4;x++)for(int z=watchedMin.getZ()>>4;z<=watchedMax.getZ()>>4;z++)if(!level.hasChunkAt(new BlockPos(x<<4,anchor.getY(),z<<4)))return false;return true;}
 private void decrement(BlueprintPlan.Cell c){needs.computeIfPresent(c.state().getBlock().asItem(),(i,n)->Math.max(0,n-1));}
 private void notifyController(){if(controller instanceof TurbineTile t)t.partChanged(anchor);else if(controller instanceof ReactorTile r)r.control(4);else if(controller instanceof FuelProcessorTile f)f.partChanged(anchor);else if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&controller instanceof ArrayTile a)a.action(0,player);}
}
