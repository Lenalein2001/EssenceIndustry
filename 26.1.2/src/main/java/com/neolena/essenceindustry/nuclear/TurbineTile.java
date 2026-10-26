package com.neolena.essenceindustry.nuclear;
import java.util.*;
import com.mojang.serialization.Codec;
import com.neolena.essenceindustry.blueprint.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.*;
/** The complete turbine has one ledger, one simulation tick and one synchronized rotor phase. */
public final class TurbineTile extends BlockEntity implements NuclearHost {
 private TurbineCore.State core=TurbineCore.State.empty();
 private BlueprintSettings layout=BlueprintSettings.DEFAULT.type(5,5,9,5);
 private TurbineCore.Rules rules=new TurbineCore.Rules(2,5,16,16000);
 private boolean enabled,engaged=true,formed,dirty=true,unloaded,working,recovery;
 private boolean lit;private int target=1800,throttle=100,flow;private long generation,audit;private double phase,rpm;private long phaseTick;
 private String issue="Build a shell and rotor; open Construction";
 private List<BlockPos> parts=List.of(),ports=List.of();
 private static final Map<Level,Set<TurbineTile>> CLIENTS=new WeakHashMap<>();
 private final SnapshotJournal<TurbineCore.State> journal=new SnapshotJournal<>(){protected TurbineCore.State createSnapshot(){return core;}protected void revertToSnapshot(TurbineCore.State s){core=s;}protected void onRootCommit(TurbineCore.State s){setChanged();}};
 public TurbineTile(BlockPos p,BlockState s){super(SteamTurbine.TILE.get(),p,s);}
 public BlueprintSettings layout(){return layout;}public TurbineCore.State core(){return core;}public TurbineCore.Rules rules(){return rules;}
 public double rpm(){return rpm;}public long generation(){return generation;}public int flow(){return flow;}public boolean enabled(){return enabled;}public boolean engaged(){return engaged;}public int target(){return target;}public int throttle(){return throttle;}public String issue(){return issue;}
 public int width(){return layout.width();}public int height(){return layout.height();}public int depth(){return layout.depth();}
 public Direction facing(){return NuclearPart.facing(getBlockState());}public BlockPos anchor(){return ControllerPlacement.anchor(this);}
 public BlockPos position(int x,int y,int z){return NuclearLayout.pos(anchor(),facing(),width(),x,y,z);}
 public BlockPos minimum(){return NuclearLayout.min(position(0,0,0),position(width()-1,height()-1,depth()-1));}public BlockPos maximum(){return NuclearLayout.max(position(0,0,0),position(width()-1,height()-1,depth()-1));}
 public boolean available(){return !recovery&&!unloaded&&!isRemoved()&&level instanceof ServerLevel&&level.hasChunkAt(worldPosition)&&level.getBlockEntity(worldPosition)==this;}
 public boolean formed(){return formed&&!dirty&&!unloaded&&!recovery;}
 public boolean contains(BlockPos p){var a=minimum();var b=maximum();return p.getX()>=a.getX()&&p.getX()<=b.getX()&&p.getY()>=a.getY()&&p.getY()<=b.getY()&&p.getZ()>=a.getZ()&&p.getZ()<=b.getZ();}
 public void partChanged(BlockPos p){if(contains(p))dirty=true;}
 private void register(){if(level!=null&&!level.isClientSide())NuclearOwners.register(level,worldPosition,minimum(),maximum());}
 public boolean configure(BlueprintSettings s){if(!available()||enabled||working||core.kinetic()>.01||s.kind()!=5)return false;try{TurbineLayout.create(s,facing());}catch(IllegalArgumentException ex){return false;}clearVisuals();layout=s;dirty=true;formed=false;register();setChanged();sync();return true;}
 private boolean loaded(){var a=minimum();var b=maximum();for(int x=a.getX()>>4;x<=b.getX()>>4;x++)for(int z=a.getZ()>>4;z<=b.getZ()>>4;z++)if(!level.hasChunkAt(new BlockPos(x<<4,worldPosition.getY(),z<<4)))return false;return true;}
 private Direction outward(BlockPos p){var a=minimum();var b=maximum();return p.getY()==a.getY()?Direction.DOWN:p.getY()==b.getY()?Direction.UP:p.getX()==a.getX()?Direction.WEST:p.getX()==b.getX()?Direction.EAST:p.getZ()==a.getZ()?Direction.NORTH:Direction.SOUTH;}
 private void validate(){
  dirty=false;audit=level.getGameTime()+100;var found=new ArrayList<BlockPos>();var endpoints=new ArrayList<BlockPos>();int coils=0;long rating=0;int inlet=0,outlet=0,power=0,controllers=0;String error="";
  if(NuclearOwners.conflict(level,worldPosition,minimum(),maximum()))error="Overlapping multiblock";
  for(var c:TurbineLayout.create(layout,facing())){var p=position(c.local().getX(),c.local().getY(),c.local().getZ());var s=level.getBlockState(p);String k=SteamTurbine.kind(s),n=NuclearReactor.kind(s),r=NuclearReactor.role(s);int x=c.local().getX(),y=c.local().getY(),z=c.local().getZ();boolean shell=x==0||x==width()-1||y==0||y==height()-1||z==0||z==depth()-1;
   boolean okay;
   if(shell){okay=Set.of("casing","pressure_panel","frame","glass","roof_window").contains(n)||NuclearReactor.PORTS.contains(n)||k.equals("controller")||k.equals("condenser");if(k.equals("controller"))controllers++;if(r.equals("coolant_in"))inlet++;if(r.equals("coolant_out"))outlet++;if(r.equals("energy_out"))power++;if(NuclearReactor.PORTS.contains(n))endpoints.add(p);}
   else{String expected=SteamTurbine.kind(c.state());if(expected.startsWith("coil_")){okay=k.startsWith("coil_");if(okay){int index=Arrays.asList(TurbineCore.COILS).indexOf(k.substring(5));okay=index>=0;if(okay){rating+=TurbineCore.COIL_FE[index];coils++;}}}else okay=s.getBlock()==c.state().getBlock()||Set.of("casing","frame","pressure_panel").contains(n)&&NuclearReactor.kind(c.state()).equals("casing");}
   if(!okay&&error.isEmpty())error="Check "+p.getX()+", "+p.getY()+", "+p.getZ()+": "+c.state().getBlock().getName().getString();if(!s.isAir())found.add(p);
  }
  if(error.isEmpty()&&(inlet<1||power<1||controllers!=1))error="Need one controller, steam input and energy port";
  if(!error.isEmpty()){if(formed)clearVisuals();formed=false;issue=error;sync();return;}
  parts=List.copyOf(found);ports=List.copyOf(endpoints);rules=new TurbineCore.Rules(layout.blades(),width(),coils,rating);formed=true;issue="Ready";visuals();sync();
 }
 public void control(int action){if(!available()||working||Transaction.getCurrentOpenedTransaction()!=null)return;switch(action){case 0->enabled=!enabled;case 1->engaged=!engaged;case 2->target=Math.max(300,target-300);case 3->target=Math.min(TurbineCore.MAX_RPM,target+300);case 4->throttle=Math.max(0,throttle-10);case 5->throttle=Math.min(100,throttle+10);default->{return;}}setChanged();sync();}
 public void tick(){if(!available()||Transaction.getCurrentOpenedTransaction()!=null)return;
  if(!loaded()){dirty=true;generation=0;flow=0;issue="Waiting for turbine chunks";if(level.getGameTime()%5==0)sync();return;}
  if(dirty&&!formed)ControllerPlacement.discoverWall(this,facing(),width(),height(),width()/2,s->Set.of("casing","pressure_panel","frame","glass","roof_window").contains(NuclearReactor.kind(s))||NuclearReactor.PORTS.contains(NuclearReactor.kind(s))||SteamTurbine.kind(s).equals("controller")||SteamTurbine.kind(s).equals("condenser"));register();
  if(dirty||level.getGameTime()>=audit)validate();
  var result=TurbineCore.tick(core,rules,formed(),enabled&&!level.hasNeighborSignal(worldPosition),engaged,target,throttle);core=result.state();rpm=result.rpm();generation=result.generated();flow=result.consumed();phase=(phase+TurbineCore.degreesPerTick(rpm))%360;phaseTick=level.getGameTime();setChanged();
  if(formed()){issue=flow>0?"Running":rpm>1?"Coasting":!enabled?"Steam valve closed":core.steam()==0?"Waiting for steam":core.energy()==TurbineCore.CAPACITY?"Energy buffer full":"At target speed";NuclearPortTile.pull(level,ports);push();}
  if(lit!=(formed()&&rpm>1)){lit=formed()&&rpm>1;for(var p:parts){String k=SteamTurbine.kind(level.getBlockState(p));if(k.equals("light")||k.equals("controller")){var s=NuclearVisualUpdates.state(level,p);NuclearVisualUpdates.set(level,p,s.setValue(NuclearPart.POWERED,lit));}}}if(level.getGameTime()%5==0)sync();
 }
 private void replace(double k,long e,int s,int w,TransactionContext tx){journal.updateSnapshots(tx);core=new TurbineCore.State(k,e,s,w);}
 public ResourceHandler<ItemResource> itemPort(String role){return null;}
 public EnergyHandler energyPort(String role){return role.equals("energy_out")?energy:null;}
 private boolean transfer(){return available()&&formed()&&!working;}
 private final EnergyHandler energy=new EnergyHandler(){public long getAmountAsLong(){return transfer()?core.energy():0;}public long getCapacityAsLong(){return TurbineCore.CAPACITY;}public int insert(int n,TransactionContext tx){TransferPreconditions.checkNonNegative(n);return 0;}public int extract(int n,TransactionContext tx){TransferPreconditions.checkNonNegative(n);int take=transfer()?(int)Math.min(n,core.energy()):0;if(take>0)replace(core.kinetic(),core.energy()-take,core.steam(),core.water(),tx);return take;}};
 public ResourceHandler<FluidResource> fluidPort(String role){if(!role.equals("coolant_in")&&!role.equals("coolant_out"))return null;boolean input=role.equals("coolant_in");return new ResourceHandler<>(){
  private FluidResource fluid(){return FluidResource.of(input?NuclearSteam.STEAM.get():Fluids.WATER);}private boolean valid(int i){return i==0&&transfer();}
  public int size(){return 1;}public FluidResource getResource(int i){return getAmountAsLong(i)>0?fluid():FluidResource.EMPTY;}public long getAmountAsLong(int i){return valid(i)?input?core.steam():core.water():0;}public long getCapacityAsLong(int i,FluidResource r){return valid(i)?TurbineCore.TANK:0;}public boolean isValid(int i,FluidResource r){return valid(i)&&r.equals(fluid());}
  public int insert(int i,FluidResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);int take=input&&isValid(i,r)?Math.min(n,TurbineCore.TANK-core.steam()):0;if(take>0)replace(core.kinetic(),core.energy(),core.steam()+take,core.water(),tx);return take;}
  public int extract(int i,FluidResource r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);int take=!input&&isValid(i,r)?Math.min(n,core.water()):0;if(take>0)replace(core.kinetic(),core.energy(),core.steam(),core.water()-take,tx);return take;}
 };}
 private void push(){working=true;try{for(var p:ports){if(!NuclearPortTile.active(level,p))continue;var state=level.getBlockState(p);String role=NuclearReactor.role(state);var face=NuclearPart.facing(state);var q=p.relative(face);if(contains(q)||!level.hasChunkAt(q))continue;
   if(role.equals("energy_out")&&core.energy()>0){var to=level.getCapability(Capabilities.Energy.BLOCK,q,face.getOpposite());if(to!=null)try(var tx=Transaction.openRoot()){int offered=(int)Math.min(Integer.MAX_VALUE,core.energy()),n=to.insert(offered,tx);if(n<0||n>offered)throw new IllegalStateException("Invalid turbine energy transfer");replace(core.kinetic(),core.energy()-n,core.steam(),core.water(),tx);tx.commit();}}
   if(role.equals("coolant_out")&&core.water()>0){var to=level.getCapability(Capabilities.Fluid.BLOCK,q,face.getOpposite());if(to!=null)try(var tx=Transaction.openRoot()){int n=to.insert(FluidResource.of(Fluids.WATER),core.water(),tx);if(n<0||n>core.water())throw new IllegalStateException("Invalid turbine condensate transfer");replace(core.kinetic(),core.energy(),core.steam(),core.water()-n,tx);tx.commit();}}
  }}catch(RuntimeException ex){enabled=false;var envelope=new CompoundTag();envelope.put("unresolvedTransfer",payload().convert(NbtOps.INSTANCE).getValue().copy());retained=new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE,envelope);recovery=true;setChanged();issue="Transfer failed; contents preserved";sync();org.slf4j.LoggerFactory.getLogger(getClass()).error("Turbine transfer at {} quarantined; ledger retained without automatic retry",worldPosition,ex);}finally{working=false;}}
 private void visuals(){var a=minimum();var b=maximum();for(var p:parts){var s=NuclearVisualUpdates.state(level,p);var n=s;if(n.hasProperty(NuclearPart.SHELL))n=n.setValue(NuclearPart.SHELL,(p.getX()==a.getX()?1:0)|(p.getX()==b.getX()?2:0)|(p.getY()==a.getY()?4:0)|(p.getY()==b.getY()?8:0)|(p.getZ()==a.getZ()?16:0)|(p.getZ()==b.getZ()?32:0));if(NuclearReactor.PORTS.contains(NuclearReactor.kind(s)))n=n.setValue(BlockStateProperties.FACING,outward(p));if(NuclearReactor.kind(s).equals("glass"))n=n.setValue(BlockStateProperties.FACING,outward(p));if(!SteamTurbine.kind(s).isEmpty()&&n.hasProperty(BlockStateProperties.HORIZONTAL_FACING))n=n.setValue(BlockStateProperties.HORIZONTAL_FACING,facing());if(n.hasProperty(NuclearPart.POWERED))n=n.setValue(NuclearPart.POWERED,SteamTurbine.kind(s).equals("light")||SteamTurbine.kind(s).equals("controller")?rpm>1:true);if(n.hasProperty(SteamTurbine.Part.VERTICAL))n=n.setValue(SteamTurbine.Part.VERTICAL,layout.vertical());n=NuclearPart.connected(n,level,p);if(n!=s)NuclearVisualUpdates.set(level,p,n);}for(var p:parts){var s=NuclearVisualUpdates.state(level,p);if(NuclearReactor.kind(s).equals("glass"))NuclearVisualUpdates.set(level,p,NuclearPart.connected(s,level,p));}}
 private void clearVisuals(){if(level==null||level.isClientSide())return;for(var p:parts)if(level.hasChunkAt(p)){var s=NuclearVisualUpdates.state(level,p);var n=s;if(n.hasProperty(NuclearPart.SHELL))n=n.setValue(NuclearPart.SHELL,0);if(n.hasProperty(NuclearPart.POWERED))n=n.setValue(NuclearPart.POWERED,false);if(n!=s)NuclearVisualUpdates.set(level,p,n);}}
 public void open(Player player){if(player instanceof ServerPlayer p)p.openMenu(new SimpleMenuProvider((id,inv,who)->new TurbineMenu(id,inv,worldPosition),Component.translatable("block.essenceindustry.turbine_controller")),b->b.writeBlockPos(worldPosition));}
 public static List<TurbineTile> clients(Level l){return List.copyOf(CLIENTS.getOrDefault(l,Set.of()));}
 public double angle(double time){return (phase+Math.clamp(time-phaseTick,0,6)*TurbineCore.degreesPerTick(rpm))%360;}
 private record Saved(BlueprintSettings layout,TurbineCore.State core,boolean enabled,boolean engaged,int target,int throttle,double phase){
  static final Codec<Saved> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.create(i->i.group(BlueprintSettings.CODEC.fieldOf("layout").forGetter(Saved::layout),TurbineCore.State.CODEC.fieldOf("core").forGetter(Saved::core),Codec.BOOL.fieldOf("enabled").forGetter(Saved::enabled),Codec.BOOL.fieldOf("engaged").forGetter(Saved::engaged),Codec.intRange(300,2700).fieldOf("target").forGetter(Saved::target),Codec.intRange(0,100).fieldOf("throttle").forGetter(Saved::throttle),Codec.doubleRange(0,360).fieldOf("phase").forGetter(Saved::phase)).apply(i,Saved::new));
 }
 private com.mojang.serialization.Dynamic<?> retained;
 private com.mojang.serialization.Dynamic<?> payload(){if(retained!=null)return retained;var ops=com.neolena.essenceindustry.library.SeedDataSafety.ops(level==null?com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup():level.registryAccess());return new com.mojang.serialization.Dynamic<>(ops,Saved.CODEC.encodeStart(ops,new Saved(layout,core,enabled,engaged,target,throttle,phase)).getOrThrow());}
 private void readPayload(com.mojang.serialization.Dynamic<?> raw){try{var saved=Saved.CODEC.parse(raw).getOrThrow();TurbineLayout.create(saved.layout,facing());layout=saved.layout;core=saved.core;enabled=saved.enabled;engaged=saved.engaged;target=saved.target;throttle=saved.throttle;phase=saved.phase;retained=null;recovery=false;dirty=true;}catch(RuntimeException ex){retained=raw;recovery=true;issue="Stored data needs recovery; contents preserved";}}
 public boolean safeToMove(){return com.neolena.essenceindustry.blueprint.BlueprintStorage.safeToMove(this)&&!working&&Transaction.getCurrentOpenedTransaction()==null&&com.neolena.essenceindustry.item.InventoryRecovery.safe(payload());}
 public boolean hasContents(){return retained!=null||core.kinetic()>0||core.energy()>0||core.steam()>0||core.water()>0;}
 @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("turbine",Codec.PASSTHROUGH,payload());}
 @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);if(in.getBooleanOr("visual",false)){layout=in.read("layout",BlueprintSettings.CODEC).orElse(layout);formed=in.getBooleanOr("formed",false);dirty=false;rpm=in.getDoubleOr("rpm",0);phase=in.getDoubleOr("phase",0);phaseTick=in.getLongOr("phaseTick",0);flow=in.getIntOr("flow",0);ControllerPlacement.set(this,worldPosition.offset(in.getIntOr("ox",0),in.getIntOr("oy",0),in.getIntOr("oz",0)));return;}in.read("turbine",Codec.PASSTHROUGH).ifPresent(this::readPayload);}
 @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder data){super.collectImplicitComponents(data);com.neolena.essenceindustry.item.InventoryRecovery.collect(data,"turbine",payload());}
 @Override protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter data){super.applyImplicitComponents(data);var raw=com.neolena.essenceindustry.item.InventoryRecovery.read(data,"turbine",level==null?com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup():level.registryAccess());if(raw!=null)readPayload(raw);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){var tag=new CompoundTag();tag.putBoolean("visual",true);tag.put("layout",BlueprintSettings.CODEC.encodeStart(NbtOps.INSTANCE,layout).getOrThrow());tag.putBoolean("formed",formed());tag.putDouble("rpm",rpm);tag.putInt("flow",flow);tag.putDouble("phase",phase);tag.putLong("phaseTick",phaseTick);var offset=anchor().subtract(worldPosition);tag.putInt("ox",offset.getX());tag.putInt("oy",offset.getY());tag.putInt("oz",offset.getZ());return tag;}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 private void sync(){if(level!=null&&!level.isClientSide())level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
 @Override public void onLoad(){super.onLoad();unloaded=false;if(level.isClientSide())CLIENTS.computeIfAbsent(level,k->Collections.newSetFromMap(new WeakHashMap<>())).add(this);else{dirty=true;register();BlueprintAutomation.loaded(this);}}
 private void unload(){BlueprintAutomation.unloaded(this);unloaded=true;if(level!=null){NuclearOwners.unregister(level,worldPosition);var set=CLIENTS.get(level);if(set!=null)set.remove(this);}}
 @Override public void onChunkUnloaded(){unload();super.onChunkUnloaded();}
 @Override public void setRemoved(){unload();super.setRemoved();}
 @Override public void preRemoveSideEffects(BlockPos p,BlockState s){BlueprintStorage.drop(this);clearVisuals();super.preRemoveSideEffects(p,s);}
}
