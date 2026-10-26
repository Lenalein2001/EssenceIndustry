package com.neolena.essenceindustry.simulation;
import java.util.*;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;
/** Stable transactional slots; all output keys and upgrade components are retained exactly. */
public final class SimulationStore extends SnapshotJournal<SimulationStore.State> implements ResourceHandler<ItemResource> {
 public static final int LIMIT=1_000_000, OUTPUTS=16, SPEED=17, YIELD=18, FARMLAND=19, CRUX=20;
 public record Output(ItemResource resource,int count){public static final Output EMPTY=new Output(ItemResource.EMPTY,0);}
 public record State(ItemResource seed,List<Output> outputs,ItemResource speed,int speedCount,ItemResource yield,int yieldCount,ItemResource farmland,ItemResource crux){
  public State{var values=new ArrayList<>(outputs);if(values.size()>OUTPUTS)throw new IllegalArgumentException("Too many output types");while(values.size()<OUTPUTS)values.add(Output.EMPTY);outputs=List.copyOf(values);}
  public State(ItemResource seed,List<Output> outputs,ItemResource speed,int speedCount,ItemResource yield,int yieldCount){this(seed,outputs,speed,speedCount,yield,yieldCount,ItemResource.EMPTY,ItemResource.EMPTY);}
  public State(ItemResource seed,ItemResource output,int count){this(seed,count==0?List.of():List.of(new Output(output,count)),ItemResource.EMPTY,0,ItemResource.EMPTY,0);}
  public State withCatalyst(int slot,ItemResource r){return new State(seed,outputs,speed,speedCount,yield,yieldCount,slot==FARMLAND?r:farmland,slot==CRUX?r:crux);}
  public int count(){long total=0;for(var r:outputs)total+=r.count();return Math.toIntExact(total);}
  public ItemResource output(){for(var r:outputs)if(r.count()>0)return r.resource();return ItemResource.EMPTY;}
  public State withSeed(ItemResource r){return new State(r,outputs,speed,speedCount,yield,yieldCount,farmland,crux);}
  public State withOutputs(List<Output> r){return new State(seed,r,speed,speedCount,yield,yieldCount,farmland,crux);}
 }
 private State state=new State(ItemResource.EMPTY,ItemResource.EMPTY,0);
 private final java.util.Map<ItemResource,Long> weights=new java.util.LinkedHashMap<>(64,0.75F,true){@Override protected boolean removeEldestEntry(java.util.Map.Entry<ItemResource,Long> e){return size()>64;}};
 private State weightedState;private long cachedWeight;
 public void invalidateWeights(){weights.clear();weightedState=null;}
 private long weight(ItemResource r){if(r.isEmpty())return 0;return weights.computeIfAbsent(r,key->{long value=com.neolena.essenceindustry.library.SeedDataSafety.weight(key,lookup.get());return value<=0?com.neolena.essenceindustry.library.SeedDataSafety.BUDGET+1:value;});}
 private long dataWeight(State value){long total=weight(value.seed())+weight(value.speed())+weight(value.yield())+weight(value.farmland())+weight(value.crux());for(var out:value.outputs())if(out.count()>0)total+=weight(out.resource());return total;}
 public long dataWeight(){if(weightedState!=state){cachedWeight=dataWeight(state);weightedState=state;}return cachedWeight;}
 private final Runnable changed;private final java.util.function.Supplier<net.minecraft.core.HolderLookup.Provider> lookup;
 public SimulationStore(Runnable changed){this(changed,com.neolena.essenceindustry.library.SeedDataSafety::builtinLookup);}
 public SimulationStore(Runnable changed,java.util.function.Supplier<net.minecraft.core.HolderLookup.Provider> lookup){this.changed=changed;this.lookup=lookup;}
 public State state(){return state;}
 public void load(State next){
  if(next.count()<0 || next.count()>LIMIT || next.speedCount()<0 || next.speedCount()>8 || next.yieldCount()<0 || next.yieldCount()>8)throw new IllegalArgumentException("Invalid simulation quantities");
  for(var r:next.outputs())if(r.count()<0 || r.count()>0 && r.resource().isEmpty())throw new IllegalArgumentException("Invalid output");
  if(next.speedCount()>0 && !next.speed().is(CropSimulation.SPEED.get()) || next.yieldCount()>0 && !next.yield().is(CropSimulation.YIELD.get()))throw new IllegalArgumentException("Invalid upgrades");
  if(!next.farmland().isEmpty() && !isFarmland(next.farmland()) || !next.crux().isEmpty() && !isCrux(next.crux()))throw new IllegalArgumentException("Invalid farmland/crux");
  if(!state.equals(next)){state=next;changed.run();}
 }
 @Override protected State createSnapshot(){return state;}@Override protected void revertToSnapshot(State s){state=s;}@Override protected void onRootCommit(State before){if(!state.equals(before))changed.run();}
 // Container menus assign whole slots after mayPlace/max-stack checks, like vanilla inventories.
 void setMenuSlot(int slot,ItemResource r,int n){
  if(n==0)r=ItemResource.EMPTY;
  State next=slot==0?state.withSeed(r):slot==SPEED?new State(state.seed(),state.outputs(),r,n,state.yield(),state.yieldCount(),state.farmland(),state.crux()):slot==YIELD?new State(state.seed(),state.outputs(),state.speed(),state.speedCount(),r,n,state.farmland(),state.crux()):slot==FARMLAND || slot==CRUX?state.withCatalyst(slot,r):null;
  if(next==null)throw new IllegalArgumentException("Output slot is not a menu input");load(next);
 }
 boolean canSetMenuSlot(int slot,ItemResource r){
  if(!isValid(slot,r) || !CropSimulationTile.safeSeed(r,lookup.get()))return false;
  State next=slot==0?state.withSeed(r):slot==SPEED?new State(state.seed(),state.outputs(),r,1,state.yield(),state.yieldCount(),state.farmland(),state.crux()):slot==FARMLAND || slot==CRUX?state.withCatalyst(slot,r):new State(state.seed(),state.outputs(),state.speed(),state.speedCount(),r,1,state.farmland(),state.crux());
  return dataWeight(next)<=com.neolena.essenceindustry.library.SeedDataSafety.BUDGET;
 }
 public static boolean isFarmland(ItemResource r){return !r.isEmpty() && net.minecraft.world.level.block.Block.byItem(r.getItem()) instanceof net.minecraft.world.level.block.FarmlandBlock;}
 private static final class Cruxes {static final Set<net.minecraft.world.item.Item> ITEMS=Collections.newSetFromMap(new IdentityHashMap<>());static{for(var c:com.blakebr0.mysticalagriculture.registry.CropRegistry.getInstance().getCrops())if(c.getCruxBlock()!=null && c.getCruxBlock().asItem()!=net.minecraft.world.item.Items.AIR)ITEMS.add(c.getCruxBlock().asItem());}}
 public static boolean isCrux(ItemResource r){return !r.isEmpty() && Cruxes.ITEMS.contains(r.getItem());}
 public int size(){return 21;}
 public ItemResource getResource(int i){return i==0?state.seed():i>0 && i<=OUTPUTS?state.outputs().get(i-1).resource():i==SPEED?state.speed():i==YIELD?state.yield():i==FARMLAND?state.farmland():i==CRUX?state.crux():ItemResource.EMPTY;}
 public long getAmountAsLong(int i){return i==0?(state.seed().isEmpty()?0:1):i>0 && i<=OUTPUTS?state.outputs().get(i-1).count():i==SPEED?state.speedCount():i==YIELD?state.yieldCount():i==FARMLAND || i==CRUX?(getResource(i).isEmpty()?0:1):0;}
 public long getCapacityAsLong(int i,ItemResource r){return i==0 || i==FARMLAND || i==CRUX?1:i==SPEED || i==YIELD?8:i>0 && i<=OUTPUTS?LIMIT:0;}
 public boolean isValid(int i,ItemResource r){return !r.isEmpty() && (i==0?PlantProfile.accepts(r):i==SPEED?r.is(CropSimulation.SPEED.get()):i==YIELD?r.is(CropSimulation.YIELD.get()):i==FARMLAND?isFarmland(r):i==CRUX && isCrux(r));}
 public int insert(int i,ItemResource r,int n,TransactionContext tx){
  TransferPreconditions.checkNonEmptyNonNegative(r,n);if(n==0 || !isValid(i,r) || !CropSimulationTile.safeSeed(r,lookup.get()))return 0;
  var old=getResource(i);if(!old.isEmpty() && !old.equals(r))return 0;int accepted=(int)Math.min(n,getCapacityAsLong(i,r)-getAmountAsLong(i));if(accepted<=0)return 0;
  var next=i==0?state.withSeed(r):i==FARMLAND || i==CRUX?state.withCatalyst(i,r):new State(state.seed(),state.outputs(),i==SPEED?r:state.speed(),i==SPEED?state.speedCount()+accepted:state.speedCount(),i==YIELD?r:state.yield(),i==YIELD?state.yieldCount()+accepted:state.yieldCount(),state.farmland(),state.crux());if(dataWeight(next)>com.neolena.essenceindustry.library.SeedDataSafety.BUDGET)return 0;updateSnapshots(tx);state=next;return accepted;
 }
 public int extract(int i,ItemResource r,int n,TransactionContext tx){
  TransferPreconditions.checkNonEmptyNonNegative(r,n);if(n==0 || !getResource(i).equals(r))return 0;int taken=(int)Math.min(n,getAmountAsLong(i));if(taken<=0)return 0;updateSnapshots(tx);
  if(i==0)state=state.withSeed(ItemResource.EMPTY);
  else if(i<=OUTPUTS){var values=new ArrayList<>(state.outputs());var old=values.get(i-1);values.set(i-1,old.count()==taken?Output.EMPTY:new Output(r,old.count()-taken));state=state.withOutputs(values);}
  else if(i==FARMLAND || i==CRUX)state=state.withCatalyst(i,ItemResource.EMPTY);
  else state=new State(state.seed(),state.outputs(),i==SPEED && state.speedCount()==taken?ItemResource.EMPTY:state.speed(),i==SPEED?state.speedCount()-taken:state.speedCount(),i==YIELD && state.yieldCount()==taken?ItemResource.EMPTY:state.yield(),i==YIELD?state.yieldCount()-taken:state.yieldCount(),state.farmland(),state.crux());return taken;
 }
 public boolean generate(ItemResource r,int n,TransactionContext tx){return generate(Map.of(r,n),tx);}
 public boolean generate(Map<ItemResource,Integer> drops,TransactionContext tx){
  long total=state.count();var values=new ArrayList<>(state.outputs());
  for(var entry:drops.entrySet()){
   var r=entry.getKey();int n=entry.getValue();if(r.isEmpty() || n<=0 || (total+=n)>LIMIT)return false;
   int target=-1;for(int i=0;i<OUTPUTS;i++)if(values.get(i).count()>0 && values.get(i).resource().equals(r)){target=i;break;}
   if(target<0)for(int i=0;i<OUTPUTS;i++)if(values.get(i).count()==0){target=i;break;}if(target<0)return false;
   var old=values.get(target);values.set(target,new Output(r,Math.addExact(old.count(),n)));
  }
  var next=state.withOutputs(values);if(dataWeight(next)>com.neolena.essenceindustry.library.SeedDataSafety.BUDGET)return false;updateSnapshots(tx);state=next;return true;
 }
}
