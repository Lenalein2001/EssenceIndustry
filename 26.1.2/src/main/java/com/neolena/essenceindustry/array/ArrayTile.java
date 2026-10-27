package com.neolena.essenceindustry.array;
import appeng.api.config.*;
import appeng.api.crafting.*;
import appeng.api.implementations.blockentities.*;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.mojang.serialization.*;
import com.neolena.essenceindustry.item.InventoryRecovery;
import com.neolena.essenceindustry.library.SeedDataSafety;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;

public final class ArrayTile extends AENetworkedBlockEntity implements ICraftingProvider,ICraftingMachine {
 private final ArrayBuilder builder=new ArrayBuilder(this);
 public ArrayBuilder builder(){return builder;}
 void constructionChanged(){changed();}
 public static final int PATTERNS=36,MACHINES=36,TOOLS=52,SLOTS=56;
 private static final Set<ArrayTile> LOADED=Collections.newSetFromMap(new WeakHashMap<>());
 private static final Map<net.minecraft.server.MinecraftServer,ScanBudget> SCANS=new WeakHashMap<>();
 private static final class ScanBudget{long tick=Long.MIN_VALUE;int cells;}
 private ArrayLedger.State state=ArrayLedger.State.empty();private Dynamic<?> recovery,inventoryRecovery,upgradeRecovery;private int schema=1;
 private boolean scheduled,working,dirty=true,structureDirty=true,formed;private int width=5,depth=5,height=5,speed=1,racks,hatches,layer;
 private long lastCheck=-100,lastWork=Long.MIN_VALUE,lastFlush=-100,lastErrorLog=-1000,revision;private String problem="";private int problemX,problemY,problemZ;
 private ArrayStructureScan structureScan;
 private boolean visualFormed,guide,focusCheck,visualReconcile=true;private int visualHash;
 private java.util.List<BlockPos> visualRacks=java.util.List.of();
 public java.util.List<BlockPos> visualRacks(){return visualRacks;}
 private java.util.List<BlockPos> visualWindows=java.util.List.of();
 public java.util.List<BlockPos> visualWindows(){return visualWindows;}
 private long[] visualRackMask=new long[0];private int visualRackWidth,visualRackDepth;
 private boolean processingNow,clientMoving;private int clientInstalled,clientActive;private net.minecraft.world.item.Item clientMachine=Items.AIR;
 public boolean guide(){return guide;}
 public int scanProgress(){return structureScan==null?(structureDirty?0:100):100*structureScan.cursor/Math.max(1,width*depth*height);}
 public BlockPos layoutAnchor(){return com.neolena.essenceindustry.blueprint.ControllerPlacement.anchor(this);}
 private void discoverController(){if(!formed&&level!=null&&!level.isClientSide()&&com.neolena.essenceindustry.blueprint.ControllerPlacement.discoverWall(this,ArrayLayout.facing(getBlockState()),width,height,0,s->s.is(ProcessingArray.CASING.get())||s.is(ProcessingArray.GLASS.get())||s.is(ProcessingArray.HATCH.get())||s.is(ProcessingArray.PORT.get()))){ArrayOwners.update(this);syncVisuals();}}
 public BlockPos position(int x,int y,int z){return ArrayLayout.position(layoutAnchor(),ArrayLayout.facing(getBlockState()),x,y,z);}
 public ItemStack visualMachine(){if(level!=null&&level.isClientSide())return clientMachine==Items.AIR?ItemStack.EMPTY:new ItemStack(clientMachine);for(int i=MACHINES;i<TOOLS;i++)if(inventory.getAmountAsLong(i)>0)return inventory.getResource(i).toStack();return ItemStack.EMPTY;}
 public int visualInstalled(){return level!=null&&level.isClientSide()?clientInstalled:installed();}
 public boolean visualWorking(){return level!=null&&level.isClientSide()?clientMoving:processingNow;}
 public int visualActive(){return level!=null&&level.isClientSide()?clientActive:active();}
 @Override protected void writeToStream(net.minecraft.network.RegistryFriendlyByteBuf out){
  super.writeToStream(out);out.writeByte(5);out.writeBlockPos(layoutAnchor().subtract(worldPosition));out.writeVarInt(width);out.writeVarInt(depth);out.writeVarInt(height);out.writeBoolean(formed());out.writeBoolean(guide);out.writeBoolean(processingNow);out.writeByte(layer);out.writeVarInt(installed());out.writeVarInt(active());out.writeVarInt(BuiltInRegistries.ITEM.getId(visualMachine().getItem()));boolean fault=!structureDirty&&!problem.isEmpty();out.writeInt(fault?problemX:-1);out.writeInt(fault?problemY:-1);out.writeInt(fault?problemZ:-1);int words=visualRackMask.length;out.writeVarInt(words);for(int i=0;i<words;i++)out.writeLong(visualRackMask[i]);out.writeVarInt(visualWindows.size());for(var window:visualWindows){out.writeByte(window.getX());out.writeByte(window.getY());out.writeByte(window.getZ());}
 }
 @Override protected boolean readFromStream(net.minecraft.network.RegistryFriendlyByteBuf in){
  super.readFromStream(in);if(in.readUnsignedByte()!=5)throw new IllegalArgumentException("Unknown Array visual packet");var offset=in.readBlockPos();if(Math.abs(offset.getX())>64||Math.abs(offset.getY())>64||Math.abs(offset.getZ())>64)throw new IllegalArgumentException("Invalid Array anchor");com.neolena.essenceindustry.blueprint.ControllerPlacement.set(this,worldPosition.offset(offset));
  int w=in.readVarInt(),d=in.readVarInt(),h=in.readVarInt();boolean f=in.readBoolean(),g=in.readBoolean(),moving=in.readBoolean();int l=in.readUnsignedByte(),count=in.readVarInt(),a=in.readVarInt(),id=in.readVarInt(),x=in.readInt(),y=in.readInt(),z=in.readInt();
  var machine=BuiltInRegistries.ITEM.byId(id);if(w<5||w>64||d<5||d>64||h<5||h>33||h%2==0||l>=h||count<0||count>1024||a<0||a>1024||machine==null)throw new IllegalArgumentException("Invalid Array visual packet");
  int words=in.readVarInt();if(words<0||words>(ArrayBayLayout.columns(w)*ArrayBayLayout.columns(d)*((h-3)/2)+63)/64)throw new IllegalArgumentException("Oversized Array rack mask");
  var mask=new long[words];for(int i=0;i<words;i++)mask[i]=in.readLong();var positions=ArrayBayLayout.decode(mask,w,d,h);
  int windows=in.readVarInt();if(windows<0||windows>4*Math.max(w,d)*h)throw new IllegalArgumentException("Oversized Array window layout");var panes=new java.util.ArrayList<BlockPos>();var seen=new java.util.HashSet<BlockPos>();for(int i=0;i<windows;i++){var pane=new BlockPos(in.readUnsignedByte(),in.readUnsignedByte(),in.readUnsignedByte());if(!ArrayHullShapes.window(pane,w,d,h)||!seen.add(pane))throw new IllegalArgumentException("Invalid Array window position");panes.add(pane);}visualWindows=java.util.List.copyOf(panes);
  visualRackMask=mask;visualRacks=positions;visualRackWidth=w;visualRackDepth=d;
  width=w;depth=d;height=h;formed=f;structureDirty=false;guide=g;clientMoving=moving;layer=l;clientInstalled=count;clientActive=a;clientMachine=machine;problemX=x;problemY=y;problemZ=z;ArrayOwners.update(this);return true;
 }
 private void syncVisuals(){int hash=Objects.hash(layoutAnchor(),width,depth,height,formed(),guide,processingNow,layer,problemX,problemY,problemZ,installed(),active(),visualMachine().getItem(),Arrays.hashCode(visualRackMask),visualWindows);if(hash!=visualHash){visualHash=hash;markForUpdate();}}

 private Map<String,Integer> cachedCapacity=Map.of();private boolean capacityDirty=true;
 private Object recipeGeneration;private List<ArrayRecipes.Native> nativeRecipes=List.of();private List<IPatternDetails> advertised=List.of();private Map<AEItemKey,ArrayRecipes.Match> plans=Map.of();
 private final Set<ArraySlot> views=Collections.newSetFromMap(new WeakHashMap<>());
 private final Map<ItemResource,Long> safePatterns=new LinkedHashMap<>(64,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<ItemResource,Long> entry){return size()>64;}};
 private final Map<ItemResource,Long> residentPatternWeights=new HashMap<>();
 private void patternChanged(ItemStack old,ItemResource current){if(!old.isEmpty())residentPatternWeights.remove(ItemResource.of(old));if(!current.isEmpty())residentPatternWeights.put(current,patternWeight(current));}
 private final List<BlockPos> outputPorts=new ArrayList<>();
 private final ItemStacksResourceHandler inventory=new ItemStacksResourceHandler(SLOTS){
  @Override public boolean isValid(int i,ItemResource r){if(r.isEmpty())return false;var stack=r.toStack();if(i<PATTERNS)return acceptsPattern(0,i,r);if(!state.jobs().isEmpty())return false;return !structureDirty&&formed&&i<TOOLS&&acceptsMachine(stack)&&fitsPatternStorage(inventory,i,r);}
  @Override protected int getCapacity(int i,ItemResource resource){
   int normal=super.getCapacity(i,resource);if(i<PATTERNS)return Math.min(1,normal);
   if(i>=MACHINES&&i<TOOLS){long present=getAmountAsLong(i);return (int)Math.min(normal,Math.max(present,Math.max(0,(long)racks*8-installed()+present)));}
   return normal;
  }
  @Override protected void onContentsChanged(int i,ItemStack old){patternChanged(old,getResource(i));dirty=true;if(i>=MACHINES&&i<TOOLS){capacityDirty=true;requestStructureCheck();}changed();}
 };
 // Extra pages are allocated on demand; the original 56-slot save layout never moves.
 private static final int MAX_PATTERN_PAGES=64*64*33;
 private record StoredPattern(int slot,ItemStack stack){
  static final Codec<StoredPattern> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.create(i->i.group(Codec.intRange(36,MAX_PATTERN_PAGES*36-1).fieldOf("slot").forGetter(StoredPattern::slot),ItemStack.CODEC.fieldOf("stack").forGetter(StoredPattern::stack)).apply(i,StoredPattern::new));
 }
 private final NavigableMap<Integer,ItemStacksResourceHandler> extraPatterns=new TreeMap<>();
 private Dynamic<?> patternRecovery;private HolderLookup.Provider patternLookup;
 private long patternWeight(ItemResource r){return safePatterns.computeIfAbsent(r,key->SeedDataSafety.weight(key,level==null?(patternLookup==null?SeedDataSafety.builtinLookup():patternLookup):level.registryAccess()));}
 private long bankWeight(ItemStacksResourceHandler bank){long total=0;for(int i=0;i<bank.size();i++)if(bank.getAmountAsLong(i)>0){long n=residentPatternWeights.getOrDefault(bank.getResource(i),0L);if(n==0)n=patternWeight(bank.getResource(i));if(n<1)return SeedDataSafety.BUDGET*16;total+=n;}return total;}
 private long patternStorageWeight(){long total=bankWeight(inventory);for(var bank:extraPatterns.values())total+=bankWeight(bank);return total;}
 private boolean acceptsPattern(int page,int slot,ItemResource resource){
  if(structureDirty||page>=hatches||patternRecovery!=null||!PatternDetailsHelper.isEncodedPattern(resource.toStack()))return false;
  long weight=patternWeight(resource);if(weight<1||weight>65536)return false;
  var bank=page==0?inventory:extraPatterns.get(page);
  if((bank==null||bank.getAmountAsLong(slot)==0)&&storedPatterns()>=16384)return false;
  return fitsPatternStorage(bank,slot,resource);
 }
 private boolean fitsPatternStorage(ItemStacksResourceHandler bank,int slot,ItemResource resource){
  long weight=patternWeight(resource);if(weight<1)return false;
  long old=bank!=null&&bank.getAmountAsLong(slot)>0?patternWeight(bank.getResource(slot)):0;
  return (bank==null?0:bankWeight(bank))-Math.max(0,old)+weight<=SeedDataSafety.BUDGET/2&&patternStorageWeight()-Math.max(0,old)+weight<=SeedDataSafety.BUDGET*8;
 }
 // Refused shift-clicks must not allocate/probe a bank for every physical hatch.
 public boolean canAddPattern(ItemResource resource){
  if(!mayEdit()||structureDirty||hatches==0||resource.isEmpty()||!PatternDetailsHelper.isEncodedPattern(resource.toStack()))return false;
  long weight=patternWeight(resource);return weight>0&&weight<=65536&&storedPatterns()<16384&&patternStorageWeight()+weight<=SeedDataSafety.BUDGET*8;
 }
 public Component patternRejection(int page,ItemStack stack){
  String reason=!usable()?"pattern_locked":structureDirty?"pattern_checking":page>=hatches?"pattern_hatch_required":patternWeight(ItemResource.of(stack))<1?"pattern_bad_data":patternWeight(ItemResource.of(stack))>65536?"pattern_too_large":"pattern_full";
  return Component.translatable("gui.essenceindustry.array."+reason);
 }
 public ItemStacksResourceHandler patternInventory(int page){
  if(page<0||page>=MAX_PATTERN_PAGES)throw new IllegalArgumentException("Invalid Array pattern page");if(page==0)return inventory;
  return extraPatterns.computeIfAbsent(page,p->new ItemStacksResourceHandler(PATTERNS){
   @Override protected int getCapacity(int slot,ItemResource resource){return Math.min(1,super.getCapacity(slot,resource));}
   @Override public boolean isValid(int slot,ItemResource r){return !r.isEmpty()&&acceptsPattern(p,slot,r);}
   @Override protected void onContentsChanged(int slot,ItemStack old){patternChanged(old,getResource(slot));dirty=true;changed();}
  });
 }
 public int patternCapacity(){return hatches*PATTERNS;}
 public int patternPages(){int last=0;for(var entry:extraPatterns.entrySet())for(int i=0;i<PATTERNS;i++)if(entry.getValue().getAmountAsLong(i)>0){last=Math.max(last,entry.getKey());break;}return Math.max(1,Math.max(hatches,last+1));}
 public int storedPatterns(){int n=0;for(int i=0;i<PATTERNS;i++)if(inventory.getAmountAsLong(i)>0)n++;for(var bank:extraPatterns.values())for(int i=0;i<PATTERNS;i++)if(bank.getAmountAsLong(i)>0)n++;return n;}
 private List<StoredPattern> savedExtraPatterns(){var result=new ArrayList<StoredPattern>();for(var entry:extraPatterns.entrySet())for(int i=0;i<PATTERNS;i++){var bank=entry.getValue();if(bank.getAmountAsLong(i)>0)result.add(new StoredPattern(entry.getKey()*PATTERNS+i,bank.getResource(i).toStack(bank.getAmountAsInt(i))));}return result;}
 private final ItemStacksResourceHandler upgrades=new ItemStacksResourceHandler(3){
  @Override public boolean isValid(int i,ItemResource resource){return installationsEditable()&&ArrayUpgrades.valid(i,resource.toStack());}
  @Override public int extract(int i,ItemResource resource,int amount,TransactionContext transaction){if(!installationsEditable())return 0;return super.extract(i,resource,amount,transaction);}
  @Override protected void onContentsChanged(int i,ItemStack old){if(i==0)speed=maximumSpeed();changed();}
 };
 private final SimpleEnergyHandler energy=new SimpleEnergyHandler(ArrayUpgrades.BASE_CAPACITY,ArrayUpgrades.MAX_CAPACITY,ArrayUpgrades.MAX_CAPACITY){
  @Override public long getCapacityAsLong(){return energyCapacity();}
  @Override public int insert(int amount,TransactionContext transaction){capacity=energyCapacity();return super.insert(amount,transaction);}
  @Override protected void onEnergyChanged(int old){setChanged();wake();}
 };
 private final SnapshotJournal<ArrayLedger.State> journal=new SnapshotJournal<>(){protected ArrayLedger.State createSnapshot(){return state;}protected void revertToSnapshot(ArrayLedger.State s){state=s;}protected void onRootCommit(ArrayLedger.State before){if(!state.equals(before))changed();}};
 private Object cookingGeneration;private String cookingPool="";
 private final Map<ItemResource,Optional<ArrayLedger.Plan>> cookingPlans=new LinkedHashMap<>(32,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<ItemResource,Optional<ArrayLedger.Plan>> e){return size()>128;}};
 private ArrayLedger.Plan cookingPlan(ItemResource resource){
  if(!usable()||working||structureDirty||!formed||hatches==0||resource.isEmpty()||state.locked())return null;
  var types=capacity().keySet();if(types.size()!=1)return null;var pool=types.iterator().next();
  if(ArrayRecipes.cookingFamily(pool).isEmpty())return null;
  var generation=level.getServer().getRecipeManager().recipeMap();if(cookingGeneration!=generation||!cookingPool.equals(pool)){cookingPlans.clear();cookingGeneration=generation;cookingPool=pool;}
  var cached=cookingPlans.get(resource);if(cached!=null)return cached.orElse(null);
  // Bound component metadata before retaining it as a cache key or reserving a refund.
  long weight=SeedDataSafety.weight(resource,level.registryAccess());if(weight<1||weight>8192)return null;
  ArrayLedger.Plan plan=null;try{plan=ArrayRecipes.cookingPlan((ServerLevel)level,pool,resource.toStack());}catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Array raw cooking input refused at {}: {}",worldPosition,error.toString());}
  cookingPlans.put(resource,Optional.ofNullable(plan));return plan;
 }
 private ArrayLedger.State reserveCooking(ArrayLedger.Plan plan,int count){var next=ArrayLedger.admit(state,plan,count);return next!=null&&metadataFits(next)?next:null;}
 private final ResourceHandler<ItemResource> rawInput=new ResourceHandler<>(){
  public int size(){return 1;}
  public ItemResource getResource(int slot){return ItemResource.EMPTY;}
  public long getAmountAsLong(int slot){return 0;}
  // Hoppers probe empty-slot capacity before trying a concrete item. Actual admission
  // still checks the recipe and reserves output/refund space transactionally.
  public long getCapacityAsLong(int slot,ItemResource resource){return slot==0&&(resource.isEmpty()||cookingPlan(resource)!=null)?ArrayLedger.MAX_OPERATIONS:0;}
  public boolean isValid(int slot,ItemResource resource){return slot==0&&cookingPlan(resource)!=null;}
  public int extract(int slot,ItemResource resource,int amount,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return 0;}
  public int insert(int slot,ItemResource resource,int amount,TransactionContext tx){
   TransferPreconditions.checkNonEmptyNonNegative(resource,amount);if(slot!=0||amount==0)return 0;var plan=cookingPlan(resource);if(plan==null)return 0;
   int accepted=Math.min(amount,ArrayLedger.MAX_OPERATIONS);var next=reserveCooking(plan,accepted);
   if(next==null){int low=0,high=accepted-1;while(low<high){int mid=low+(high-low+1)/2;if(reserveCooking(plan,mid)!=null)low=mid;else high=mid-1;}accepted=low;if(accepted==0)return 0;next=reserveCooking(plan,accepted);if(next==null)return 0;}
   // Input ownership and its complete output/refund reservation share the caller's transaction.
   journal.updateSnapshots(tx);state=next;return accepted;
  }
 };
 public ResourceHandler<ItemResource> rawInputHandler(){return rawInput;}
 private final ResourceHandler<ItemResource> output=new ResourceHandler<>(){
  private GenericStack entry(int i){return i>=0&&i<state.output().size()?state.output().get(i):null;}
  public int size(){return ArrayLedger.MAX_TYPES;}
  public ItemResource getResource(int i){var s=entry(i);return s!=null&&s.what() instanceof AEItemKey key?key.toResource():ItemResource.EMPTY;}
  public long getAmountAsLong(int i){var s=entry(i);return s!=null&&s.what() instanceof AEItemKey?s.amount():0;}
  public long getCapacityAsLong(int i,ItemResource r){return 0;}
  public boolean isValid(int i,ItemResource r){return false;}
  public int insert(int i,ItemResource r,int n,TransactionContext tx){return 0;}
  public int extract(int i,ItemResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);if(!usable()||working||n==0||!getResource(i).equals(r))return 0;int take=(int)Math.min(n,getAmountAsLong(i));if(take>0){journal.updateSnapshots(tx);state=ArrayLedger.extract(state,AEItemKey.of(r),take);}return take;}
 };
 private final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> fluidOutput=new ResourceHandler<>(){
  private GenericStack entry(int i){return i>=0&&i<state.output().size()?state.output().get(i):null;}
  public int size(){return ArrayLedger.MAX_TYPES;}
  public net.neoforged.neoforge.transfer.fluid.FluidResource getResource(int i){var s=entry(i);return s!=null&&s.what() instanceof AEFluidKey k?k.toResource():net.neoforged.neoforge.transfer.fluid.FluidResource.EMPTY;}
  public long getAmountAsLong(int i){var s=entry(i);return s!=null&&s.what() instanceof AEFluidKey?s.amount():0;}
  public long getCapacityAsLong(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r){return 0;}
  public boolean isValid(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r){return false;}
  public int insert(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r,int n,TransactionContext tx){return 0;}
  public int extract(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);if(!usable()||working||n==0||!getResource(i).equals(r))return 0;int take=(int)Math.min(n,getAmountAsLong(i));if(take>0){journal.updateSnapshots(tx);state=ArrayLedger.extract(state,AEFluidKey.of(r),take);}return take;}
 };
 public ArrayTile(BlockPos pos,BlockState block){super(ProcessingArray.TILE.get(),pos,block);getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(1).addService(ICraftingProvider.class,this);}
 public ItemStacksResourceHandler inventory(){return inventory;}public ItemStacksResourceHandler upgrades(){return upgrades;}
 public int upgradeTier(int slot){return ArrayUpgrades.tier(slot,upgrades.getResource(slot).toStack(upgrades.getAmountAsInt(slot)));}
 public int maximumSpeed(){return 1<<upgradeTier(0);}public int efficiency(){return 15*upgradeTier(1);}
 public int energyCapacity(){return ArrayUpgrades.BASE_CAPACITY*(1<<upgradeTier(2));}
 public long demand(){return ArrayUpgrades.cost(ArrayLedger.demand(state,speed()),upgradeTier(1));}
 public int progress(int job){if(job>=state.jobs().size())return 0;var j=state.jobs().get(job);return (int)(1000L*j.progress()/j.plan().ticks());}
 public int problemX(){return problemX;}public int problemY(){return problemY;}public int problemZ(){return problemZ;}
public EnergyHandler energy(){return energy;}public ResourceHandler<ItemResource> outputHandler(){return output;}public ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> fluidOutputHandler(){return fluidOutput;}public ArrayLedger.State ledger(){return state;}
 public boolean usable(){return schema==1&&recovery==null&&inventoryRecovery==null&&upgradeRecovery==null&&patternRecovery==null&&!builder.locked()&&!state.locked()&&!isRemoved()&&level instanceof ServerLevel s&&s.isLoaded(worldPosition)&&s.getBlockEntity(worldPosition)==this;}
 public boolean empty(){if(!builder.empty())return false;if(schema!=1||recovery!=null||inventoryRecovery!=null||upgradeRecovery!=null||patternRecovery!=null||storedPatterns()>0||!state.jobs().isEmpty()||!state.output().isEmpty())return false;for(int i=0;i<SLOTS;i++)if(inventory.getAmountAsLong(i)>0)return false;for(int i=0;i<3;i++)if(upgrades.getAmountAsLong(i)>0)return false;return true;}
 public boolean installationsEditable(){return usable()&&!working&&state.jobs().isEmpty();}
 public boolean mayEdit(){return usable()&&!working;}
 public int height(){return height;}public int width(){return width;}public int depth(){return depth;}public int speed(){return Math.min(speed,maximumSpeed());}public int racks(){return racks;}public boolean formed(){return formed;}public int layer(){return layer;}public String problem(){return problem;}
 public long revision(){return revision;}public int active(){return state.jobs().stream().mapToInt(ArrayLedger.Job::active).sum();}public int patterns(){return advertised.size();}public int installed(){return capacity().values().stream().mapToInt(Integer::intValue).sum();}
 public long pending(){return state.output().stream().mapToLong(GenericStack::amount).sum();}
 public int hatches(){return hatches;}
 public long invalidPatterns(){return invalidPatterns(0);}
 public long invalidPatterns(int page){long bits=0;var bank=page==0?inventory:extraPatterns.get(page);if(bank==null)return 0;for(int i=0;i<PATTERNS;i++)if(bank.getAmountAsLong(i)>0&&(!plans.containsKey(AEItemKey.of(bank.getResource(i)))||page>=hatches))bits|=1L<<i;return bits;}
 void watch(ArraySlot view){views.add(view);}void unwatch(ArraySlot view){views.remove(view);}
 private void changed(){revision++;setChanged();for(var view:views)view.changed();wake();}
 private void wake(){if(!working&&!scheduled&&level instanceof ServerLevel s&&!isRemoved()){scheduled=true;s.scheduleTick(worldPosition,getBlockState().getBlock(),1);}}
 @Override public void onReady(){super.onReady();com.neolena.essenceindustry.blueprint.BlueprintAutomation.loaded(this);ArrayOwners.update(this);if(level!=null&&!level.isClientSide()){LOADED.add(this);structureDirty=true;dirty=true;wake();}}
 @Override public void onMainNodeStateChanged(IGridNodeListener.State reason){wake();}
 void redstoneChanged(){wake();}
 @Override public void onChunkUnloaded(){com.neolena.essenceindustry.blueprint.BlueprintAutomation.unloaded(this);ArrayOwners.remove(this);LOADED.remove(this);scheduled=false;for(var p:outputPorts)if(level!=null&&level.isLoaded(p)&&level.getBlockEntity(p) instanceof ArrayConnectorTile endpoint)endpoint.disconnect();invalidatePorts();super.onChunkUnloaded();}
 @Override public void setRemoved(){com.neolena.essenceindustry.blueprint.BlueprintAutomation.unloaded(this);ArrayOwners.remove(this);if(visualFormed)ArrayVisualTransition.enqueue(this,false);for(var p:outputPorts)if(level!=null&&level.isLoaded(p)&&level.getBlockEntity(p) instanceof ArrayConnectorTile endpoint)endpoint.disconnect();LOADED.remove(this);super.setRemoved();}
 public static void clearLoaded(){LOADED.clear();SCANS.clear();ArrayVisualTransition.clear();ArrayOwners.clear();ArrayHullShapes.clear();}
 public static void invalidateRecipes(){for(var t:List.copyOf(LOADED)){t.dirty=true;t.wake();}}
 public static void invalidateStructure(Level level,BlockPos pos){if(level.isClientSide())return;for(var t:ArrayOwners.at(level,pos))t.queueStructureCheck();}
 static void chunkChanged(Level level,net.minecraft.world.level.ChunkPos chunk){for(var t:ArrayOwners.inChunk(level,chunk))if(!t.isRemoved())t.queueStructureCheck();}
 private void queueStructureCheck(){visualReconcile=true;structureScan=null;requestStructureCheck();wake();com.neolena.essenceindustry.nuclear.StructureValidationQueue.request(this,()->{if(structureDirty&&usable()&&!working)checkStructure();});}
 public static ArrayTile owner(Level level,BlockPos pos){return ArrayOwners.owner(level,pos);}

 boolean contains(BlockPos p){var local=ArrayLayout.local(layoutAnchor(),ArrayLayout.facing(getBlockState()),p);return local.getX()>=0&&local.getX()<width&&local.getZ()>=0&&local.getZ()<depth&&local.getY()>=0&&local.getY()<height;}

 public Map<String,Integer> capacity(){boolean transactional=Transaction.getCurrentOpenedTransaction()!=null;if(!transactional&&!capacityDirty)return cachedCapacity;var counts=new LinkedHashMap<String,Integer>();for(int i=MACHINES;i<TOOLS;i++)if(inventory.getAmountAsLong(i)>0){var item=inventory.getResource(i).toStack();if(ArrayRecipes.machine(item))counts.merge(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),inventory.getAmountAsInt(i),Integer::sum);}if(!transactional){cachedCapacity=Map.copyOf(counts);capacityDirty=false;return cachedCapacity;}return counts;}
 public boolean acceptsMachine(ItemStack stack){
  if(!ArrayRecipes.machine(stack))return false;var wanted=ItemResource.of(stack);
  for(int i=MACHINES;i<TOOLS;i++)if(inventory.getAmountAsLong(i)>0&&!inventory.getResource(i).equals(wanted))return false;
  return true;
 }
 private Map<AEKey,Long> tools(){return Map.of();}
 private void recoverLegacyPresses(){
  for(int i=TOOLS;i<SLOTS;i++){
   int amount=inventory.getAmountAsInt(i);if(amount<=0)continue;var resource=inventory.getResource(i);
   var output=ArrayLedger.map(state.output());output.merge(AEItemKey.of(resource),(long)amount,Math::addExact);
   var values=ArrayLedger.list(output);if(!ArrayLedger.fits(state.jobs(),values))continue;
   var next=new ArrayLedger.State(state.next(),state.jobs(),values,state.locked());if(!metadataFits(next))continue;
   // Clearing legacy storage and crediting output share one transaction, including rollback.
   try(var transaction=Transaction.openRoot()){
    journal.updateSnapshots(transaction);if(inventory.extract(i,resource,amount,transaction)!=amount)continue;
    state=next;transaction.commit();
   }
  }
 }
 private static ArrayLedger.State migrateLegacyPressJobs(ArrayLedger.State saved){
  var jobs=saved.jobs().stream().map(job->{var plan=job.plan();if(!plan.pool().equals("ae2:inscriber")||plan.tools().isEmpty())return job;
   return new ArrayLedger.Job(job.id(),new ArrayLedger.Plan(plan.recipe(),plan.pool(),plan.ticks(),plan.power(),plan.inputs(),plan.outputs(),List.of()),job.remaining(),job.active(),job.progress());
  }).toList();
  return jobs.equals(saved.jobs())?saved:new ArrayLedger.State(saved.next(),jobs,saved.output(),saved.locked());
 }

 void orientationChanged(Direction oldFace){builder.invalidate();ArrayVisualTransition.enqueueOldOrientation(this,oldFace);visualFormed=false;formed=false;ArrayOwners.update(this);requestStructureCheck();dirty=true;wake();}
 void requestStructureCheck(){ArrayOwners.update(this);if(!structureDirty)structureScan=null;structureDirty=true;}
 public static void invalidateStructures(){for(var tile:List.copyOf(LOADED)){tile.requestStructureCheck();tile.wake();}}
 /** Explicit immediate validation for diagnostics/tests; scheduled gameplay uses the bounded scanner. */
 public void checkStructure(){discoverController();var scan=new ArrayStructureScan(level,worldPosition,width,depth,height);scan.advance(width*depth*height);finishStructureScan(scan);}
 private void finishStructureScan(ArrayStructureScan scan){
  ArrayOwners.update(this);
  var previousPorts=List.copyOf(outputPorts);boolean was=formed;int oldHatches=hatches;
  if(scan.problem.isEmpty())visualWindows=List.copyOf(scan.windows);
  var mask=scan.rackMask.toLongArray();if(visualRackWidth!=width||visualRackDepth!=depth||!Arrays.equals(mask,visualRackMask)){visualRackMask=mask;visualRacks=ArrayBayLayout.decode(mask,width,depth,height);visualRackWidth=width;visualRackDepth=depth;}outputPorts.clear();outputPorts.addAll(scan.ports);racks=scan.racks;hatches=scan.hatches;problem=scan.problem;
  problemX=scan.x;problemY=scan.y;problemZ=scan.z;
  if(problem.isEmpty()&&racks<1)problem="no_rack";
  if(problem.isEmpty()&&capacity().size()>1)problem="mixed";
  if(problem.isEmpty()&&installed()>racks*8)problem="capacity";
  formed=problem.isEmpty();structureDirty=false;structureScan=null;lastCheck=level.getGameTime();
  if(was!=formed||oldHatches!=hatches)dirty=true;
  if(was!=formed||!previousPorts.equals(outputPorts)){invalidatePorts();for(var pos:previousPorts)if(level.isLoaded(pos))level.invalidateCapabilities(pos);}
  if(focusCheck){if(!formed&&problemY>=0&&problemY<height){layer=problemY;guide=true;}focusCheck=false;}
  if(visualFormed!=formed||visualReconcile){ArrayVisualTransition.enqueue(this,formed);visualFormed=formed;visualReconcile=false;}
  for(var p:previousPorts)if(level.isLoaded(p)&&!outputPorts.contains(p)&&level.getBlockEntity(p) instanceof ArrayConnectorTile endpoint)endpoint.disconnect();
  for(var p:outputPorts)if(level.isLoaded(p)&&level.getBlockEntity(p) instanceof ArrayConnectorTile endpoint)endpoint.connect(formed?this:null);
  syncVisuals();revision++;
 }
 private void scanStructure(){
  if(structureScan==null){discoverController();structureScan=new ArrayStructureScan(level,worldPosition,width,depth,height);}
  var budget=SCANS.computeIfAbsent(level.getServer(),key->new ScanBudget());long tick=level.getServer().getTickCount();
  if(budget.tick!=tick){budget.tick=tick;budget.cells=0;}
  budget.cells+=structureScan.advance(Math.max(0,Math.min(2048,8192-budget.cells)));
  if(structureScan.done())finishStructureScan(structureScan);
 }
 private void invalidatePorts(){if(level!=null&&!level.isClientSide())for(var p:outputPorts)if(level.isLoaded(p))level.invalidateCapabilities(p);}
 private boolean metadataFits(ArrayLedger.State candidate){try{return InventoryRecovery.safe(new Dynamic<>(SeedDataSafety.ops(level.registryAccess()),ArrayLedger.State.CODEC.encodeStart(SeedDataSafety.ops(level.registryAccess()),candidate).getOrThrow()));}catch(RuntimeException e){return false;}}
 private void refresh(){
  if(!(level instanceof ServerLevel s))return;dirty=false;recipeGeneration=s.getServer().getRecipeManager().recipeMap();var map=new LinkedHashMap<AEItemKey,ArrayRecipes.Match>();var patterns=new ArrayList<IPatternDetails>();
  var types=capacity().keySet();var recipes=types.size()>1?List.<ArrayRecipes.Native>of():ArrayRecipes.catalog(s,types);nativeRecipes=recipes;var banks=new TreeMap<Integer,ItemStacksResourceHandler>(extraPatterns);banks.put(0,inventory);for(var entry:banks.entrySet()){int page=entry.getKey();if(page>=hatches)break;var bank=entry.getValue();for(int i=0;i<PATTERNS;i++){var stack=bank.getResource(i).toStack();if(stack.isEmpty())continue;try{var pattern=PatternDetailsHelper.decodePattern(stack,level);if(pattern==null)continue;var matched=ArrayRecipes.match(pattern,recipes,tools());if(matched!=null&&map.putIfAbsent(pattern.getDefinition(),matched)==null)patterns.add(pattern);}catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Array pattern slot {} rejected at {}: {}",page*PATTERNS+i,worldPosition,error.toString());}}}
  plans=Map.copyOf(map);advertised=hatches>0?List.copyOf(patterns):List.of();ICraftingProvider.requestUpdate(getMainNode());
 }
 public void work(){
  scheduled=false;if(!usable()||Transaction.getCurrentOpenedTransaction()!=null||lastWork==level.getGameTime())return;lastWork=level.getGameTime();
  working=true;boolean progressed=false;
  try{
   // The shared controller planner owns new construction jobs.
   // Construction changes many cells; validate once it stops instead of repeatedly
   // scanning an expanding prefix of the unfinished shell after every batch.
   if(builder.busy())builder.pause();
   recoverLegacyPresses();
   if(structureDirty||structureScan!=null||(!state.jobs().isEmpty()&&level.getGameTime()-lastCheck>=20))scanStructure();if(structureDirty){scheduled=true;((ServerLevel)level).scheduleTick(worldPosition,getBlockState().getBlock(),1);return;}if(recipeGeneration!=level.getServer().getRecipeManager().recipeMap())dirty=true;if(dirty)refresh();
   if(formed&&!level.hasNeighborSignal(worldPosition)){
    var dispatched=ArrayLedger.dispatch(state,capacity(),tools());
    var advanced=ArrayLedger.advance(dispatched,speed(),this::pay);progressed=!advanced.equals(dispatched);if(!advanced.equals(state)){state=advanced;changed();}
   }
   if(!state.output().isEmpty()&&getMainNode().isActive()&&level.getGameTime()-lastFlush>=5){lastFlush=level.getGameTime();flush();}
  }catch(RuntimeException error){formed=false;problem="work_error";requestStructureCheck();if(level.getGameTime()-lastErrorLog>=100){lastErrorLog=level.getGameTime();org.slf4j.LoggerFactory.getLogger(getClass()).error("Array at {} paused after work failure; job and output snapshots retained",worldPosition,error);}}
  finally{processingNow=progressed&&!state.jobs().isEmpty();working=false;syncVisuals();}
  if(builder.busy()||!state.jobs().isEmpty()||!state.output().isEmpty()){scheduled=true;((ServerLevel)level).scheduleTick(worldPosition,getBlockState().getBlock(),(builder.busy()||formed&&progressed)?1:20);}
 }
 private boolean pay(long fe){
  fe=ArrayUpgrades.cost(fe,upgradeTier(1));
  if(fe<1)return false;
  if(fe<=Integer.MAX_VALUE&&energy.getAmountAsLong()>=fe){int amount=(int)fe;try(var tx=Transaction.openRoot()){if(energy.extract(amount,tx)!=amount)return false;tx.commit();return true;}}
  if(!getMainNode().isActive())return false;var source=getMainNode().getGrid().getEnergyService();double ae=PowerUnit.FE.convertTo(PowerUnit.AE,fe);
  return source.extractAEPower(ae,Actionable.SIMULATE,PowerMultiplier.CONFIG)>=ae&&source.extractAEPower(ae,Actionable.MODULATE,PowerMultiplier.CONFIG)>=ae;
 }
 private void flush(){
  var grid=getMainNode().getGrid();int count=0;for(var entry:List.copyOf(state.output())){if(++count>8)break;
   try{long accepted=StorageHelper.poweredInsert(grid.getEnergyService(),grid.getStorageService().getInventory(),entry.what(),entry.amount(),new MachineSource(this),Actionable.MODULATE);if(accepted<0||accepted>entry.amount())throw new IllegalStateException("Invalid ME output amount");if(accepted>0){state=ArrayLedger.extract(state,entry.what(),accepted);changed();}}
   catch(RuntimeException e){state=new ArrayLedger.State(state.next(),state.jobs(),state.output(),true);changed();org.slf4j.LoggerFactory.getLogger(getClass()).error("Array output locked after ambiguous third-party ME transfer at {}; contents retained",worldPosition,e);break;}
  }
 }
 @Override public List<IPatternDetails> getAvailablePatterns(){return usable()?advertised:List.of();}
 @Override public boolean isBusy(){return !usable()||working||!formed||structureDirty||dirty||!getMainNode().isActive()||state.jobs().size()>=ArrayLedger.MAX_JOBS;}
 @Override public boolean pushPattern(IPatternDetails pattern,KeyCounter[] counters){
  if(pattern==null||counters==null||hatches==0||isBusy()||Transaction.getCurrentOpenedTransaction()!=null)return false;
  if(recipeGeneration!=level.getServer().getRecipeManager().recipeMap()){dirty=true;wake();return false;}
  var match=plans.get(pattern.getDefinition());return admit(pattern,counters,match);
 }
 private boolean admit(IPatternDetails pattern,KeyCounter[] counters,ArrayRecipes.Match match){
  if(match==null||counters.length!=pattern.getInputs().length)return false;
  try{var supplied=new LinkedHashMap<AEKey,Long>();for(var counter:counters){if(counter==null)return false;for(var e:counter){if(e.getLongValue()<=0)return false;supplied.merge(e.getKey(),e.getLongValue(),Math::addExact);}}
   if(!supplied.equals(match.expected()))return false;var next=ArrayLedger.admit(state,match.plan(),match.operations());if(next==null||!metadataFits(next))return false;
   state=next;changed();return true; // AE2 hands ownership over on true; its counters remain untouched.
  }catch(ArithmeticException e){return false;}
 }
 @Override public boolean pushPattern(IPatternDetails pattern,KeyCounter[] counters,Direction side){
  if(pattern==null||counters==null||hatches==0||isBusy()||Transaction.getCurrentOpenedTransaction()!=null)return false;
  if(recipeGeneration!=level.getServer().getRecipeManager().recipeMap()){dirty=true;wake();return false;}
  try{return admit(pattern,counters,ArrayRecipes.match(pattern,nativeRecipes,tools()));}catch(RuntimeException e){return false;}
 }
 @Override public boolean acceptsPlans(){return true;}
 @Override public PatternContainerGroup getCraftingMachineInfo(){return new PatternContainerGroup(AEItemKey.of(ProcessingArray.CONTROLLER.get().asItem()),Component.translatable("block.essenceindustry.processing_array"),List.of());}
 @Override public void preRemoveSideEffects(BlockPos pos,BlockState block){com.neolena.essenceindustry.blueprint.BlueprintStorage.drop(this);super.preRemoveSideEffects(pos,block);}
 public void open(Player player){if(player instanceof ServerPlayer s)s.openMenu(new SimpleMenuProvider((id,inv,p)->new ArrayMenu(id,inv,this),Component.translatable("block.essenceindustry.processing_array")),buf->buf.writeBlockPos(worldPosition));}
 @Override public InteractionResult disassembleWithWrench(Player player,Level level,net.minecraft.world.phys.BlockHitResult hit,ItemStack wrench){if(!empty()){if(!level.isClientSide())player.sendSystemMessage(Component.translatable("gui.essenceindustry.array.empty_first"));return InteractionResult.FAIL;}return super.disassembleWithWrench(player,level,hit,wrench);}
 /** Atomic planner resize: retain installed machines, patterns and construction stock. */
 public boolean configureBuildSize(int w,int d,int h){
  if(!mayEdit()||!state.jobs().isEmpty()||builder.busy()||w<5||w>ArrayConfig.maxWidth()||d<5||d>ArrayConfig.maxDepth()||h<5||h>ArrayConfig.maxHeight()||h%2==0)return false;
  if(w==width&&d==depth&&h==height)return true;
  builder.invalidate();formed=false;visualRackMask=new long[0];visualRacks=List.of();visualWindows=List.of();if(visualFormed){ArrayVisualTransition.enqueue(this,false);visualFormed=false;}
  width=w;depth=d;height=h;layer=Math.min(layer,h-1);requestStructureCheck();changed();return true;
 }
 public void action(int button,ServerPlayer player){
  if(!mayEdit())return;
  if(button==40){builder.calculate();return;}
  if(button==41){builder.deposit(player);return;}
  if(button==42){builder.start(player);return;}
  if(button==43){builder.pause();return;}
  if(button==44){builder.withdraw(player);return;}
  if(button==0){requestStructureCheck();focusCheck=true;dirty=true;wake();}
  if(button==1){state=ArrayLedger.refund(state);changed();}
  if(button==2){speed=speed()>=maximumSpeed()?1:speed()*2;changed();}
  if((button>=3&&button<=6||button==10||button==11)&&state.jobs().isEmpty()&&!builder.busy()){builder.invalidate();formed=false;visualRackMask=new long[0];visualRacks=List.of();visualWindows=List.of();if(visualFormed){ArrayVisualTransition.enqueue(this,false);visualFormed=false;}if(button==10)height=ArrayConfig.nextSize(height,ArrayConfig.maxHeight(),true);if(button==11)height=ArrayConfig.nextSize(height,ArrayConfig.maxHeight(),false);layer=Math.min(layer,height-1);if(button==3)width=ArrayConfig.nextSize(width,ArrayConfig.maxWidth(),true);if(button==4)depth=ArrayConfig.nextSize(depth,ArrayConfig.maxDepth(),true);if(button==5)width=ArrayConfig.nextSize(width,ArrayConfig.maxWidth(),false);if(button==6)depth=ArrayConfig.nextSize(depth,ArrayConfig.maxDepth(),false);requestStructureCheck();changed();}
  if(button==7){layer=(layer+1)%height;changed();syncVisuals();}
  if(button==8){guide=!guide;changed();syncVisuals();}

 }
 public String diagnostic(){return builder.locked()||schema!=1||recovery!=null||inventoryRecovery!=null||upgradeRecovery!=null||patternRecovery!=null||state.locked()?"locked":structureDirty?"unknown":!formed?problem:level!=null&&level.hasNeighborSignal(worldPosition)?"redstone":state.jobs().isEmpty()?"idle":"running";}
 public String problemLocation(){return problemX+", "+problemY+", "+problemZ;}
 @Override public void saveAdditional(ValueOutput out){super.saveAdditional(out);builder.save(out);out.putBoolean("arrayGuideEnabled",guide);out.putInt("guideLayer",layer);out.putInt("arraySchema",schema);out.putInt("width",width);out.putInt("depth",depth);out.putInt("height",height);out.putInt("speed",speed);out.putInt("fe",energy.getAmountAsInt());if(upgradeRecovery==null)out.store("arrayUpgrades",ItemStack.OPTIONAL_CODEC.listOf(3,3),java.util.stream.IntStream.range(0,3).mapToObj(i->upgrades.getResource(i).toStack(upgrades.getAmountAsInt(i))).toList());else out.store("arrayUpgrades",Codec.PASSTHROUGH,upgradeRecovery);if(patternRecovery==null)out.store("arrayExtraPatterns",StoredPattern.CODEC.listOf(0,16384),savedExtraPatterns());else out.store("arrayExtraPatterns",Codec.PASSTHROUGH,patternRecovery);if(inventoryRecovery==null)inventory.serialize(out);else out.store("stacks",Codec.PASSTHROUGH,inventoryRecovery);if(recovery==null)out.store("arrayLedger",ArrayLedger.State.CODEC,state);else out.store("arrayLedger",Codec.PASSTHROUGH,recovery);}
 @Override public void loadTag(ValueInput in){super.loadTag(in);builder.load(in);patternLookup=in.lookup();safePatterns.clear();residentPatternWeights.clear();guide=in.getBooleanOr("arrayGuideEnabled",false);height=in.getIntOr("height",5);if(height<5||height>33||height%2==0)height=5;layer=Math.clamp(in.getIntOr("guideLayer",0),0,height-1);width=in.getIntOr("width",5);depth=in.getIntOr("depth",5);speed=in.getIntOr("speed",1);if(width<5||width>ArrayConfig.HARD_MAX||depth<5||depth>ArrayConfig.HARD_MAX||!Set.of(1,2,4,8,16,32,64).contains(speed)){width=5;depth=5;speed=1;}
  schema=in.getIntOr("arraySchema",0);upgradeRecovery=in.read("arrayUpgrades",Codec.PASSTHROUGH).orElse(null);
  // Absent upgrades are the pre-upgrade save format. Unknown data is retained and locks recovery.
  if(upgradeRecovery==null){for(int i=0;i<3;i++)upgrades.set(i,ItemResource.EMPTY,0);}
  else if(schema==1)try{var parsed=ItemStack.OPTIONAL_CODEC.listOf(3,3).parse(upgradeRecovery);if(parsed.error().isEmpty()){
   var values=parsed.getOrThrow();boolean valid=true;for(int i=0;i<3;i++)if(!values.get(i).isEmpty()&&(values.get(i).getCount()!=1||!ArrayUpgrades.valid(i,values.get(i))))valid=false;
   if(valid){for(int i=0;i<3;i++)upgrades.set(i,ItemResource.of(values.get(i)),values.get(i).getCount());upgradeRecovery=null;}
  }}catch(RuntimeException ignored){}
  // Preserve saved throttle preference and surplus FE after capacity downgrades.
  speed=in.getIntOr("speed",1);if(!Set.of(1,2,4,8,16,32,64).contains(speed))speed=1;
  energy.set(Math.max(0,in.getIntOr("fe",0)));schema=in.getIntOr("arraySchema",0);inventoryRecovery=in.read("stacks",Codec.PASSTHROUGH).orElse(null);
  if(inventoryRecovery!=null&&schema==1){try{var parsed=ItemStack.OPTIONAL_CODEC.listOf(SLOTS,SLOTS).parse(inventoryRecovery);if(parsed.error().isEmpty()){var values=parsed.getOrThrow();long weight=0;boolean valid=true;for(var stack:values){if(stack.getCount()>stack.getMaxStackSize()){valid=false;break;}if(!stack.isEmpty()){long n=SeedDataSafety.weight(ItemResource.of(stack),in.lookup());if(n<1){valid=false;break;}weight+=n;}}if(valid&&weight<=SeedDataSafety.BUDGET/2){for(int i=0;i<SLOTS;i++)inventory.set(i,ItemResource.of(values.get(i)),values.get(i).getCount());inventoryRecovery=null;}}}catch(RuntimeException ignored){}}
  extraPatterns.clear();patternRecovery=in.read("arrayExtraPatterns",Codec.PASSTHROUGH).orElse(null);
  if(patternRecovery!=null&&schema==1)try{var parsed=StoredPattern.CODEC.listOf(0,16384).parse(patternRecovery);if(parsed.error().isEmpty()){
   var records=parsed.getOrThrow();var seen=new HashSet<Integer>();var pageWeights=new HashMap<Integer,Long>();long weight=patternStorageWeight();boolean valid=true;
   for(var record:records){long n=patternWeight(ItemResource.of(record.stack()));if(!seen.add(record.slot())||(record.stack().getCount()<1||record.stack().getCount()>record.stack().getMaxStackSize())||n<1||n>65536){valid=false;break;}weight+=n;if(pageWeights.merge(record.slot()/PATTERNS,n,Long::sum)>SeedDataSafety.BUDGET/2){valid=false;break;}}
   if(valid&&weight<=SeedDataSafety.BUDGET*8){for(var record:records)patternInventory(record.slot()/PATTERNS).set(record.slot()%PATTERNS,ItemResource.of(record.stack()),record.stack().getCount());patternRecovery=null;}
  }}catch(RuntimeException ignored){}
  recovery=in.read("arrayLedger",Codec.PASSTHROUGH).orElse(null);if(recovery!=null&&schema==1){try{var parsed=ArrayLedger.State.CODEC.parse(recovery);if(parsed.error().isEmpty()){state=migrateLegacyPressJobs(parsed.getOrThrow());recovery=null;}}catch(RuntimeException ignored){}}
  if(schema==1&&(in.read("stacks",Codec.PASSTHROUGH).isEmpty()||in.read("arrayLedger",Codec.PASSTHROUGH).isEmpty()))schema=0;
  dirty=true;requestStructureCheck();
 }
}
