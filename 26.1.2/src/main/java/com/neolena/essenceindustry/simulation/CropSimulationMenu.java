package com.neolena.essenceindustry.simulation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class CropSimulationMenu extends AbstractContainerMenu {
 private final CropSimulationTile tile;private final ContainerLevelAccess access;private final int[] data=new int[10];
 public CropSimulationMenu(int id,Inventory p,RegistryFriendlyByteBuf b){this(id,p,b.readBlockPos(),null);}
 public CropSimulationMenu(int id,Inventory p,CropSimulationTile tile){this(id,p,tile.getBlockPos(),tile);}
 private CropSimulationMenu(int id,Inventory player,BlockPos pos,CropSimulationTile tile){super(CropSimulation.MENU.get(),id);this.tile=tile;access=ContainerLevelAccess.create(player.player.level(),pos);
  var inv=tile==null?new SimulationStore(()->{}):tile.inventory();addSlot(new SimulationSlot(inv,0,30,43,tile));addSlot(new SimulationSlot(inv,SimulationStore.SPEED,90,43,tile));addSlot(new SimulationSlot(inv,SimulationStore.YIELD,150,43,tile));addSlot(new SimulationSlot(inv,SimulationStore.FARMLAND,210,43,tile));addSlot(new SimulationSlot(inv,SimulationStore.CRUX,270,43,tile));
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(player,9+y*9+x,79+x*18,151+y*18));for(int x=0;x<9;x++)addSlot(new Slot(player,x,79+x*18,209));
  for(int k=0;k<data.length;k++){final int i=k;addDataSlot(new DataSlot(){public int get(){return tile==null?data[i]:switch(i){case 0->tile.inventory().state().count()&65535;case 1->tile.inventory().state().count()>>>16;case 2->tile.batchSize();case 3->tile.status();case 4->tile.meEnabled()?1:0;case 5->tile.meStatus();case 6->tile.speedMultiplier()&65535;case 7->tile.speedMultiplier()>>>16;case 8->tile.yieldMultiplier()&65535;default->tile.yieldMultiplier()>>>16;};}public void set(int v){data[i]=v&65535;}});}
 }
 public int count(){return data[0]|data[1]<<16;}public int batch(){return data[2];}public int status(){return data[3];}public boolean me(){return data[4]!=0;}public int meStatus(){return data[5];}public int speed(){return data[6]|data[7]<<16;}public int yield(){return data[8]|data[9]<<16;}
 @Override public boolean stillValid(Player p){return (tile==null || tile.available()) && stillValid(access,p,CropSimulation.BLOCK.get());}
 @Override public boolean clickMenuButton(Player p,int button){if(button!=0 || tile==null || !p.mayBuild() || !stillValid(p))return false;tile.toggleMe();return true;}
 @Override public void removed(Player p){super.removed(p);if(tile!=null)for(var slot:slots)if(slot instanceof SimulationSlot s)tile.unwatch(s);}
 @Override public void clicked(int s,int b,ContainerInput input,Player p){if(stillValid(p) && p.mayBuild())super.clicked(s,b,input,p);}
 @Override public ItemStack quickMoveStack(Player p,int i){if(!stillValid(p) || !p.mayBuild() || i<0 || i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var before=s.copy();if(!(i<5?moveItemStackTo(s,5,slots.size(),true):s.is(CropSimulation.SPEED.get())?moveItemStackTo(s,1,2,false):s.is(CropSimulation.YIELD.get())?moveItemStackTo(s,2,3,false):SimulationStore.isFarmland(net.neoforged.neoforge.transfer.item.ItemResource.of(s))?moveItemStackTo(s,3,4,false):SimulationStore.isCrux(net.neoforged.neoforge.transfer.item.ItemResource.of(s))?moveItemStackTo(s,4,5,false):moveItemStackTo(s,0,1,false)))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return before;}
}
