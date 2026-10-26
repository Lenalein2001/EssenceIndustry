package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class TurbineMenu extends AbstractContainerMenu {
 private final Inventory inventory;private final BlockPos pos;private final TurbineTile original;
 private final ContainerData data=new SimpleContainerData(224);
 public TurbineMenu(int id,Inventory inv,RegistryFriendlyByteBuf b){this(id,inv,b.readBlockPos());}
 public TurbineMenu(int id,Inventory inv,BlockPos pos){super(SteamTurbine.MENU.get(),id);inventory=inv;this.pos=pos;original=tile();addDataSlots(data);refresh();}
 private TurbineTile tile(){return inventory.player.level().getBlockEntity(pos) instanceof TurbineTile t?t:null;}
 private void value(int i,long n){for(int p=0;p<4;p++)data.set(i*4+p,(int)(n>>>(p*16))&65535);}
 public long value(int i){long n=0;for(int p=0;p<4;p++)n|=(data.get(i*4+p)&65535L)<<(p*16);return n;}
 public String issue(){var b=new StringBuilder();for(int i=64;i<224&&data.get(i)!=0;i++)b.append((char)(data.get(i)&65535));return b.toString();}
 private void refresh(){var t=tile();if(inventory.player.level().isClientSide()||t==null)return;value(0,Math.round(t.rpm()*10));value(1,t.generation());value(2,t.flow());value(3,t.core().steam());value(4,t.core().water());value(5,t.core().energy());value(6,t.target());value(7,t.throttle());value(8,t.enabled()?1:0);value(9,t.engaged()?1:0);value(10,t.rules().rating());value(11,t.formed()?1:0);value(12,t.layout().blades());value(13,t.rules().coils());String issue=t.issue();for(int i=0;i<160;i++)data.set(64+i,i<issue.length()?issue.charAt(i):0);}
 @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
 @Override public boolean stillValid(Player p){var t=tile();if(t==null||t!=original||t.isRemoved()||!p.isAlive())return false;var a=t.minimum();var b=t.maximum();return p.distanceToSqr(Math.clamp(p.getX(),a.getX(),b.getX()+1),Math.clamp(p.getY(),a.getY(),b.getY()+1),Math.clamp(p.getZ(),a.getZ(),b.getZ()+1))<=64;}
 @Override public boolean clickMenuButton(Player p,int id){if(!stillValid(p)||!p.mayBuild())return false;if(id==20&&p instanceof net.minecraft.server.level.ServerPlayer server){com.neolena.essenceindustry.blueprint.BlueprintStorage.open(server,tile());return true;}if(id<0||id>5)return false;tile().control(id);broadcastChanges();return true;}
 @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
