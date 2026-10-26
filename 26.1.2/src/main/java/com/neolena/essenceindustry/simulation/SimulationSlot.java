package com.neolena.essenceindustry.simulation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.*;
final class SimulationSlot extends ResourceHandlerSlot {
 private final CropSimulationTile tile;private final SimulationStore store;private final int index;
 SimulationSlot(SimulationStore inv,int index,int x,int y,CropSimulationTile tile){super(inv,inv::setMenuSlot,index,x,y);this.tile=tile;store=inv;this.index=index;if(tile!=null)tile.watch(this);}
 void changed(){clearCachedReturnStack();}
 @Override public boolean mayPlace(ItemStack s){return (tile==null || tile.available()) && store.canSetMenuSlot(index,ItemResource.of(s)) && super.mayPlace(s);}
 @Override public boolean mayPickup(Player p){return (tile==null || tile.available()) && super.mayPickup(p);}
}
