package com.neolena.essenceindustry.construction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
public final class BuilderMenu extends AbstractContainerMenu {
 private final Inventory inv;private final int anchor;private final ItemStack bound;private final int[] sync=new int[19];
 public BuilderMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,buf.readVarInt());}
 public BuilderMenu(int id,Inventory inv,int anchor){super(QuantumBuilder.MENU.get(),id);this.inv=inv;this.anchor=anchor;bound=anchor>=0&&anchor<inv.getContainerSize()?inv.getItem(anchor):ItemStack.EMPTY;
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)playerSlot(9+y*9+x,111+x*18,232+y*18);for(int x=0;x<9;x++)playerSlot(x,111+x*18,290);
  addDataSlots(new ContainerData(){public int getCount(){return 19;}public void set(int i,int v){sync[i]=v;}public int get(int i){return inv.player.level().isClientSide()?sync[i]:value(i);}});
 }
 private void playerSlot(int id,int x,int y){addSlot(new Slot(inv,id,x,y){@Override public boolean mayPickup(Player p){return id!=anchor;}@Override public boolean mayPlace(ItemStack s){return id!=anchor;}});}
 public ItemStack tool(){return inv.player.level().isClientSide()?inv.getItem(anchor):bound;}
 private int value(int i){var s=BuilderItem.settings(bound);return switch(i){case 17->BuilderWork.undoOperations(bound);case 18->BuilderWork.undoBlocks(bound);case 16->bound.has(QuantumBuilder.ANCHOR.get())?1:0;case 0->s.mode();case 1->s.width();case 2->s.height();case 3->s.depth();case 4->s.rotation();case 5->s.offset();case 6->s.source();case 7->s.centered()?1:0;case 8->BuilderWork.buffered(bound);case 9->bound.getOrDefault(QuantumBuilder.JOURNAL.get(),BuilderJournal.EMPTY).blocks().size();case 10->inv.player instanceof ServerPlayer p&&BuilderWork.busy(p)?1:0;case 11->bound.has(QuantumBuilder.MATERIAL.get())?Block.getId(bound.get(QuantumBuilder.MATERIAL.get()))&65535:0;case 15->Math.min(32767,BuilderPlan.count(s));case 14->bound.has(QuantumBuilder.RECOVERY.get())?1:0;case 13->bound.has(QuantumBuilder.MATERIAL.get())?Block.getId(bound.get(QuantumBuilder.MATERIAL.get()))>>>16:0;case 12->!bound.has(QuantumBuilder.LINK.get())?0:net.neoforged.fml.ModList.get().isLoaded("ae2")&&inv.player instanceof ServerPlayer p&&BuilderMe.grid(p,bound)!=null?2:1;default->0;};}
 public int stat(int i){return inv.player.level().isClientSide()?sync[i]:value(i);}
 public BuilderSettings settings(){return new BuilderSettings(stat(0),Math.max(1,stat(1)),Math.max(1,stat(2)),Math.max(1,stat(3)),stat(4),stat(5),stat(6),stat(7)!=0);}
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.mayBuild()&&anchor>=0&&anchor<inv.getContainerSize()&&tool().getItem() instanceof BuilderItem&&(p.level().isClientSide()||inv.getItem(anchor)==bound);}
 @Override public boolean clickMenuButton(Player p,int id){if(!(p instanceof ServerPlayer sp)||!stillValid(p)||id<0||id>15)return false;
  if(id==13){BuilderWork.cancel(sp);return true;}if(bound.has(QuantumBuilder.RECOVERY.get())||BuilderWork.busy(sp))return false;
  if(id==12)BuilderWork.undo(sp,bound);else if(id==14)BuilderWork.returnItems(sp,bound);else if(id==15)bound.remove(QuantumBuilder.LINK.get());else if(id==10&&!net.neoforged.fml.ModList.get().isLoaded("ae2"))return false;else bound.set(QuantumBuilder.SETTINGS.get(),BuilderItem.settings(bound).change(id));inv.setChanged();broadcastChanges();return true;
 }
 @Override public void clicked(int slot,int button,ContainerInput input,Player p){if(!stillValid(p)||input==ContainerInput.SWAP&&button==anchor)return;if(slot>=0&&slot<slots.size()&&slots.get(slot).getContainerSlot()==anchor)return;super.clicked(slot,button,input,p);}
 @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
