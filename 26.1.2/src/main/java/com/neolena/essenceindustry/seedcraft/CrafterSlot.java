package com.neolena.essenceindustry.seedcraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.*;
final class CrafterSlot extends ResourceHandlerSlot {
 private final SeedCrafterTile tile;
 CrafterSlot(ItemStacksResourceHandler handler,int index,int x,int y,SeedCrafterTile tile){super(handler,handler::set,index,x,y);this.tile=tile;if(tile!=null)tile.watch(this);}
 void changed(){clearCachedReturnStack();}
 @Override public boolean mayPlace(ItemStack stack){return getContainerSlot()<SeedCrafterTile.INPUTS && (tile==null || tile.available()) && super.mayPlace(stack);}
 @Override public boolean mayPickup(Player player){return (tile==null || tile.available()) && super.mayPickup(player);}
}
