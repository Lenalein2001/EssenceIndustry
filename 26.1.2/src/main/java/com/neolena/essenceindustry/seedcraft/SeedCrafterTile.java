package com.neolena.essenceindustry.seedcraft;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.mojang.serialization.*;
import com.neolena.essenceindustry.item.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;

/** Scheduled work only; recipe lookup is cached and all inventory mutations share a transaction. */
public class SeedCrafterTile extends BlockEntity {
 public static final int INPUTS=9,SLOTS=19;
 private static final Map<RecipeManager,List<IInfusionRecipe>> CATALOG=new WeakHashMap<>();
 private static final Set<SeedCrafterTile> LOADED=Collections.newSetFromMap(new WeakHashMap<>());
 private static long catalogEpoch;
 public static void clearCatalog(){CATALOG.clear();catalogEpoch++;for(var tile:List.copyOf(LOADED))tile.wake(5);}
 private Dynamic<?> recovery;
 private long lastAttempt=-1,lastEpoch=-1;
 private boolean scheduled,working,unloaded;private long revision;private int blocked;
 private final Set<CrafterSlot> openSlots=Collections.newSetFromMap(new WeakHashMap<>());
 private final List<BlockCapabilityCache<ResourceHandler<ItemResource>,Direction>> neighbors=new ArrayList<>();
 private final ItemStacksResourceHandler inventory=new ItemStacksResourceHandler(SLOTS){
  @Override public boolean isValid(int slot,ItemResource resource){return super.isValid(slot,resource)&&(slot>=INPUTS||acceptsInput(resource));}
  @Override protected void onContentsChanged(int slot,ItemStack previous){revision++;setChanged();for(var view:openSlots)view.changed();if(!working)wake(5);}
 };
 private final ResourceHandler<ItemResource> io=new ResourceHandler<>(){
  public int size(){return SLOTS;}
  public ItemResource getResource(int i){return inventory.getResource(i);}
  public long getAmountAsLong(int i){return inventory.getAmountAsLong(i);}
  public long getCapacityAsLong(int i,ItemResource r){return inventory.getCapacityAsLong(i,r);}
  public boolean isValid(int i,ItemResource r){return i<INPUTS && inventory.isValid(i,r);}
  public int insert(int i,ItemResource r,int amount,TransactionContext tx){return i<INPUTS?inventory.insert(i,r,amount,tx):0;}
  public int extract(int i,ItemResource r,int amount,TransactionContext tx){return i>=INPUTS?inventory.extract(i,r,amount,tx):0;}
 };
 private final ResourceHandler<ItemResource> guarded=new GuardedItemStorage(io,this::available);
 public SeedCrafterTile(BlockPos pos,BlockState state){this(SeedCrafter.TILE.get(),pos,state);}
 public SeedCrafterTile(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos pos,BlockState state){super(type,pos,state);}
 protected boolean copiesModels(){return false;}
 protected boolean acceptsInput(ItemResource resource){return true;}
 public ItemStacksResourceHandler inventory(){return inventory;}
 public ResourceHandler<ItemResource> automation(){return guarded;}
 public boolean quarantined(){return recovery!=null;}
 public boolean safeToMove(){return !working && InventoryRecovery.safe(recovery);}
 public boolean available(){return !working && !unloaded && !quarantined() && !isRemoved() && level instanceof ServerLevel server && server.isLoaded(worldPosition) && server.getBlockEntity(worldPosition)==this;}
 public long revision(){return revision;}
 public int status(){return quarantined()?3:blocked;}
 void watch(CrafterSlot slot){openSlots.add(slot);}
 void unwatch(CrafterSlot slot){openSlots.remove(slot);}
 protected final void wake(int delay){if(!scheduled && !isRemoved() && level instanceof ServerLevel server){scheduled=true;server.scheduleTick(worldPosition,getBlockState().getBlock(),delay);}}
 @Override public void onLoad(){unloaded=false;super.onLoad();LOADED.add(this);wake(5);}
 @Override public void onChunkUnloaded(){unloaded=true;scheduled=false;neighbors.clear();LOADED.remove(this);super.onChunkUnloaded();}
 @Override public void setRemoved(){LOADED.remove(this);super.setRemoved();}
 private boolean hasOutput(){for(int i=INPUTS;i<SLOTS;i++)if(inventory.getAmountAsLong(i)>0)return true;return false;}
 private boolean insertOutput(ItemStack stack,TransactionContext tx){
  if(stack.isEmpty())return true;var resource=ItemResource.of(stack);int remaining=stack.getCount();
  for(int i=INPUTS;i<SLOTS && remaining>0;i++)remaining-=inventory.insert(i,resource,remaining,tx);
  return remaining==0;
 }
 /** Exposed for verification; caller must be on the server thread. */
 public boolean craftOne(){
  if(!available() || Transaction.getCurrentOpenedTransaction()!=null)return false;
  if(lastAttempt==revision && lastEpoch==catalogEpoch)return false;lastAttempt=revision;lastEpoch=catalogEpoch;
  var stacks=new ArrayList<ItemStack>();int count=0;for(int i=0;i<INPUTS;i++){var stack=inventory.getResource(i).toStack(inventory.getAmountAsInt(i));stacks.add(stack);count+=Math.min(9,stack.getCount());}
  if(count<2){blocked=0;return false;}
  if(copiesModels()){
   working=true;try{var copied=com.neolena.essenceindustry.compat.hnn.ModelCopy.craft(inventory);blocked=copied==null?1:copied?0:2;return copied!=null && copied;}finally{working=false;}
  }
  var recipes=CATALOG.computeIfAbsent(level.getServer().getRecipeManager(),manager->manager.getRecipes().stream().filter(r->r.value() instanceof IInfusionRecipe).sorted(Comparator.comparing(r->r.id().identifier().toString())).map(r->(IInfusionRecipe)r.value()).toList());
  for(var recipe:recipes){
   var batch=InfusionBatch.match(recipe,stacks,level);if(batch==null)continue;
   // Third-party recipe methods receive copies, never references to stored inventory.
   var result=recipe.assemble(batch.input());var remains=recipe.getRemainingItems(batch.input());
   if(result.isEmpty() || result.getCount()<=0 || remains.size()>9){blocked=1;return false;}
   working=true;
   try(var tx=Transaction.openRoot()){
    for(int i=0;i<INPUTS;i++)if(batch.consumed()[i]>0 && inventory.extract(i,inventory.getResource(i),batch.consumed()[i],tx)!=batch.consumed()[i])return false;
    if(!insertOutput(result,tx)){blocked=2;return false;}
    for(var remainder:remains)if(!insertOutput(remainder,tx)){blocked=2;return false;}
    tx.commit();blocked=0;return true;
   }finally{working=false;}
  }
  blocked=1;return false;
 }
 /** Transactional destinations cannot commit an insertion without the matching source removal. */
 public int pushTo(ResourceHandler<ItemResource> destination){
  if(!available() || destination==guarded || Transaction.getCurrentOpenedTransaction()!=null)return 0;
  working=true;
  try{
   for(int i=INPUTS;i<SLOTS;i++){
    int amount=inventory.getAmountAsInt(i);if(amount<=0)continue;var resource=inventory.getResource(i);
    try(var tx=Transaction.openRoot()){
     int inserted=destination.insert(resource,amount,tx);if(inserted<0 || inserted>amount)throw new IllegalStateException("Invalid crafter destination result");
     if(inserted>0 && inventory.extract(i,resource,inserted,tx)==inserted){tx.commit();return inserted;}
    }
   }
   return 0;
  }finally{working=false;}
 }
 public void work(){
  scheduled=false;if(!available())return;
  if(neighbors.isEmpty())for(var direction:Direction.values())neighbors.add(BlockCapabilityCache.create(Capabilities.Item.BLOCK,(ServerLevel)level,worldPosition.relative(direction),direction.getOpposite()));
  boolean moved=false;
  try{if(hasOutput())for(var cache:neighbors){var target=cache.getCapability();if(target!=null)moved|=pushTo(target)>0;}
   boolean crafted=craftOne();if(crafted || moved)wake(5);else if(hasOutput())wake(40);
  }catch(RuntimeException error){
   // A compliant transaction rolls back. Keep the original serialized state locked if a handler failed.
   var ops=com.neolena.essenceindustry.library.SeedDataSafety.ops(level.registryAccess());
   var tag=new net.minecraft.nbt.CompoundTag();tag.put("unresolvedTransfer",ItemStack.OPTIONAL_CODEC.listOf().encodeStart(ops,snapshot()).getOrThrow());recovery=new Dynamic<>(ops,tag);setChanged();
   org.slf4j.LoggerFactory.getLogger(getClass()).error("Seed crafter at {} locked after transfer/recipe failure; inventory retained",worldPosition,error);
  }
 }
 private List<ItemStack> snapshot(){var out=new ArrayList<ItemStack>();for(int i=0;i<SLOTS;i++)out.add(inventory.getResource(i).toStack(inventory.getAmountAsInt(i)));return out;}
 private void loadRaw(Dynamic<?> raw){
  var decoded=ItemStack.OPTIONAL_CODEC.listOf().parse(raw);if(decoded.error().isPresent() || decoded.result().orElseThrow().size()!=SLOTS){recovery=raw;return;}
  recovery=null;var stacks=decoded.result().orElseThrow();for(int i=0;i<SLOTS;i++)inventory.set(i,ItemResource.of(stacks.get(i)),stacks.get(i).getCount());
 }
 @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(recovery==null)inventory.serialize(out);else out.store("stacks",Codec.PASSTHROUGH,recovery);}
 @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);var raw=in.read("stacks",Codec.PASSTHROUGH);if(raw.isPresent())loadRaw(raw.get());}
 @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder data){super.collectImplicitComponents(data);InventoryRecovery.collect(data,"seedCrafter",recovery);}
 @Override protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter data){super.applyImplicitComponents(data);var raw=InventoryRecovery.read(data,"seedCrafter",level==null?com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup():level.registryAccess());if(raw!=null)loadRaw(raw);}
 @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){if(!quarantined() && level!=null && !level.isClientSide())for(var stack:snapshot())if(!stack.isEmpty())Containers.dropItemStack(level,pos.getX(),pos.getY(),pos.getZ(),stack);super.preRemoveSideEffects(pos,state);}
}
