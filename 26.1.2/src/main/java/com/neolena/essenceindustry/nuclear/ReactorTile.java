package com.neolena.essenceindustry.nuclear;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neolena.essenceindustry.library.*;
import com.neolena.essenceindustry.item.InventoryRecovery;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.fluid.*;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;
/** One server simulation and one durable ledger for the complete reactor. */
public final class ReactorTile extends BlockEntity implements NuclearHost {
 private record Saved(int version,ReactorCore.State core,List<ItemStack> items,int width,int depth,int height,int water,int returned,int steam,int credit,boolean active){
  static final Codec<Saved> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,2).fieldOf("version").forGetter(Saved::version),ReactorCore.State.CODEC.fieldOf("core").forGetter(Saved::core),ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(Saved::items),Codec.intRange(5,64).fieldOf("width").forGetter(Saved::width),Codec.intRange(5,64).fieldOf("depth").forGetter(Saved::depth),Codec.intRange(5,64).fieldOf("height").forGetter(Saved::height),Codec.intRange(0,ReactorSteam.MAX_TANK).fieldOf("water").forGetter(Saved::water),Codec.intRange(0,8000).fieldOf("returned").forGetter(Saved::returned),Codec.intRange(0,ReactorSteam.MAX_TANK).optionalFieldOf("steam",0).forGetter(Saved::steam),Codec.intRange(0,ReactorSteam.MAX_CREDIT).optionalFieldOf("steamCredit",0).forGetter(Saved::credit),Codec.BOOL.optionalFieldOf("activeCooling",false).forGetter(Saved::active)).apply(i,Saved::new));
 }
 private record Ledger(ReactorCore.State core,int water,int returned,int steam,int credit){}
 private ReactorCore.State core=ReactorCore.State.empty();private int water,returned,steam,steamCredit;private boolean activeCooling;private long production;
 private int width=5,depth=5,height=5,fuelSegments=0,waterInputs,steamOutputs,moderated,reflected,status=ReactorCore.Status.UNFORMED.ordinal();
 private boolean formed,running,unloaded,dirty=true,checking,working,actuatorMissing,visualDirty=true,coldVisualReset=true;private int lastFuel=-1,lastLevel=-1,lastRods=-1,visualRods=50,visualResetCursor=-1;private boolean lastRunning;
 private long nextAudit,nextSync;private int energyCursor,wasteCursor,waterCursor;private String issue="Build the shell and columns; open the guide for a starter layout";private Dynamic<?> recovery;
 private ReactorStructure scan;private List<BlockPos> fuelColumns=List.of(),controlColumns=List.of(),ports=List.of(),parts=List.of();
 private static final Map<Level,Set<ReactorTile>> CLIENTS=new WeakHashMap<>();
 private final SnapshotJournal<Ledger> journal=new SnapshotJournal<>(){protected Ledger createSnapshot(){return new Ledger(core,water,returned,steam,steamCredit);}protected void revertToSnapshot(Ledger s){core=s.core;water=s.water;returned=s.returned;steam=s.steam;steamCredit=s.credit;}protected void onRootCommit(Ledger before){setChanged();}};
 private final ItemStacksResourceHandler inventory=new ItemStacksResourceHandler(8){@Override protected void onContentsChanged(int slot,ItemStack before){setChanged();}};
 public ReactorTile(BlockPos p,BlockState s){super(NuclearReactor.TILE.get(),p,s);}
 public ReactorCore.State core(){return core;}public ItemStacksResourceHandler inventory(){return inventory;}
 public int width(){return width;}public int depth(){return depth;}public int height(){return height;}public int fuelSegments(){return fuelSegments;}public boolean running(){return running;}public int statusCode(){return recovery==null?status:ReactorCore.Status.RECOVERY_REQUIRED.ordinal();}public Component issue(){return Component.literal(recovery!=null?"Stored data needs recovery; contents are preserved":checking?"Checking structure...":activeCooling&&formed&&waterInputs==0?"Add a Fluid Port in Input mode to a shell face":activeCooling&&formed&&steamOutputs==0?"Add a Fluid Port in Output mode to a shell face":issue);}
 public int steam(){return steam;}public int fluidCapacity(){return ReactorSteam.capacity(fuelSegments);}public boolean activeCooling(){return activeCooling;}public long production(){return production;}public int steamCredit(){return steamCredit;}
 public boolean canSwitchMode(){return !core.enabled()&&steam==0&&returned==0&&core.energy()<=rules().energyCapacity()-steamCredit;}
 public int water(){return water;}public int returnedWater(){return returned;}public int waterInputPorts(){return waterInputs;}public int steamOutputPorts(){return steamOutputs;}
 public int controlColumnCount(){return controlColumns.size();}
 public Direction facing(){return NuclearPart.facing(getBlockState());}
 public BlockPos layoutAnchor(){return com.neolena.essenceindustry.blueprint.ControllerPlacement.anchor(this);}
 private void discoverController(){if(!formed&&scan==null&&com.neolena.essenceindustry.blueprint.ControllerPlacement.discoverWall(this,facing(),width,height,width/2,ReactorStructure::shell)){registerOwner();sync();}}
 public BlockPos position(int x,int y,int z){return NuclearLayout.pos(layoutAnchor(),facing(),width,x,y,z);}
 public BlockPos minimum(){return NuclearLayout.min(position(0,0,0),position(width-1,height-1,depth-1));}public BlockPos maximum(){return NuclearLayout.max(position(0,0,0),position(width-1,height-1,depth-1));}
 public static List<ReactorTile> clientTiles(Level level){return List.copyOf(CLIENTS.getOrDefault(level,Set.of()));}
 public List<BlockPos> visualFuelColumns(){return fuelColumns;}
 public List<BlockPos> visualControlColumns(){return controlColumns;}
 public int visualRodInsertion(){return level!=null&&level.isClientSide()?visualRods:core.rods();}
 public boolean available(){return recovery==null&&!unloaded&&!isRemoved()&&level instanceof ServerLevel&&level.hasChunkAt(worldPosition)&&level.getBlockEntity(worldPosition)==this;}
 @Override public boolean formed(){return formed&&!unloaded&&!dirty&&recovery==null;}
 @Override public boolean contains(BlockPos p){var a=minimum();var b=maximum();return p.getX()>=a.getX()&&p.getX()<=b.getX()&&p.getY()>=a.getY()&&p.getY()<=b.getY()&&p.getZ()>=a.getZ()&&p.getZ()<=b.getZ();}
 private boolean chunksLoaded(){var a=minimum();var b=maximum();for(int x=a.getX()>>4;x<=b.getX()>>4;x++)for(int z=a.getZ()>>4;z<=b.getZ()>>4;z++)if(!level.hasChunkAt(new BlockPos(x<<4,worldPosition.getY(),z<<4)))return false;return true;}
 private void registerOwner(){if(level!=null&&!level.isClientSide())NuclearOwners.register(level,worldPosition,minimum(),maximum());}
 @Override public void partChanged(BlockPos p){if(contains(p)){dirty=true;scan=null;StructureValidationQueue.request(this,this::structureTick);}}
 @Override public void open(Player p){if(p instanceof ServerPlayer player)player.openMenu(new SimpleMenuProvider((id,inv,who)->new ReactorMenu(id,inv,worldPosition),Component.translatable("block.essenceindustry.reactor_controller")),b->b.writeBlockPos(worldPosition));}
 public void adjustSize(int axis,int delta){if(!available()||working||core.enabled()||axis<0||axis>2||Math.abs(delta)!=1)return;int w=width,d=depth,h=height;if(axis==0)w=Math.clamp(w+delta,5,ReactorConfig.maxWidth());if(axis==1)d=Math.clamp(d+delta,5,ReactorConfig.maxDepth());if(axis==2)h=Math.clamp(h+delta,5,ReactorConfig.maxHeight());if(w==width&&d==depth&&h==height)return;clearVisuals();width=w;depth=d;height=h;formed=false;dirty=true;scan=null;registerOwner();setChanged();sync();}
 public void control(int action){if(!available()||working||Transaction.getCurrentOpenedTransaction()!=null)return;if(action==4){dirty=true;scan=null;return;}if(action==5){if(!canSwitchMode())return;activeCooling=!activeCooling;if(!activeCooling&&steamCredit>0){core=ReactorSteam.energy(core,core.energy()+steamCredit);steamCredit=0;}production=0;setChanged();sync();return;}var next=switch(action){case 0->ReactorCore.controls(core,!core.enabled(),core.rods());case 1->ReactorCore.controls(core,core.enabled(),Math.max(0,core.rods()-1));case 2->ReactorCore.controls(core,core.enabled(),Math.min(100,core.rods()+1));case 6->ReactorCore.controls(core,core.enabled(),Math.max(0,core.rods()-10));case 7->ReactorCore.controls(core,core.enabled(),Math.min(100,core.rods()+10));case 3->ReactorCore.resetTrip(core,rules());default->null;};if(next!=null){core=next;setChanged();visualDirty=true;sync();}}
 public ReactorCore.Rules rules(){var base=ReactorConfig.rules();if(fuelSegments==0)return base;var rates=new LinkedHashMap<String,ReactorFuels.Rate>();for(var e:base.rates().entrySet()){var r=e.getValue();long fe=Math.min(100_000_000_000L,r.fePerMb()+r.fePerMb()*reflected/(4L*fuelSegments));long heat=r.heatPerMb()-r.heatPerMb()*moderated/(4L*fuelSegments);rates.put(e.getKey(),new ReactorFuels.Rate(r.burnUnitsPerSegmentTick(),fe,heat));}return new ReactorCore.Rules(base.fuelCapacityMb(),base.wasteCapacityMb(),base.energyCapacity(),base.heatCapacityUnits(),base.passiveCoolingUnitsPerSegment(),rates);}
 public void structureTick(){if(dirty&&available()&&chunksLoaded()){discoverController();validate();}}
 private void validate(){registerOwner();if(scan==null)scan=new ReactorStructure(this);checking=true;var result=scan.step(level,dirty?width*depth*height:1024);if(result==null)return;scan=null;checking=false;nextAudit=level.getGameTime()+100;dirty=false;actuatorMissing=result.actuatorMissing();issue=result.issue()+(result.valid()?"":" at "+result.position().getX()+", "+result.position().getY()+", "+result.position().getZ());if(!result.valid()){if(formed)clearVisuals();formed=false;controlColumns=result.controls();if(actuatorMissing){parts=result.parts();clearVisuals();}if(coldVisualReset){visualResetCursor=0;coldVisualReset=false;}visualDirty=true;sync();return;}coldVisualReset=false;visualResetCursor=-1;formed=true;fuelColumns=result.fuel();controlColumns=result.controls();ports=result.ports();parts=result.parts();fuelSegments=fuelColumns.size()*(height-2);waterInputs=steamOutputs=0;for(var p:ports){String role=NuclearReactor.role(level.getBlockState(p));if(role.equals("coolant_in"))waterInputs++;else if(role.equals("coolant_out"))steamOutputs++;}moderated=result.moderated();reflected=result.reflected();visualDirty=true;sync();}
 /** Saved formed flags can outlive the host's transient part list. Restore normal rods once, in bounded slices. */
 private void resetColdControlVisuals(){
  if(visualResetCursor<0||formed)return;
  int count=width*depth*height;
  for(int budget=0;budget<1024&&visualResetCursor<count;budget++,visualResetCursor++){
   int i=visualResetCursor;var p=position(i%width,i/(width*depth),i/width%depth);
   if(!level.hasChunkAt(p))return;
   var state=NuclearVisualUpdates.state(level,p);
   if(state.hasProperty(NuclearPart.CONTROLLED)&&state.getValue(NuclearPart.CONTROLLED)){var owner=NuclearOwners.at(level,p);if(owner==null||owner==this||!owner.formed())NuclearVisualUpdates.set(level,p,state.setValue(NuclearPart.CONTROLLED,false));}
  }
  if(visualResetCursor>=count)visualResetCursor=-1;
 }
 public void tick(){
  if(!available()||Transaction.getCurrentOpenedTransaction()!=null)return;
  try{
   if(dirty&&scan==null)discoverController();
   if(!chunksLoaded()){dirty=true;scan=null;checking=false;issue="Waiting for all reactor chunks";running=false;production=0;status=ReactorCore.Status.UNFORMED.ordinal();if(level.getGameTime()>=nextSync)sync();return;}
   if(width>ReactorConfig.maxWidth()||depth>ReactorConfig.maxDepth()||height>ReactorConfig.maxHeight()){dirty=true;formed=false;issue="Dimensions exceed the server limits";running=false;production=0;status=ReactorCore.Status.CONFIG_BLOCKED.ordinal();return;}
   if(dirty||scan!=null||level.getGameTime()>=nextAudit)validate();
   resetColdControlVisuals();
   working=true;boolean operable=formed&&!dirty&&!level.hasNeighborSignal(worldPosition);var limits=rules();
   if(operable){solidify();loadFuel(limits);}
   boolean blocked=ReactorCore.wasteOffer(core).count()>0&&!wasteFits();
   production=0;
   try(var tx=Transaction.openRoot()){journal.updateSnapshots(tx);
    if(activeCooling){var result=ReactorSteam.tick(core,limits,Math.max(1,fuelSegments),operable&&returned==0&&waterInputs>0&&steamOutputs>0,blocked,water,steam,steamCredit);core=result.core();steam=result.steam();steamCredit=result.credit();water-=result.produced();production=result.produced();running=result.status()==ReactorCore.Status.RUNNING;status=returned>0?ReactorCore.Status.LEGACY_COOLANT.ordinal():result.status().ordinal();if(operable&&returned==0&&!core.tripped()&&(waterInputs==0||steamOutputs==0))status=(!core.enabled()?ReactorCore.Status.OFF:core.rods()==100?ReactorCore.Status.RODS_INSERTED:waterInputs==0?ReactorCore.Status.WATER_INPUT_MISSING:ReactorCore.Status.STEAM_OUTPUT_MISSING).ordinal();}
    else{var result=ReactorCore.tick(core,limits,Math.max(1,fuelSegments),operable,blocked,0);core=result.state();production=result.generatedFE();running=result.working();status=result.status().ordinal();}
    if(!formed&&actuatorMissing&&!checking)status=ReactorCore.Status.ACTUATOR_MISSING.ordinal();
    if(formed&&!dirty&&level.hasNeighborSignal(worldPosition))status=ReactorCore.Status.REDSTONE_PAUSED.ordinal();tx.commit();}
   working=false;
   if(formed&&!dirty){NuclearPortTile.pull(level,ports);pushEnergy();pushWater();if(level.getGameTime()%5==0)pushWaste();}
   if(visualDirty||running!=lastRunning||fuelIndex()!=lastFuel||fuelLevel()!=lastLevel||core.rods()!=lastRods)applyVisuals();
   if(level.getGameTime()>=nextSync)sync();
  }catch(RuntimeException error){working=false;var envelope=new CompoundTag();envelope.put("unresolvedTransfer",payload().convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue().copy());recovery=new Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE,envelope);running=false;production=0;setChanged();org.slf4j.LoggerFactory.getLogger(getClass()).error("Reactor at {} quarantined after processing/transfer failure; ledger retained without automatic retry",worldPosition,error);sync();}finally{working=false;}
 }
 private void loadFuel(ReactorCore.Rules rules){for(int i=0;i<4;i++){var r=inventory.getResource(i);if(r.isEmpty())continue;var next=ReactorCore.insertFuel(core,ReactorFuels.charge(r.toStack()),1,rules);if(next==null)continue;try(var tx=Transaction.openRoot()){if(inventory.extract(i,r,1,tx)==1){journal.updateSnapshots(tx);core=next;tx.commit();}}break;}}
 private boolean insertWaste(int count,TransactionContext tx){var r=ItemResource.of(ReactorFuels.spentStack(core.fuelId(),count));int left=count;for(int i=4;i<8&&left>0;i++)left-=inventory.insert(i,r,left,tx);return left==0;}
 private boolean wasteFits(){if(ReactorCore.wasteOffer(core).count()==0)return true;try(var tx=Transaction.openRoot()){return insertWaste(1,tx);}}
 private void solidify(){int n=Math.min(64,ReactorCore.wasteOffer(core).count());if(n==0)return;try(var tx=Transaction.openRoot()){var resource=ItemResource.of(ReactorFuels.spentStack(core.fuelId(),1));int left=n;for(int i=4;i<8&&left>0;i++)left-=inventory.insert(i,resource,left,tx);int accepted=n-left;if(accepted>0){journal.updateSnapshots(tx);core=ReactorCore.ejectWaste(core,accepted);tx.commit();}}}
 private ResourceHandler<ItemResource> items(boolean input){return new ResourceHandler<>(){private boolean valid(int i){return available()&&!working&&formed()&&i>=0&&i<8&&(input?i<4:i>=4);}public int size(){return 8;}public ItemResource getResource(int i){return valid(i)?inventory.getResource(i):ItemResource.EMPTY;}public long getAmountAsLong(int i){return valid(i)?inventory.getAmountAsLong(i):0;}public long getCapacityAsLong(int i,ItemResource r){return valid(i)?inventory.getCapacityAsLong(i,r):0;}public boolean isValid(int i,ItemResource r){return input&&valid(i)&&ReactorFuels.charge(r.toStack())!=null;}public int insert(int i,ItemResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);return isValid(i,r)?inventory.insert(i,r,n,tx):0;}public int extract(int i,ItemResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);return !input&&valid(i)?inventory.extract(i,r,n,tx):0;}};}
 private final ResourceHandler<ItemResource> input=items(true),output=items(false);
 @Override public ResourceHandler<ItemResource> itemPort(String role){return role.equals("fuel_input")?input:role.equals("waste_output")?output:null;}
 private final EnergyHandler energy=new EnergyHandler(){public long getAmountAsLong(){return available()&&formed()?core.energy():0;}public long getCapacityAsLong(){return rules().energyCapacity();}public int insert(int n,TransactionContext tx){TransferPreconditions.checkNonNegative(n);return 0;}public int extract(int n,TransactionContext tx){TransferPreconditions.checkNonNegative(n);if(!available()||working||!formed())return 0;int take=(int)Math.min(n,core.energy());if(take>0){journal.updateSnapshots(tx);core=ReactorCore.extractEnergy(core,take);}return take;}};
 @Override public EnergyHandler energyPort(String role){return role.equals("energy_out")?energy:null;}
 private FluidResource outletFluid(){return FluidResource.of(returned>0?Fluids.WATER:NuclearSteam.STEAM.get());}
 @Override public ResourceHandler<FluidResource> fluidPort(String role){if(!role.equals("coolant_in")&&!role.equals("coolant_out"))return null;boolean input=role.equals("coolant_in");return new ResourceHandler<>(){
  private boolean valid(int i){return i==0&&available()&&!working&&formed();}
  public int size(){return 1;}
  public FluidResource getResource(int i){return valid(i)&&(input?water:returned>0?returned:steam)>0?(input?FluidResource.of(Fluids.WATER):outletFluid()):FluidResource.EMPTY;}
  public long getAmountAsLong(int i){return valid(i)?input?water:returned>0?returned:steam:0;}
  public long getCapacityAsLong(int i,FluidResource r){return valid(i)?input?Math.max(water,fluidCapacity()):returned>0?8000:Math.max(steam,fluidCapacity()):0;}
  public boolean isValid(int i,FluidResource r){return valid(i)&&r.equals(input?FluidResource.of(Fluids.WATER):outletFluid());}
  public int insert(int i,FluidResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);if(!input||!isValid(i,r))return 0;int take=Math.max(0,Math.min(n,fluidCapacity()-water));if(take>0){journal.updateSnapshots(tx);water+=take;}return take;}
  public int extract(int i,FluidResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);if(input||!isValid(i,r))return 0;int take=Math.min(n,returned>0?returned:steam);if(take>0){journal.updateSnapshots(tx);if(returned>0)returned-=take;else steam-=take;}return take;}
 };}
 private void pushEnergy(){if(core.energy()==0||ports.isEmpty())return;for(int probe=0;probe<Math.min(16,ports.size());probe++){var p=ports.get(Math.floorMod(energyCursor++,ports.size()));if(!NuclearPortTile.active(level,p))continue;if(!NuclearReactor.role(level.getBlockState(p)).equals("energy_out"))continue;var face=NuclearPart.facing(level.getBlockState(p));var q=p.relative(face);if(contains(q)||!level.hasChunkAt(q))continue;var to=level.getCapability(Capabilities.Energy.BLOCK,q,face.getOpposite());if(to==null)continue;working=true;try(var tx=Transaction.openRoot()){int offered=(int)Math.min(Integer.MAX_VALUE,core.energy());int n=to.insert(offered,tx);if(n<0||n>offered)throw new IllegalStateException("Invalid energy insertion result");if(n>0){journal.updateSnapshots(tx);core=ReactorCore.extractEnergy(core,n);tx.commit();}}finally{working=false;}if(core.energy()==0)break;}}
 private void pushWaste(){if(ports.isEmpty())return;for(int probe=0;probe<Math.min(16,ports.size());probe++){var p=ports.get(Math.floorMod(wasteCursor++,ports.size()));if(!NuclearPortTile.active(level,p))continue;if(!NuclearReactor.role(level.getBlockState(p)).equals("waste_output"))continue;var face=NuclearPart.facing(level.getBlockState(p));var q=p.relative(face);if(contains(q)||!level.hasChunkAt(q))continue;var to=level.getCapability(Capabilities.Item.BLOCK,q,face.getOpposite());if(to==null)continue;working=true;try{for(int i=4;i<8;i++){var r=inventory.getResource(i);int offered=inventory.getAmountAsInt(i);if(offered==0)continue;try(var tx=Transaction.openRoot()){int n=to.insert(r,offered,tx);if(n<0||n>offered)throw new IllegalStateException("Invalid item insertion result");if(n>0&&inventory.extract(i,r,n,tx)==n){tx.commit();return;}}}}finally{working=false;}}}
 private void pushWater(){if((returned==0&&steam==0)||ports.isEmpty())return;for(int probe=0;probe<Math.min(16,ports.size());probe++){var p=ports.get(Math.floorMod(waterCursor++,ports.size()));if(!NuclearPortTile.active(level,p))continue;if(!NuclearReactor.role(level.getBlockState(p)).equals("coolant_out"))continue;var face=NuclearPart.facing(level.getBlockState(p));var q=p.relative(face);if(contains(q)||!level.hasChunkAt(q))continue;var to=level.getCapability(Capabilities.Fluid.BLOCK,q,face.getOpposite());if(to==null)continue;working=true;try(var tx=Transaction.openRoot()){int offered=returned>0?returned:steam;int n=to.insert(outletFluid(),offered,tx);if(n<0||n>offered)throw new IllegalStateException("Invalid vapor/coolant insertion result");if(n>0){journal.updateSnapshots(tx);if(returned>0)returned-=n;else steam-=n;tx.commit();}}finally{working=false;}if(returned==0&&steam==0)break;}}
 private int fuelIndex(){for(int i=0;i<ReactorFuels.ALL.size();i++)if(ReactorFuels.ALL.get(i).id().equals(core.fuelId()))return i;return 0;}
 private int fuelLevel(){long total=core.fuelUnits()+core.wasteUnits();return total==0?4:(int)Math.clamp(4*core.wasteUnits()/total,0,4);}
 private Direction outward(BlockPos p){var min=minimum();var max=maximum();if(p.getX()==min.getX())return Direction.WEST;if(p.getX()==max.getX())return Direction.EAST;if(p.getZ()==min.getZ())return Direction.NORTH;if(p.getZ()==max.getZ())return Direction.SOUTH;if(p.getY()==min.getY())return Direction.DOWN;return Direction.UP;}
 private void clearVisuals(){if(level==null||level.isClientSide())return;for(var p:parts)if(level.hasChunkAt(p)){var s=NuclearVisualUpdates.state(level,p);var next=s;if(s.hasProperty(NuclearPart.POWERED))next=next.setValue(NuclearPart.POWERED,false);if(s.hasProperty(NuclearPart.SHELL))next=next.setValue(NuclearPart.SHELL,0);if(s.hasProperty(NuclearPart.CONTROLLED))next=next.setValue(NuclearPart.CONTROLLED,false);if(next!=s)NuclearVisualUpdates.set(level,p,next);}running=false;}
 private void applyVisuals(){if(!formed){visualDirty=false;lastRunning=running;return;}var min=minimum();var max=maximum();for(var p:parts){if(!level.hasChunkAt(p))continue;var s=NuclearVisualUpdates.state(level,p);var next=s;String k=NuclearReactor.kind(s);if(next.hasProperty(NuclearPart.POWERED))next=next.setValue(NuclearPart.POWERED,running);if(next.hasProperty(NuclearPart.SHELL)){int mask=(p.getX()==min.getX()?1:0)|(p.getX()==max.getX()?2:0)|(p.getY()==min.getY()?4:0)|(p.getY()==max.getY()?8:0)|(p.getZ()==min.getZ()?16:0)|(p.getZ()==max.getZ()?32:0);next=next.setValue(NuclearPart.SHELL,mask);}if(NuclearReactor.PORTS.contains(k))next=next.setValue(BlockStateProperties.FACING,outward(p));if(k.equals("glass"))next=next.setValue(BlockStateProperties.FACING,outward(p));if(k.equals("fuel_assembly"))next=next.setValue(NuclearPart.FUEL,fuelIndex()).setValue(NuclearPart.FUEL_STATE,fuelLevel()).setValue(NuclearPart.CONTROLLED,true);next=NuclearPart.connected(next,level,p);if(next!=s)NuclearVisualUpdates.set(level,p,next);}
  // Second pass makes glass connections independent of which wall was visited first.
  for(var p:parts){var s=NuclearVisualUpdates.state(level,p);if(NuclearReactor.kind(s).equals("glass")){var n=NuclearPart.connected(s,level,p);if(n!=s)NuclearVisualUpdates.set(level,p,n);}}
  visualDirty=false;lastRunning=running;lastFuel=fuelIndex();lastLevel=fuelLevel();lastRods=core.rods();
 }
 private List<ItemStack> stacks(){var list=new ArrayList<ItemStack>();for(int i=0;i<8;i++)list.add(inventory.getResource(i).toStack(inventory.getAmountAsInt(i)));return list;}
 private Dynamic<?> payload(){if(recovery!=null)return recovery;var lookup=level==null?SeedDataSafety.builtinLookup():level.registryAccess();var ops=SeedDataSafety.ops(lookup);return new Dynamic<>(ops,Saved.CODEC.encodeStart(ops,new Saved(2,core,stacks(),width,depth,height,water,returned,steam,steamCredit,activeCooling)).getOrThrow());}
 private void readPayload(Dynamic<?> raw){var decoded=Saved.CODEC.parse(raw);if(decoded.error().isPresent()||decoded.result().orElseThrow().items.size()!=8){recovery=raw;return;}var s=decoded.result().orElseThrow();for(var stack:s.items)if(stack.getCount()>stack.getMaxStackSize()){recovery=raw;return;}core=s.core;width=s.width;depth=s.depth;height=s.height;water=s.water;returned=s.returned;steam=s.steam;steamCredit=s.credit;activeCooling=s.active;for(int i=0;i<8;i++){var stack=s.items.get(i);inventory.set(i,ItemResource.of(stack),stack.getCount());}recovery=null;dirty=true;scan=null;registerOwner();}
 public boolean safeToMove(){return com.neolena.essenceindustry.blueprint.BlueprintStorage.safeToMove(this)&&!working&&Transaction.getCurrentOpenedTransaction()==null&&InventoryRecovery.safe(payload());}
 public boolean hasContents(){return recovery!=null||!core.equals(ReactorCore.State.empty())||width!=5||depth!=5||height!=5||water>0||returned>0||steam>0||steamCredit>0||activeCooling||stacks().stream().anyMatch(s->!s.isEmpty());}
 @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("reactor",Codec.PASSTHROUGH,payload());}
 @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);if(in.getBooleanOr("visual",false)){com.neolena.essenceindustry.blueprint.ControllerPlacement.set(this,worldPosition.offset(in.getIntOr("ox",0),in.getIntOr("oy",0),in.getIntOr("oz",0)));width=in.getIntOr("width",5);depth=in.getIntOr("depth",5);height=in.getIntOr("height",5);formed=in.getBooleanOr("formed",false);dirty=false;running=in.getBooleanOr("running",false);fuelColumns=in.read("columns",BlockPos.CODEC.listOf()).orElse(List.of());controlColumns=in.read("controls",BlockPos.CODEC.listOf()).orElse(List.of());visualRods=Math.clamp(in.getIntOr("rods",50),0,100);return;}in.read("reactor",Codec.PASSTHROUGH).ifPresent(this::readPayload);}
 @Override protected void collectImplicitComponents(DataComponentMap.Builder data){super.collectImplicitComponents(data);InventoryRecovery.collect(data,"reactor",payload());}
 @Override protected void applyImplicitComponents(DataComponentGetter data){super.applyImplicitComponents(data);var raw=InventoryRecovery.read(data,"reactor",level==null?SeedDataSafety.builtinLookup():level.registryAccess());if(raw!=null)readPayload(raw);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){var t=new CompoundTag();t.putBoolean("visual",true);var o=layoutAnchor().subtract(worldPosition);t.putInt("ox",o.getX());t.putInt("oy",o.getY());t.putInt("oz",o.getZ());t.putInt("width",width);t.putInt("depth",depth);t.putInt("height",height);t.putBoolean("formed",formed());t.putBoolean("running",running);t.putInt("rods",core.rods());t.put("columns",BlockPos.CODEC.listOf().encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,fuelColumns).getOrThrow());t.put("controls",BlockPos.CODEC.listOf().encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,controlColumns).getOrThrow());return t;}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 private void sync(){if(level!=null&&!level.isClientSide()){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);nextSync=level.getGameTime()+20;}}
 @Override public void onLoad(){super.onLoad();unloaded=false;if(level!=null&&level.isClientSide())CLIENTS.computeIfAbsent(level,k->Collections.newSetFromMap(new WeakHashMap<>())).add(this);else{dirty=true;coldVisualReset=true;registerOwner();com.neolena.essenceindustry.blueprint.BlueprintAutomation.loaded(this);}}
 @Override public void onChunkUnloaded(){com.neolena.essenceindustry.blueprint.BlueprintAutomation.unloaded(this);unloaded=true;running=false;if(level!=null){NuclearOwners.unregister(level,worldPosition);var set=CLIENTS.get(level);if(set!=null)set.remove(this);}super.onChunkUnloaded();}
 @Override public void setRemoved(){com.neolena.essenceindustry.blueprint.BlueprintAutomation.unloaded(this);unloaded=true;if(level!=null){NuclearOwners.unregister(level,worldPosition);var set=CLIENTS.get(level);if(set!=null)set.remove(this);}super.setRemoved();}
 @Override public void preRemoveSideEffects(BlockPos p,BlockState s){com.neolena.essenceindustry.blueprint.BlueprintStorage.drop(this);clearVisuals();super.preRemoveSideEffects(p,s);}
}

