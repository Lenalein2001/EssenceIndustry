package com.neolena.essenceindustry.compat.ae2.bulk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import java.util.*;
public final class DiskManagerMenu extends AbstractContainerMenu {
 public static final int ROWS=6;
 private final DiskManagerTile tile;private final ContainerLevelAccess access;
 private final SimpleContainer display=new SimpleContainer(ROWS);private final ContainerData data=new SimpleContainerData(4);private int page;
 private List<DiskManagerTile.Suggestion> rows=List.of();
 public DiskManagerMenu(int id,Inventory player,RegistryFriendlyByteBuf buf){this(id,player,buf.readBlockPos(),null);}
 public DiskManagerMenu(int id,Inventory player,DiskManagerTile tile){this(id,player,tile.getBlockPos(),tile);}
 private DiskManagerMenu(int id,Inventory player,BlockPos pos,DiskManagerTile tile){super(BulkDiskManager.MENU.get(),id);this.tile=tile;access=ContainerLevelAccess.create(player.player.level(),pos);addDataSlots(data);
  for(int row=0;row<ROWS;row++)addSlot(new Slot(display,row,14,55+row*24){public boolean mayPlace(ItemStack stack){return false;}public boolean mayPickup(Player p){return false;}});
  if(tile!=null){tile.scan();refresh();}
 }
 private void refresh(){
  rows=tile.suggestions();page=Math.min(page,Math.max(0,(rows.size()-1)/ROWS));data.set(0,page);data.set(1,Math.max(1,(rows.size()+ROWS-1)/ROWS));
  for(int row=0;row<ROWS;row++){int i=page*ROWS+row;if(i>=rows.size()){display.setItem(row,ItemStack.EMPTY);continue;}var e=rows.get(i);var stack=e.key().toStack();stack.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(String.format(Locale.ROOT,"%,d in network",e.amount())))));display.setItem(row,stack);}
 }
 public int stat(int i){return data.get(i);}
 @Override public void broadcastChanges(){if(tile!=null){data.set(3,tile.reserved()?1:0);data.set(2,!tile.getMainNode().isActive()?0:tile.limited()?2:1);}super.broadcastChanges();}
 @Override public boolean clickMenuButton(Player player,int id){
  if(tile==null || !player.mayBuild() || !stillValid(player))return false;
  if(id==0){tile.scan();refresh();}else if(id==1){page=Math.max(0,page-1);refresh();}else if(id==2){page++;refresh();}
  else if(id>=10 && id<10+ROWS){int i=page*ROWS+id-10; if(i>=rows.size() || !rows.equals(tile.suggestions()))return false;boolean configured=tile.configure(player.getInventory(),i);
            player.sendOverlayMessage(Component.translatable("gui.essenceindustry.disk."+(configured?"configured":"need_cell")));return configured;}else return false;return true;
 }
 @Override public boolean stillValid(Player p){return (tile==null || !tile.isRemoved()) && stillValid(access,p,BulkDiskManager.BLOCK.get());}
 @Override public void clicked(int slot,int button,ContainerInput input,Player player){}
 @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
}
