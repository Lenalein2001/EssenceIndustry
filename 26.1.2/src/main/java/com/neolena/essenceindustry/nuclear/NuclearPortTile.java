package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
/** Stable, non-ticking forwarding capabilities resolve ownership on every transfer. */
public final class NuclearPortTile extends BlockEntity {
 private boolean active=true;private int sourceCursor;
 public boolean active(){return active;}
 public void toggleActive(){if(level!=null&&!level.isClientSide()){active=!active;setChanged();}}
 public static boolean active(net.minecraft.world.level.Level level,BlockPos pos){return !(level.getBlockEntity(pos) instanceof NuclearPortTile t)||t.active();}
 @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out){super.saveAdditional(out);if(!active)out.putBoolean("activeTransfer",false);}
 @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in){super.loadAdditional(in);active=in.getBooleanOr("activeTransfer",true);}
 /** Bounded polling is owned by the multiblock, so ports need no server ticker. */
 public static void pull(net.minecraft.world.level.Level level,java.util.List<BlockPos> ports){
  if(ports.isEmpty()||level.getGameTime()%5!=0)return;
  int start=(int)Math.floorMod(level.getGameTime()/5*16,ports.size());
  pullBatch(level,ports,start);
 }
 /** Scheduled hosts own their polling interval; resume the bounded cursor on every poll. */
 public static int pullBatch(net.minecraft.world.level.Level level,java.util.List<BlockPos> ports,int cursor){
  if(ports.isEmpty())return 0;
  int start=Math.floorMod(cursor,ports.size());
  for(int i=0;i<Math.min(16,ports.size());i++){
   var pos=ports.get((start+i)%ports.size());
   if(level.getBlockEntity(pos) instanceof NuclearPortTile port)port.pullFromNeighbor();
  }
  return (start+Math.min(16,ports.size()))%ports.size();
 }
 public void pullFromNeighbor(){
  if(level==null||level.isClientSide()||!active||outputMode())return;
  var owner=host();var face=NuclearPart.facing(getBlockState());var neighbor=worldPosition.relative(face);
  if(owner==null||owner.contains(neighbor)||!level.hasChunkAt(neighbor))return;
  if(type()==0){var source=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,neighbor,face.getOpposite());if(source!=null)move(source,items,64);}
  else if(type()==2){var source=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK,neighbor,face.getOpposite());if(source!=null){int offered=(int)Math.min(Integer.MAX_VALUE,Math.min(source.getAmountAsLong(),Math.max(0,energy.getCapacityAsLong()-energy.getAmountAsLong())));if(offered>0){try(var simulation=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){int available=source.extract(offered,simulation);if(available<0||available>offered)throw new IllegalStateException("Invalid simulated energy extraction");offered=available;}}if(offered>0)try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){int accepted=energy.insert(offered,tx);if(accepted<0||accepted>offered)throw new IllegalStateException("Invalid energy input count");if(accepted>0){int extracted=source.extract(accepted,tx);if(extracted<0||extracted>accepted)throw new IllegalStateException("Invalid energy extraction count");if(extracted==accepted)tx.commit();}}}}
  else if(type()==1){var source=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK,neighbor,face.getOpposite());if(source!=null)move(source,fluids,Integer.MAX_VALUE);}
 }

 private <R extends net.neoforged.neoforge.transfer.resource.Resource> void move(ResourceHandler<R> source,ResourceHandler<R> target,int maximum){
  for(int probe=0;probe<Math.min(source.size(),64);probe++){int slot=Math.floorMod(sourceCursor++,source.size());
   var resource=source.getResource(slot);int offered=(int)Math.min(maximum,source.getAmountAsLong(slot));if(resource.isEmpty()||offered<=0)continue;
   // Respect extraction limits before reserving destination space. The probe rolls back.
   try(var simulation=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){
    int available=source.extract(slot,resource,offered,simulation);
    if(available<0||available>offered)throw new IllegalStateException("Invalid simulated active port extraction");
    offered=available;
   }
   if(offered==0)continue;
   try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){
    int accepted=target.insert(resource,offered,tx);if(accepted<0||accepted>offered)throw new IllegalStateException("Invalid active port insert count");
    if(accepted>0){int extracted=source.extract(slot,resource,accepted,tx);if(extracted<0||extracted>accepted)throw new IllegalStateException("Invalid active port extract count");if(extracted==accepted){tx.commit();return;}}
   }
  }
 }
 public NuclearPortTile(BlockPos p,BlockState s){super(NuclearReactor.PORT_TILE.get(),p,s);}
 private NuclearHost host(){if(level==null||isRemoved()||!level.hasChunkAt(worldPosition)||level.getBlockEntity(worldPosition)!=this)return null;var h=NuclearOwners.at(level,worldPosition);return h!=null&&h.formed()?h:null;}
 private String role(){return NuclearReactor.role(getBlockState());}
 public int type(){return switch(NuclearReactor.baseKind(NuclearReactor.kind(getBlockState()))){case "fuel_input"->0;case "coolant_in"->1;default->2;};}
 public boolean outputMode(){return type()==2?!(NuclearOwners.at(level,worldPosition) instanceof FuelProcessorTile):role().equals("waste_output")||role().equals("coolant_out");}
 public boolean connected(){return host()!=null;}
 public void mode(boolean output){if(level==null||level.isClientSide()||isRemoved()||type()==2||net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction()!=null)return;var state=NuclearReactor.portState(type()==0?(output?"waste_output":"fuel_input"):(output?"coolant_out":"coolant_in")).setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,NuclearPart.facing(getBlockState())).setValue(NuclearPart.POWERED,getBlockState().getValue(NuclearPart.POWERED));level.setBlock(worldPosition,state,net.minecraft.world.level.block.Block.UPDATE_ALL);if(level.getBlockEntity(worldPosition) instanceof NuclearPortTile replacement){replacement.active=active;replacement.setChanged();}level.invalidateCapabilities(worldPosition);NuclearOwners.changed(level,worldPosition);}
 public void open(net.minecraft.world.entity.player.Player player){if(player instanceof net.minecraft.server.level.ServerPlayer p&&p.mayBuild())p.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,who)->new NuclearPortMenu(id,inv,worldPosition),net.minecraft.network.chat.Component.translatable("gui.essenceindustry.port.title")),b->b.writeBlockPos(worldPosition));}
 public boolean side(Direction side){return side==null||side==NuclearPart.facing(getBlockState());}
 public final ResourceHandler<ItemResource> items=new Forward<ItemResource>(ItemResource.EMPTY){ResourceHandler<ItemResource> target(){var h=host();return h==null?null:h.itemPort(role());}};
 public final ResourceHandler<FluidResource> fluids=new Forward<FluidResource>(FluidResource.EMPTY){ResourceHandler<FluidResource> target(){var h=host();return h==null?null:h.fluidPort(role());}};
 public final EnergyHandler energy=new EnergyHandler(){private EnergyHandler target(){var h=host();return h==null?null:h.energyPort(role());}public long getAmountAsLong(){var t=target();return t==null?0:t.getAmountAsLong();}public long getCapacityAsLong(){var t=target();return t==null?0:t.getCapacityAsLong();}public int insert(int n,TransactionContext tx){TransferPreconditions.checkNonNegative(n);var t=target();return t==null?0:t.insert(n,tx);}public int extract(int n,TransactionContext tx){TransferPreconditions.checkNonNegative(n);var t=target();return t==null?0:t.extract(n,tx);}};
 private abstract class Forward<R extends net.neoforged.neoforge.transfer.resource.Resource> implements ResourceHandler<R>{final R empty;Forward(R empty){this.empty=empty;}abstract ResourceHandler<R> target();public int size(){var t=target();return t==null?0:t.size();}public R getResource(int i){var t=target();return t==null||i<0||i>=t.size()?empty:t.getResource(i);}public long getAmountAsLong(int i){var t=target();return t==null||i<0||i>=t.size()?0:t.getAmountAsLong(i);}public long getCapacityAsLong(int i,R r){var t=target();return t==null||i<0||i>=t.size()?0:t.getCapacityAsLong(i,r);}public boolean isValid(int i,R r){var t=target();return t!=null&&i>=0&&i<t.size()&&t.isValid(i,r);}public int insert(int i,R r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);var t=target();return t==null||i<0||i>=t.size()?0:t.insert(i,r,n,tx);}public int extract(int i,R r,int n,TransactionContext tx){TransferPreconditions.checkNonEmptyNonNegative(r,n);var t=target();return t==null||i<0||i>=t.size()?0:t.extract(i,r,n,tx);}}
}
