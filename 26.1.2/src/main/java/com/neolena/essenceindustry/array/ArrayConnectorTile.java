package com.neolena.essenceindustry.array;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.api.networking.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
/** One non-ticking ME endpoint per port/hatch, joined to the controller through an internal connection. */
public final class ArrayConnectorTile extends AENetworkedBlockEntity {
 private ArrayTile controller;private IGridConnection connection;
 private final net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> input=new net.neoforged.neoforge.transfer.ResourceHandler<>(){
  private net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> target(){var owner=controller();return owner!=null&&!isRemoved()&&level!=null&&level.hasChunkAt(worldPosition)&&level.getBlockEntity(worldPosition)==ArrayConnectorTile.this&&getBlockState().is(ProcessingArray.HATCH.get())?owner.rawInputHandler():null;}
  public int size(){return 1;}
  public net.neoforged.neoforge.transfer.item.ItemResource getResource(int slot){return net.neoforged.neoforge.transfer.item.ItemResource.EMPTY;}
  public long getAmountAsLong(int slot){return 0;}
  public long getCapacityAsLong(int slot,net.neoforged.neoforge.transfer.item.ItemResource r){var t=target();return t==null?0:t.getCapacityAsLong(slot,r);}
  public boolean isValid(int slot,net.neoforged.neoforge.transfer.item.ItemResource r){var t=target();return t!=null&&t.isValid(slot,r);}
  public int insert(int slot,net.neoforged.neoforge.transfer.item.ItemResource r,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(r,amount);var t=target();return t==null?0:t.insert(slot,r,amount,tx);}
  public int extract(int slot,net.neoforged.neoforge.transfer.item.ItemResource r,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(r,amount);return 0;}
 };
 public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> rawInput(){return input;}
 public ArrayConnectorTile(BlockPos pos,BlockState state){super(ProcessingArray.CONNECTOR_TILE.get(),pos,state);getMainNode().setIdlePowerUsage(0);}
 public ArrayTile controller(){return controller!=null&&controller.usable()&&controller.formed()?controller:null;}
 public void connect(ArrayTile owner){
  if(owner==controller&&connection!=null&&getMainNode().getNode()!=null&&getMainNode().getNode().getConnections().contains(connection))return;disconnect();controller=owner;
  if(owner==null||!owner.formed())return;
  var own=getMainNode().getNode();var target=owner.getMainNode().getNode();
  // Adjacent endpoints already have a native edge whose lifetime belongs to AE2.
  if(own!=null&&target!=null&&own.getConnections().stream().noneMatch(edge->edge.getOtherSide(own)==target))connection=GridHelper.createConnection(own,target);
 }
 public void disconnect(){if(connection!=null){connection.destroy();connection=null;}controller=null;}
 @Override public void onReady(){super.onReady();connect(ArrayTile.owner(level,worldPosition));}
 @Override public void setRemoved(){disconnect();super.setRemoved();}
 @Override public void onChunkUnloaded(){disconnect();super.onChunkUnloaded();}
}
