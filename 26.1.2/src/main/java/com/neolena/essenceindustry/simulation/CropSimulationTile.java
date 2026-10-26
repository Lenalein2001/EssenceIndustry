package com.neolena.essenceindustry.simulation;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neolena.essenceindustry.item.*;
import com.neolena.essenceindustry.library.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;
public final class CropSimulationTile extends BlockEntity {
 public static final int PERIOD=40;
 private static final Codec<SimulationStore.Output> OUTPUT=RecordCodecBuilder.create(i->i.group(ItemResource.CODEC.optionalFieldOf("resource",ItemResource.EMPTY).forGetter(SimulationStore.Output::resource),Codec.INT.fieldOf("amount").forGetter(SimulationStore.Output::count)).apply(i,SimulationStore.Output::new));
 private static final Codec<SimulationStore.State> STATE=RecordCodecBuilder.create(i->i.group(ItemResource.CODEC.optionalFieldOf("seed",ItemResource.EMPTY).forGetter(SimulationStore.State::seed),OUTPUT.listOf(0,16).fieldOf("outputs").forGetter(SimulationStore.State::outputs),ItemResource.CODEC.optionalFieldOf("speed",ItemResource.EMPTY).forGetter(SimulationStore.State::speed),Codec.INT.optionalFieldOf("speedCount",0).forGetter(SimulationStore.State::speedCount),ItemResource.CODEC.optionalFieldOf("yield",ItemResource.EMPTY).forGetter(SimulationStore.State::yield),Codec.INT.optionalFieldOf("yieldCount",0).forGetter(SimulationStore.State::yieldCount),ItemResource.CODEC.optionalFieldOf("farmland",ItemResource.EMPTY).forGetter(SimulationStore.State::farmland),ItemResource.CODEC.optionalFieldOf("crux",ItemResource.EMPTY).forGetter(SimulationStore.State::crux)).apply(i,SimulationStore.State::new));
 private static final Codec<SimulationStore.State> LEGACY=RecordCodecBuilder.create(i->i.group(ItemResource.CODEC.optionalFieldOf("seed",ItemResource.EMPTY).forGetter(SimulationStore.State::seed),ItemResource.CODEC.optionalFieldOf("output",ItemResource.EMPTY).forGetter(SimulationStore.State::output),Codec.INT.fieldOf("count").forGetter(SimulationStore.State::count)).apply(i,SimulationStore.State::new));
 private Dynamic<?> recovery,nodeRecovery;
 private boolean scheduled,working,unloaded,meEnabled;
 private long lastProduction=Long.MIN_VALUE,lastWork=Long.MIN_VALUE;
 private static long catalogEpoch;private long observedEpoch=-1;
 public static void invalidate(){catalogEpoch++;}
 private ItemResource cachedSeed=ItemResource.EMPTY;private PlantProfile profile;private boolean hadSeedCache;private long progress;private int batch,productionStatus;
 private final List<BlockCapabilityCache<ResourceHandler<ItemResource>,Direction>> neighbors=new ArrayList<>();
 private final Set<SimulationSlot> views=Collections.newSetFromMap(new WeakHashMap<>());
 private final SimulationStore inventory=new SimulationStore(()->{setChanged();for(var view:views)view.changed();if(!working)wake();},()->level==null?SeedDataSafety.builtinLookup():level.registryAccess());
 private final ResourceHandler<ItemResource> io=new ResourceHandler<>(){
  public int size(){return inventory.size();}public ItemResource getResource(int i){return inventory.getResource(i);}public long getAmountAsLong(int i){return inventory.getAmountAsLong(i);}public long getCapacityAsLong(int i,ItemResource r){return inventory.getCapacityAsLong(i,r);}public boolean isValid(int i,ItemResource r){return inventory.isValid(i,r);}
  public int insert(int i,ItemResource r,int n,TransactionContext tx){return inventory.insert(i,r,n,tx);}public int extract(int i,ItemResource r,int n,TransactionContext tx){return i>0 && i<=SimulationStore.OUTPUTS?inventory.extract(i,r,n,tx):0;}
 };
 private final ResourceHandler<ItemResource> guarded=new GuardedItemStorage(io,this::available);
 private final SimulationNetwork network=CropSimulation.networkFactory==null?null:CropSimulation.networkFactory.apply(this);
 public CropSimulationTile(BlockPos p,BlockState s){super(CropSimulation.TILE.get(),p,s);}
 public SimulationStore inventory(){return inventory;}
 public ResourceHandler<ItemResource> automation(){return guarded;}
 public SimulationNetwork network(){return network;}
 public boolean meEnabled(){return meEnabled;}
 public int meStatus(){return network==null?3:network.status();}
 public boolean quarantined(){return recovery!=null;}
 public boolean safeToMove(){
  refresh();if(working)return false;long weight=inventory.dataWeight();if(recovery!=null)weight+=SeedDataSafety.tagWeight(recovery.convert(NbtOps.INSTANCE).getValue());return weight<=SeedDataSafety.BUDGET;
 }
 public boolean available(){return !working && !unloaded && !quarantined() && !isRemoved() && level instanceof ServerLevel s && s.isLoaded(worldPosition) && s.getBlockEntity(worldPosition)==this;}
 public void toggleMe(){if(available() && network!=null){meEnabled=!meEnabled;network.settingsChanged();level.invalidateCapabilities(worldPosition);setChanged();wake();}}
 private static final class Crops {
  static final Map<net.minecraft.world.item.Item,Crop> BY_SEED=new IdentityHashMap<>();
  static {for(var c:CropRegistry.getInstance().getCrops())if(c.getSeedsItem()!=null)BY_SEED.put(c.getSeedsItem(),c);}
 }
 public static Crop crop(ItemResource seed){if(seed.isEmpty())return null;var c=Crops.BY_SEED.get(seed.getItem());return c!=null && c.isEnabled() && c.getEssenceItem()!=null && c.getEssenceItem()!=Items.AIR?c:null;}
 public static boolean safeSeed(ItemResource r,net.minecraft.core.HolderLookup.Provider lookup){if(r.isEmpty())return true;long weight=SeedDataSafety.weight(r,lookup);return weight>0 && weight<=SeedDataSafety.BUDGET/4;}
 private void refresh(){var seed=inventory.state().seed();if(seed.equals(cachedSeed) && observedEpoch==catalogEpoch)return;
  if(observedEpoch!=catalogEpoch)inventory.invalidateWeights();if(hadSeedCache && !seed.equals(cachedSeed))progress=0;hadSeedCache=true;observedEpoch=catalogEpoch;cachedSeed=seed;profile=PlantProfile.resolve(seed);batch=0;productionStatus=profile==null?0:1;
 }
 public int batchSize(){return batch;}
 public BlockState farmland(){var r=inventory.state().farmland();var state=r.isEmpty()?level==null?net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState():level.getBlockState(worldPosition.below()):net.minecraft.world.level.block.Block.byItem(r.getItem()).defaultBlockState();return state.hasProperty(net.minecraft.world.level.block.FarmlandBlock.MOISTURE)?state.setValue(net.minecraft.world.level.block.FarmlandBlock.MOISTURE,7):state;}
 public BlockState crux(){var r=inventory.state().crux();return r.isEmpty()?level==null?net.minecraft.world.level.block.Blocks.AIR.defaultBlockState():level.getBlockState(worldPosition.below(2)):net.minecraft.world.level.block.Block.byItem(r.getItem()).defaultBlockState();}
 public int speedMultiplier(){return Math.multiplyExact(SimulationConfig.speed(inventory.state().speedCount()),!inventory.state().farmland().isEmpty() && farmland().getBlock() instanceof com.neolena.essenceindustry.farmland.VitalizedFarmlandBlock?2:1);}
 public int yieldMultiplier(){return SimulationConfig.yield(inventory.state().yieldCount());}
 public int status(){refresh();return quarantined()?3:profile==null?0:productionStatus;}
 void watch(SimulationSlot s){views.add(s);} void unwatch(SimulationSlot s){views.remove(s);}
 private void wake(){if(!scheduled && !isRemoved() && level instanceof ServerLevel s){scheduled=true;s.scheduleTick(worldPosition,getBlockState().getBlock(),PERIOD);}}
 @Override public void onLoad(){unloaded=false;lastProduction=level.getGameTime();super.onLoad();if(network!=null)network.start();wake();}
 @Override public void onChunkUnloaded(){unloaded=true;scheduled=false;neighbors.clear();if(network!=null)network.stop();super.onChunkUnloaded();}
 @Override public void setRemoved(){if(network!=null)network.stop();super.setRemoved();}
 public boolean generate(){
  if(!available() || Transaction.getCurrentOpenedTransaction()!=null)return false;
  long now=level.getGameTime();if(lastProduction==Long.MIN_VALUE){lastProduction=now;return false;}if(now-lastProduction<PERIOD)return false;lastProduction=now;
  refresh();if(profile==null)return false;if(inventory.state().count()>=SimulationStore.LIMIT){productionStatus=2;return false;}if(!profile.environment((ServerLevel)level,worldPosition,farmland(),crux())){productionStatus=4;return false;}
  var traits=SeedTraits.read(inventory.state().seed().toStack());int cycle=SimulationConfig.cycle(profile);
  long credit=Math.addExact(progress,Math.multiplyExact((long)PERIOD*speedMultiplier(),Math.max(1L,1L+traits.growth())));long cycles=credit/cycle;
  if(cycles==0){progress=credit;productionStatus=1;setChanged();return false;}
  // Analyze no more than eight mature harvests, irrespective of speed or output count.
  var plan=profile.analyze((ServerLevel)level,worldPosition,(int)Math.min(cycles,8),farmland(),crux());Map<ItemResource,Integer> drops=null;
  int room=SimulationStore.LIMIT-inventory.state().count();
  for(int attempt=0;attempt<63 && cycles>0;attempt++){
   try{var candidate=plan.produce(cycles,yieldMultiplier());if(candidate.values().stream().mapToLong(Integer::longValue).sum()<=room){drops=candidate;break;}}
   catch(PlantProfile.BatchTooLarge | ArithmeticException ignored){}cycles/=2;
  }
  if(drops==null){productionStatus=5;return false;}if(drops.isEmpty()){progress=credit-cycles*cycle;productionStatus=6;setChanged();return false;}
  working=true;try(var tx=Transaction.openRoot()){
   if(!inventory.generate(drops,tx)){productionStatus=2;return false;}tx.commit();progress=credit-cycles*cycle;batch=drops.values().stream().mapToInt(Integer::intValue).sum();productionStatus=1;setChanged();return true;
  }finally{working=false;}
 }
 public int pushTo(ResourceHandler<ItemResource> destination){
  if(!available() || destination==guarded || Transaction.getCurrentOpenedTransaction()!=null)return 0;
  working=true;try(var tx=Transaction.openRoot()){
   int total=0;for(int slot=1;slot<=SimulationStore.OUTPUTS;slot++){var resource=inventory.getResource(slot);int amount=inventory.getAmountAsInt(slot);if(amount==0)continue;
    int moved=destination.insert(resource,amount,tx);if(moved<0 || moved>amount)throw new IllegalStateException("Invalid simulation destination result");
    if(moved>0 && inventory.extract(slot,resource,moved,tx)!=moved)throw new IllegalStateException("Source changed during output");total+=moved;
   }
   tx.commit();return total;
  }finally{working=false;}
 }
 public int export(java.util.function.ToLongBiFunction<ItemResource,Integer> insert){
  if(!available() || Transaction.getCurrentOpenedTransaction()!=null)return 0;working=true;int total=0;
  // At most sixteen bulk offers. Reserve each exact output before entering the nontransactional ME API.
  try{
   for(int slot=1;slot<=SimulationStore.OUTPUTS;slot++){
    var original=inventory.state();var entry=original.outputs().get(slot-1);int offered=entry.count();if(offered==0)continue;
    recovery=STATE.encodeStart(SeedDataSafety.ops(level.registryAccess()),original).map(v->new Dynamic<>(SeedDataSafety.ops(level.registryAccess()),v)).getOrThrow();
    var reserved=new ArrayList<>(original.outputs());reserved.set(slot-1,SimulationStore.Output.EMPTY);inventory.load(original.withOutputs(reserved));setChanged();
    long accepted=insert.applyAsLong(entry.resource(),offered);if(accepted<0 || accepted>offered)throw new IllegalStateException("Invalid ME insertion result");
    reserved.set(slot-1,accepted==offered?SimulationStore.Output.EMPTY:new SimulationStore.Output(entry.resource(),offered-(int)accepted));inventory.load(original.withOutputs(reserved));recovery=null;setChanged();total+=(int)accepted;
   }
   return total;
  }catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger(getClass()).error("Crop simulator at {} locked after uncertain ME transfer; reserved inventory retained",worldPosition,error);return total;}
  finally{working=false;}
 }
 public void work(){
  scheduled=false;if(!available())return;if(Transaction.getCurrentOpenedTransaction()!=null){wake();return;}
  long now=level.getGameTime();if(lastWork==now){wake();return;}lastWork=now;
  try{
   generate();if(meEnabled && network!=null){if(network.status()==2)export(network::insert);}
   else{
    if(neighbors.isEmpty())for(var dir:Direction.values())neighbors.add(BlockCapabilityCache.create(Capabilities.Item.BLOCK,(ServerLevel)level,worldPosition.relative(dir),dir.getOpposite()));
    if(inventory.state().count()>0)for(var cache:neighbors){var target=cache.getCapability();if(target!=null){pushTo(target);if(inventory.state().count()==0)break;}}
   }
  }catch(RuntimeException e){lock();org.slf4j.LoggerFactory.getLogger(getClass()).error("Crop simulator locked after handler failure",e);}
  if(!quarantined() && (!inventory.state().seed().isEmpty() || inventory.state().count()>0))wake();
 }
 private void lock(){recovery=encode();setChanged();}
 private Dynamic<?> encode(){var ops=SeedDataSafety.ops(level==null?SeedDataSafety.builtinLookup():level.registryAccess());var tag=(CompoundTag)STATE.encodeStart(ops,inventory.state()).getOrThrow();tag.putInt("schema",2);return new Dynamic<>(ops,tag);}
 private void remember(Dynamic<?> raw){
  if(recovery==null){recovery=new Dynamic<>(SeedDataSafety.ops(level==null?SeedDataSafety.builtinLookup():level.registryAccess()),raw.convert(NbtOps.INSTANCE).getValue().copy());return;}if(recovery.equals(raw))return;
  var tag=new CompoundTag();tag.put("previous",recovery.convert(NbtOps.INSTANCE).getValue());tag.put("additional",raw.convert(NbtOps.INSTANCE).getValue().copy());recovery=new Dynamic<>(SeedDataSafety.ops(level==null?SeedDataSafety.builtinLookup():level.registryAccess()),tag);
 }
 private void decode(Dynamic<?> raw){
  if(raw.get("schema").result().isPresent() && (raw.get("schema").asInt(-1)<1 || raw.get("schema").asInt(-1)>2) || raw.get("count").result().isPresent() && (raw.get("outputs").result().isPresent() || raw.get("farmland").result().isPresent() || raw.get("crux").result().isPresent())){remember(raw);return;}
  try{inventory.load((raw.get("count").result().isPresent()?LEGACY:STATE).parse(raw).getOrThrow());recovery=null;}catch(RuntimeException e){remember(raw);}
 }
 @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("simulation",Codec.PASSTHROUGH,encode());if(recovery!=null)out.store("simulationRecovery",Codec.PASSTHROUGH,recovery);out.putBoolean("meEnabled",meEnabled);out.putLong("growthProgress",progress);if(network!=null)network.save(out);else if(nodeRecovery!=null)out.store("meNode",Codec.PASSTHROUGH,nodeRecovery);}
 @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);in.read("simulation",Codec.PASSTHROUGH).ifPresent(this::decode);in.read("simulationRecovery",Codec.PASSTHROUGH).ifPresent(this::remember);meEnabled=in.getBooleanOr("meEnabled",false);progress=in.getLongOr("growthProgress",0);if(progress<0){var tag=new CompoundTag();tag.putLong("invalidProgress",progress);remember(new Dynamic<>(SeedDataSafety.ops(level==null?SeedDataSafety.builtinLookup():level.registryAccess()),tag));progress=0;}if(network!=null)network.load(in);else nodeRecovery=in.read("meNode",Codec.PASSTHROUGH).orElse(null);observedEpoch=-1;hadSeedCache=false;}
 @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder data){super.collectImplicitComponents(data);
  // One portable record contains the exact seed, bulk output, settings and opaque transfer recovery.
  var ops=SeedDataSafety.ops(level==null?SeedDataSafety.builtinLookup():level.registryAccess());var tag=new CompoundTag();tag.put("inventory",encode().convert(NbtOps.INSTANCE).getValue());tag.putBoolean("meEnabled",meEnabled);tag.putLong("growthProgress",progress);if(recovery!=null)tag.put("recovery",recovery.convert(NbtOps.INSTANCE).getValue());InventoryRecovery.collect(data,"cropSimulator",new Dynamic<>(ops,tag));
 }
 @Override protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter data){super.applyImplicitComponents(data);var raw=InventoryRecovery.read(data,"cropSimulator",level==null?SeedDataSafety.builtinLookup():level.registryAccess());if(raw!=null){var inv=raw.get("inventory").result();if(inv.isPresent()){decode(inv.get());meEnabled=raw.get("meEnabled").asBoolean(false);progress=raw.get("growthProgress").asLong(0);if(progress<0){remember(raw);progress=0;}hadSeedCache=false;raw.get("recovery").result().ifPresent(this::remember);}else remember(raw);}}
}
