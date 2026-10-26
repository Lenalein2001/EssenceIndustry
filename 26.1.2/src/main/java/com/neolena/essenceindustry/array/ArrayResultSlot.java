package com.neolena.essenceindustry.array;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
/** Bounded vanilla stack window onto exact long-count results. Never accepts player input. */
final class ArrayResultSlot extends Slot {
 private final ArrayMenu menu;private final ArrayTile tile;private final ResourceHandler<ItemResource> handler;private final int local;
 private ItemStack cached;private int shown;private ItemResource resource=ItemResource.EMPTY;
 ArrayResultSlot(ArrayMenu menu,int local,int x,int y,ArrayTile tile){this(menu,local,x,y,tile,tile==null?new ItemStacksResourceHandler(144):tile.outputHandler());}
 private ArrayResultSlot(ArrayMenu menu,int local,int x,int y,ArrayTile tile,ResourceHandler<ItemResource> handler){super(new SimpleContainer(12),local,x,y);this.menu=menu;this.local=local;this.tile=tile;this.handler=handler;}
 /** Reflow the client view without replacing its result backing or selected output page. */
 ArrayResultSlot atClientPosition(int x,int y){if(tile!=null)throw new IllegalStateException("Cannot reflow server result slots");return new ArrayResultSlot(menu,local,x,y,null,handler);}
 private int index(){return menu.stat(10)*12+local;}
 @Override public ItemStack getItem(){int i=index();if(i>=128){shown=0;resource=ItemResource.EMPTY;return cached=ItemStack.EMPTY;}if(tile!=null&&i<tile.ledger().output().size()&&tile.ledger().output().get(i).what() instanceof appeng.api.stacks.AEFluidKey){resource=ItemResource.EMPTY;shown=1;return cached=appeng.api.stacks.GenericStack.wrapInItemStack(tile.ledger().output().get(i));}resource=handler.getResource(i);int max=resource.isEmpty()?64:resource.toStack().getMaxStackSize();shown=(int)Math.min(max,handler.getAmountAsLong(i));return cached=resource.toStack(shown);}
 @Override public boolean isActive(){return menu.tab()==0;}
 @Override public boolean mayPlace(ItemStack s){return false;}
 @Override public boolean mayPickup(Player p){var item=getItem();var generic=appeng.api.stacks.GenericStack.unwrapItemStack(item);return isActive()&&(generic==null||!(generic.what() instanceof appeng.api.stacks.AEFluidKey))&&(tile==null||tile.mayEdit());}
 @Override public void set(ItemStack s){
  if(tile==null){((ItemStacksResourceHandler)handler).set(index(),ItemResource.of(s),s.getCount());cached=null;return;}
  if(!tile.mayEdit()||(!s.isEmpty()&&!ItemResource.of(s).equals(resource))||s.getCount()>shown)return;
  int remove=shown-s.getCount();if(remove>0&&!resource.isEmpty())try(var tx=Transaction.openRoot()){if(handler.extract(index(),resource,remove,tx)==remove)tx.commit();}cached=null;
 }
 @Override public void setChanged(){var edited=cached;if(edited!=null&&edited.getCount()!=shown)set(edited);cached=null;}
 @Override public ItemStack remove(int n){if(tile==null)return super.remove(n);if(!tile.mayEdit()||n<=0)return ItemStack.EMPTY;var r=handler.getResource(index());if(r.isEmpty())return ItemStack.EMPTY;try(var tx=Transaction.openRoot()){int take=handler.extract(index(),r,Math.min(n,r.toStack().getMaxStackSize()),tx);if(take==0)return ItemStack.EMPTY;tx.commit();cached=null;return r.toStack(take);}}
}
