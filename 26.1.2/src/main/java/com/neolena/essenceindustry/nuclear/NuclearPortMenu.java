package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class NuclearPortMenu extends AbstractContainerMenu {
 private final Inventory inventory;private final BlockPos pos;private final ContainerData data=new SimpleContainerData(4);
 public NuclearPortMenu(int id,Inventory inv,RegistryFriendlyByteBuf b){this(id,inv,b.readBlockPos());}
 public NuclearPortMenu(int id,Inventory inv,BlockPos pos){super(NuclearReactor.PORT_MENU.get(),id);inventory=inv;this.pos=pos;data.set(0,-1);refresh();addDataSlots(data);}
 private NuclearPortTile tile(){return inventory.player.level().getBlockEntity(pos) instanceof NuclearPortTile t?t:null;}
 private void refresh(){if(!inventory.player.level().isClientSide()&&tile()!=null){data.set(0,tile().type());data.set(1,tile().outputMode()?1:0);data.set(2,tile().connected()?1:0);data.set(3,tile().active()?1:0);}}
 public boolean activeTransfer(){return data.get(3)==1;}
 public int type(){return data.get(0);}public boolean output(){return data.get(1)==1;}public boolean connected(){return data.get(2)==1;}
 @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.mayBuild()&&p.level().isLoaded(pos)&&tile()!=null&&p.distanceToSqr(pos.getCenter())<=64;}
 @Override public boolean clickMenuButton(Player p,int id){if(!stillValid(p)||id<0||id>2||id<2&&tile().type()==2)return false;if(id==2)tile().toggleActive();else tile().mode(id==1);broadcastChanges();return true;}
 @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
