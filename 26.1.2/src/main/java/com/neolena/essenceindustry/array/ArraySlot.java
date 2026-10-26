package com.neolena.essenceindustry.array;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.*;
final class ArraySlot extends ResourceHandlerSlot {
 private final ArrayTile tile;private final ArrayMenu menu;private final int tab;private final boolean installation;
 ArraySlot(ArrayMenu menu,ItemStacksResourceHandler handler,int index,int x,int y,ArrayTile tile,int tab,boolean installation){this(menu,handler,handler::set,index,x,y,tile,tab,installation);}
 ArraySlot(ArrayMenu menu,net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> handler,net.neoforged.neoforge.transfer.IndexModifier<ItemResource> modifier,int index,int x,int y,ArrayTile tile,int tab,boolean installation){super(handler,modifier,index,x,y);this.menu=menu;this.tile=tile;this.tab=tab;this.installation=installation;if(tile!=null)tile.watch(this);}
 void changed(){clearCachedReturnStack();}
 @Override public boolean isActive(){return menu.tab()==tab;}
 private boolean editable(){return isActive()&&(tile==null||(installation?tile.installationsEditable():tile.mayEdit()));}
 @Override public boolean mayPickup(Player player){return editable();}
 @Override public boolean mayPlace(ItemStack stack){return editable()&&super.mayPlace(stack);}
}
