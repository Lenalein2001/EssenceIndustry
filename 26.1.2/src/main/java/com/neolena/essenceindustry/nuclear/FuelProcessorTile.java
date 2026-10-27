package com.neolena.essenceindustry.nuclear;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neolena.essenceindustry.seedcraft.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;

/** One durable timed batch per plant; held inputs and exact FE progress share transactions. */
public final class FuelProcessorTile extends SeedCrafterTile implements NuclearHost {
 private record Job(String product,List<ItemStack> inputs,List<ItemStack> outputs,int duration,int progress,int energy,int paid){
  private static final Codec<Job> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.fieldOf("product").forGetter(Job::product),ItemStack.CODEC.listOf().fieldOf("inputs").forGetter(Job::inputs),ItemStack.CODEC.listOf().fieldOf("outputs").forGetter(Job::outputs),Codec.intRange(1,12000).fieldOf("duration").forGetter(Job::duration),Codec.intRange(0,12000).fieldOf("progress").forGetter(Job::progress),Codec.intRange(1,1000000).fieldOf("energy").forGetter(Job::energy),Codec.intRange(0,1000000).fieldOf("paid").forGetter(Job::paid)).apply(i,Job::new));
  private Job next(int ticks){int progress=Math.min(duration,this.progress+ticks);return new Job(product,inputs,outputs,duration,progress,energy,(int)((long)energy*progress/duration));}
 }
 private Job job;private Dynamic<?> jobRecovery;private int jobSchema=1;
 private String lastRecipe="";
 private long queueRevision=Long.MIN_VALUE;private String queueCursor="";private int queueMode=-1,readyCount,nextProduct,nextInput;
 private record Candidate(FuelCycle.Process recipe,int[] used,String key){}
 private List<FuelCycle.Process> recipes(){return FuelCycle.PROCESSES.stream().filter(p->p.machine().equals(machine())).toList();}
 private static String recipeKey(FuelCycle.Process p){return p.inputs().stream().map(i->i.ingredient()+"="+i.count()).collect(java.util.stream.Collectors.joining(";"))+"->"+p.outputs().getFirst().item();}
 /** Fair round-robin among ready recipes; a complete specific recipe owns its shared inputs. */
 private Candidate select(int[] amounts,String cursor){
  var recipes=recipes();var matches=new ArrayList<Candidate>();
  for(var recipe:recipes){
   if(machine().equals("fuel_fabricator")&&fabricationMode<4&&!recipe.outputs().getFirst().item().equals(List.of("plutonium_fuel","mox_fuel","thorium_fuel","advanced_nuclear_fuel").get(fabricationMode)))continue;
   int[] used=new int[INPUTS];boolean matched=true;
   for(var input:recipe.inputs()){int needed=input.count();for(int i=0;i<INPUTS&&needed>0;i++)if(input.matches(inventory().getResource(i).toStack())){int n=Math.min(needed,amounts[i]-used[i]);used[i]+=n;needed-=n;}if(needed>0){matched=false;break;}}
   if(matched)matches.add(new Candidate(recipe,used,recipeKey(recipe)));
  }
  int start=0;for(int i=0;i<recipes.size();i++)if(recipeKey(recipes.get(i)).equals(cursor)){start=(i+1)%recipes.size();break;}
  for(int offset=0;offset<recipes.size();offset++){
   var recipe=recipes.get((start+offset)%recipes.size());Candidate candidate=null;for(var c:matches)if(c.recipe==recipe){candidate=c;break;}if(candidate==null)continue;
   boolean specific=false;for(var other:matches)if(other.recipe.inputs().size()>recipe.inputs().size())for(int i=0;i<INPUTS;i++)if(other.used[i]>0&&candidate.used[i]>0){specific=true;break;}
   if(!specific)return candidate;
  }
  return null;
 }
 private int[] inputAmounts(){int[] amounts=new int[INPUTS];for(int i=0;i<INPUTS;i++)amounts[i]=inventory().getAmountAsInt(i);return amounts;}
 private void refreshQueue(){
  if(queueRevision==revision()&&queueMode==fabricationMode&&queueCursor.equals(lastRecipe))return;
  queueRevision=revision();queueMode=fabricationMode;queueCursor=lastRecipe;readyCount=0;nextProduct=nextInput=0;
  int[] amounts=inputAmounts();String cursor=lastRecipe;
  // Nine input stacks bound the number of one-item batches to 576.
  for(int n=0;n<INPUTS*64;n++){var candidate=select(amounts,cursor);if(candidate==null)break;if(readyCount==0){nextProduct=BuiltInRegistries.ITEM.getId(NuclearFuels.stack(candidate.recipe.outputs().getFirst().item(),1).getItem());for(int i=0;i<INPUTS;i++)if(candidate.used[i]>0){nextInput=BuiltInRegistries.ITEM.getId(inventory().getResource(i).toStack().getItem());break;}}readyCount++;for(int i=0;i<INPUTS;i++)amounts[i]-=candidate.used[i];cursor=candidate.key;}
 }
 public int readyBatches(){refreshQueue();return readyCount;}
 public int nextProductId(){refreshQueue();return nextProduct;}
 public int nextInputId(){refreshQueue();return nextInput;}
 public int currentProductId(){return job==null?0:BuiltInRegistries.ITEM.getId(job.outputs.getFirst().getItem());}
 public int currentInputId(){return job==null?0:BuiltInRegistries.ITEM.getId(job.inputs.getFirst().getItem());}

 private record JobState(Job value){}
 private List<BlockPos> transferPorts=List.of();
 private long lastPortTransfer=Long.MIN_VALUE;private int pullCursor;
 private int issueItem,issueFound,issueRequired;
 public int issueItem(){return issueItem;}public int issueFound(){return issueFound;}public int issueRequired(){return issueRequired;}
 private int fabricationMode=4,fuelStatus=5;private boolean processing,formed,structureDirty=true,running,visualRunning;private long lastStructureCheck=Long.MIN_VALUE,attemptedRevision=-1,attemptedEnergy=-1;private BlockPos issue=BlockPos.ZERO;
 private final SnapshotJournal<JobState> journal=new SnapshotJournal<>(){protected JobState createSnapshot(){return new JobState(job);}protected void revertToSnapshot(JobState before){job=before.value;}protected void onRootCommit(JobState before){setChanged();}};
 private final SimpleEnergyHandler energy=new SimpleEnergyHandler(1000000,1000000,1000000){@Override protected void onEnergyChanged(int old){setChanged();wake(5);}};
 private final ResourceHandler<ItemResource> inputPort=createPort(true),outputPort=createPort(false);
 private final EnergyHandler energyInput=new EnergyHandler(){
  public long getAmountAsLong(){return available()?energy.getAmountAsLong():0;}public long getCapacityAsLong(){return available()?energy.getCapacityAsLong():0;}
  public int insert(int amount,TransactionContext tx){net.neoforged.neoforge.transfer.TransferPreconditions.checkNonNegative(amount);return available()?energy.insert(amount,tx):0;}
  public int extract(int amount,TransactionContext tx){net.neoforged.neoforge.transfer.TransferPreconditions.checkNonNegative(amount);return 0;}
 };
 public EnergyHandler automationEnergy(){return energyInput;}
 public FuelProcessorTile(BlockPos pos,BlockState state){super(NuclearFuels.TILE.get(),pos,state);}
 public String machine(){return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();}
 public BlockPos layoutAnchor(){return com.neolena.essenceindustry.blueprint.ControllerPlacement.anchor(this);}
 public Direction facing(){return com.neolena.essenceindustry.blueprint.ControllerPlacement.facing(this,getBlockState().getValue(FuelProcessorBlock.FACING));}
 public FuelPlantLayout.Layout layout(){return FuelPlantLayout.get(machine());}
 public int fabricationMode(){return fabricationMode;}
 public void cycleMode(){if(available()){fabricationMode=(fabricationMode+1)%5;attemptedRevision=-1;setChanged();wake(5);}}
 public EnergyHandler energy(){return energy;}
 public boolean hasJob(){return job!=null;}
 public int progress(){return job==null?0:1000*job.progress/job.duration;}
 public int elapsedTicks(){return job==null?0:job.progress;}
 public int durationTicks(){return job==null?0:job.duration;}
 public int paidEnergy(){return job==null?0:job.paid;}
 public BlockPos issue(){return issue;}
 public boolean running(){return running;}
 public boolean formed(){return formed&&!structureDirty&&available()&&chunksLoaded();}
 /** Fixed plants span only a few chunks. Checking them never requests or loads a chunk. */
 private boolean chunksLoaded(){
  if(level==null)return false;var min=layout().minimum(layoutAnchor(),facing());var max=layout().maximum(layoutAnchor(),facing());
  for(int x=min.getX()>>4;x<=max.getX()>>4;x++)for(int z=min.getZ()>>4;z<=max.getZ()>>4;z++)if(!level.hasChunkAt(new BlockPos(x<<4,worldPosition.getY(),z<<4)))return false;
  return true;
 }
 @Override public boolean quarantined(){return jobRecovery!=null||super.quarantined();}
 @Override public boolean safeToMove(){return com.neolena.essenceindustry.blueprint.BlueprintStorage.safeToMove(this)&&!processing&&jobRecovery==null&&!(job!=null&&super.quarantined())&&super.safeToMove();}
 @Override public boolean available(){return !processing&&super.available();}
 @Override public int status(){return quarantined()?3:fuelStatus;}
 @Override public boolean contains(BlockPos pos){var min=layout().minimum(layoutAnchor(),facing());var max=layout().maximum(layoutAnchor(),facing());return pos.getX()>=min.getX()&&pos.getX()<=max.getX()&&pos.getY()>=min.getY()&&pos.getY()<=max.getY()&&pos.getZ()>=min.getZ()&&pos.getZ()<=max.getZ();}
 @Override public void partChanged(BlockPos pos){if(contains(pos)){structureDirty=true;attemptedRevision=-1;wake(2);StructureValidationQueue.request(this,this::structureTick);}}
 @Override public void open(Player player){if(player instanceof ServerPlayer server&&available())server.openMenu(new SimpleMenuProvider((id,inv,p)->new SeedCrafterMenu(id,inv,this),Component.translatable(getBlockState().getBlock().getDescriptionId())),b->{b.writeBlockPos(worldPosition);b.writeVarInt(BuiltInRegistries.BLOCK.getId(getBlockState().getBlock()));});}
 @Override public ResourceHandler<ItemResource> itemPort(String role){return switch(role){case "fuel_input"->inputPort;case "waste_output"->outputPort;default->null;};}
 private ResourceHandler<ItemResource> createPort(boolean input){
  var delegate=automation();
  return new ResourceHandler<>(){
   private boolean allowed(int i){return formed()&&i>=0&&i<SLOTS&&(input?i<INPUTS:i>=INPUTS);}
   public int size(){return formed()?SLOTS:0;}
   public ItemResource getResource(int i){return allowed(i)?delegate.getResource(i):ItemResource.EMPTY;}
   public long getAmountAsLong(int i){return allowed(i)?delegate.getAmountAsLong(i):0;}
   public long getCapacityAsLong(int i,ItemResource r){return allowed(i)?delegate.getCapacityAsLong(i,r):0;}
   public boolean isValid(int i,ItemResource r){return input&&allowed(i)&&delegate.isValid(i,r);}
   public int insert(int i,ItemResource r,int n,TransactionContext tx){return input&&allowed(i)?delegate.insert(i,r,n,tx):0;}
   public int extract(int i,ItemResource r,int n,TransactionContext tx){return !input&&allowed(i)?delegate.extract(i,r,n,tx):0;}
  };
 }
 @Override public EnergyHandler energyPort(String role){return role.equals("energy_out")&&formed()?energyInput:null;}
 private void registerOwner(){if(level instanceof ServerLevel)NuclearOwners.register(level,worldPosition,layout().minimum(layoutAnchor(),facing()),layout().maximum(layoutAnchor(),facing()));}
 @Override public void onLoad(){super.onLoad();com.neolena.essenceindustry.blueprint.BlueprintAutomation.loaded(this);structureDirty=true;lastStructureCheck=Long.MIN_VALUE;registerOwner();}
 @Override public void onChunkUnloaded(){com.neolena.essenceindustry.blueprint.BlueprintAutomation.unloaded(this);if(level!=null)NuclearOwners.unregister(level,worldPosition);running=false;super.onChunkUnloaded();}
 @Override public void setRemoved(){com.neolena.essenceindustry.blueprint.BlueprintAutomation.unloaded(this);if(level!=null)NuclearOwners.unregister(level,worldPosition);super.setRemoved();}
 public void structureTick(){if(structureDirty&&!isRemoved()&&available())validateStructure();}
 public boolean validateStructure(){
  if(!(level instanceof ServerLevel server))return false;if(!formed&&!FuelPlantStructure.check(server,worldPosition,facing(),machine()).valid())FuelPlantStructure.discover(this);registerOwner();var result=FuelPlantStructure.check(server,worldPosition,facing(),machine());lastStructureCheck=level.getGameTime();structureDirty=false;attemptedRevision=-1;issue=result.issue();issueItem=result.component().isEmpty()?0:BuiltInRegistries.ITEM.getId(FuelPlantLayout.block(machine(),result.component()).asItem());issueFound=result.found();issueRequired=result.required();
  if(result.valid()){var foundPorts=new ArrayList<BlockPos>();for(var p:BlockPos.betweenClosed(layout().minimum(layoutAnchor(),facing()),layout().maximum(layoutAnchor(),facing())))if(NuclearReactor.PORTS.contains(NuclearReactor.kind(level.getBlockState(p))))foundPorts.add(p.immutable());transferPorts=List.copyOf(foundPorts);}else transferPorts=List.of();
  boolean changed=formed!=result.valid();formed=result.valid();if(!formed){fuelStatus=result.status();running=false;}else if(fuelStatus>=5&&fuelStatus!=9)fuelStatus=job==null?1:0;
  if(changed&&result.status()!=7)FuelPlantStructure.visuals(server,worldPosition,facing(),machine(),formed,false);
  return formed;
 }
 private void visuals(){if(!(level instanceof ServerLevel server))return;if(visualRunning!=running){visualRunning=running;FuelPlantStructure.visuals(server,worldPosition,facing(),machine(),formed,running);}}
 /** Parent scheduler also drains completed output through ordinary adjacent inventories. */
 @Override public void work(){super.work();visuals();if(!isRemoved()&&!quarantined())wake(job==null?20:5);}
 public static int duration(FuelCycle.Process recipe){return recipe.machine().equals("fuel_enricher")?200:recipe.machine().equals("fuel_reprocessor")||recipe.outputs().getFirst().item().equals("advanced_nuclear_fuel")?400:100;}
 private boolean outputsFit(List<ItemStack> outputs,TransactionContext tx){for(var output:outputs){var resource=ItemResource.of(output);int left=output.getCount();for(int i=INPUTS;i<SLOTS&&left>0;i++)left-=inventory().insert(i,resource,left,tx);if(left>0)return false;}return true;}
 private boolean canReserve(List<ItemStack> outputs){try(var tx=Transaction.openRoot()){return outputsFit(outputs,tx);}}
 @Override public boolean craftOne(){
  if(!available()||Transaction.getCurrentOpenedTransaction()!=null)return false;running=false;
  if(structureDirty||lastStructureCheck==Long.MIN_VALUE||level.getGameTime()-lastStructureCheck>=20)validateStructure();
  if(!chunksLoaded()){structureDirty=true;formed=false;fuelStatus=7;return false;}
  if(!formed())return false;
  long now=level.getGameTime();
  // Work wakes relative to this host, so a global modulo gate can starve every transfer.
  if(lastPortTransfer==Long.MIN_VALUE||now<lastPortTransfer||now-lastPortTransfer>=5){
   lastPortTransfer=now;pullCursor=NuclearPortTile.pullBatch(level,transferPorts,pullCursor);
   for(var p:transferPorts){if(!NuclearPortTile.active(level,p)||!NuclearReactor.role(level.getBlockState(p)).equals("waste_output"))continue;var face=NuclearPart.facing(level.getBlockState(p));var neighbor=p.relative(face);if(contains(neighbor)||!level.hasChunkAt(neighbor))continue;var destination=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,neighbor,face.getOpposite());if(destination!=null)pushTo(destination);}
  }

  if(level.hasNeighborSignal(worldPosition)){fuelStatus=9;return false;}
  if(job!=null)return advance();
  if(attemptedRevision==revision()&&attemptedEnergy==energy.getAmountAsLong())return false;
  attemptedRevision=revision();attemptedEnergy=energy.getAmountAsLong();
  var candidate=select(inputAmounts(),lastRecipe);
  if(candidate!=null){
   var recipe=candidate.recipe;var used=candidate.used;
   int duration=duration(recipe);if(energy.getAmountAsLong()<(long)recipe.energy()*Math.min(5,duration)/duration){fuelStatus=4;return false;}
   var outputs=recipe.outputs().stream().map(o->NuclearFuels.stack(o.item(),o.count())).toList();if(!canReserve(outputs)){fuelStatus=2;return false;}
   var inputs=new ArrayList<ItemStack>();for(int i=0;i<INPUTS;i++)if(used[i]>0)inputs.add(inventory().getResource(i).toStack(used[i]));
   processing=true;
   try(var tx=Transaction.openRoot()){
    for(int i=0;i<INPUTS;i++)if(used[i]>0&&inventory().extract(i,inventory().getResource(i),used[i],tx)!=used[i])return false;
    journal.updateSnapshots(tx);job=new Job(recipe.outputs().getFirst().item(),List.copyOf(inputs),outputs,duration,0,recipe.energy(),0);tx.commit();lastRecipe=candidate.key;setChanged();fuelStatus=0;return true;
   }finally{processing=false;}
  }
  fuelStatus=1;return false;
 }
 private boolean advance(){
  var next=job.next(5);int cost=next.paid-job.paid;
  if(energy.getAmountAsLong()<cost){fuelStatus=4;return false;}
  // Capacity was reserved before inputs moved. Recheck against manual/admin changes safely.
  if(!canReserve(job.outputs)){fuelStatus=2;return false;}
  processing=true;
  try(var tx=Transaction.openRoot()){
   if(energy.extract(cost,tx)!=cost)return false;
   if(next.progress==next.duration&&!outputsFit(next.outputs,tx)){fuelStatus=2;return false;}
   journal.updateSnapshots(tx);job=next.progress==next.duration?null:next;tx.commit();running=job!=null;fuelStatus=0;attemptedRevision=-1;return true;
  }finally{processing=false;}
 }
 private boolean validJob(Job value){
  if(value.inputs.isEmpty()||value.inputs.size()>INPUTS||value.outputs.isEmpty()||value.outputs.size()>10||value.progress>value.duration||value.paid!=(int)((long)value.energy*value.progress/value.duration))return false;
  // Several spent fuels share an output. Match the complete reserved recipe, not only its product.
  return FuelCycle.PROCESSES.stream().filter(r->r.machine().equals(machine())&&r.outputs().getFirst().item().equals(value.product)).anyMatch(recipe->matchesJob(value,recipe));
 }
 private boolean matchesJob(Job value,FuelCycle.Process recipe){
  if(duration(recipe)!=value.duration||recipe.energy()!=value.energy||recipe.outputs().size()!=value.outputs.size())return false;
  for(int i=0;i<value.outputs.size();i++){var expected=recipe.outputs().get(i);if(!ItemStack.matches(value.outputs.get(i),NuclearFuels.stack(expected.item(),expected.count())))return false;}
  int count=value.inputs.stream().mapToInt(ItemStack::getCount).sum();if(count!=recipe.inputs().stream().mapToInt(FuelCycle.Input::count).sum()||value.inputs.stream().anyMatch(s->s.isEmpty()||s.getCount()>64))return false;
  int[] consumed=new int[value.inputs.size()];for(var ingredient:recipe.inputs()){int remaining=ingredient.count();for(int i=0;i<value.inputs.size()&&remaining>0;i++)if(ingredient.matches(value.inputs.get(i))){int n=Math.min(remaining,value.inputs.get(i).getCount()-consumed[i]);consumed[i]+=n;remaining-=n;}if(remaining>0)return false;}return true;
 }
 @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("fuelProcessorEnergy",energy.getAmountAsInt());out.putInt("fabricationMode",fabricationMode);out.putString("fuelLastRecipe",lastRecipe);if(jobRecovery!=null||job!=null)out.putInt("fuelJobSchema",jobSchema);if(jobRecovery!=null)out.store("fuelJob",Codec.PASSTHROUGH,jobRecovery);else if(job!=null)out.store("fuelJob",Job.CODEC,job);}
 @Override protected void loadAdditional(ValueInput in){
  super.loadAdditional(in);fabricationMode=Math.clamp(in.getIntOr("fabricationMode",0),0,4);lastRecipe=in.getStringOr("fuelLastRecipe","");queueRevision=Long.MIN_VALUE;energy.set(Math.clamp(in.getIntOr("fuelProcessorEnergy",0),0,1000000));job=null;jobRecovery=null;jobSchema=in.getIntOr("fuelJobSchema",1);
  var raw=in.read("fuelJob",Codec.PASSTHROUGH);if(raw.isPresent()){var decoded=Job.CODEC.parse(raw.get());if(jobSchema==1&&decoded.result().isPresent()&&validJob(decoded.result().get()))job=decoded.result().get();else jobRecovery=raw.get();}
  attemptedRevision=-1;attemptedEnergy=-1;structureDirty=true;formed=false;running=false;visualRunning=false;
 }
 @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){com.neolena.essenceindustry.blueprint.BlueprintStorage.drop(this);
  if(level instanceof ServerLevel server){FuelPlantStructure.visuals(server,worldPosition,facing(),machine(),false,false);if(job!=null&&!quarantined()){var held=job.inputs;job=null;for(var input:held)Containers.dropItemStack(level,pos.getX(),pos.getY(),pos.getZ(),input.copy());}NuclearOwners.unregister(level,worldPosition);}
  super.preRemoveSideEffects(pos,state);
 }
}
