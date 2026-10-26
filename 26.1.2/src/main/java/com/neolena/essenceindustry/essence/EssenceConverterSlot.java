package com.neolena.essenceindustry.essence;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** A normal-stack window onto bulk stock. Each vanilla edit applies exactly once. */
final class EssenceConverterSlot extends Slot {
    private final ResourceHandler<ItemResource> inventory;
    private final EssenceConverterTile tile;
    private final int index;
    private ItemStack cached;
    private int displayed;
    EssenceConverterSlot(ResourceHandler<ItemResource> inventory, int index, EssenceConverterTile tile) {
        super(new SimpleContainer(6), index, 36+index*46,62);
        this.inventory=inventory;this.tile=tile;this.index=index;
        if(tile!=null)tile.watchSlot(this);
    }
    @Override public ItemStack getItem() {
        displayed=(int)Math.min(64L,inventory.getAmountAsLong(index));
        return cached=inventory.getResource(index).toStack(displayed);
    }
    @Override public void set(ItemStack stack) {
        if(tile==null){((ItemStacksResourceHandler)inventory).set(index,ItemResource.of(stack),stack.getCount());cached=null;return;}
        long count=inventory.getAmountAsLong(index);
        if(!tile.storageAvailable() || stack.getCount()>64 || (!stack.isEmpty() && !mayPlace(stack)))return;
        long next=Math.addExact(count,stack.getCount()-Math.min(64L,count));
        tile.inventory().set(index,next==0?ItemResource.EMPTY:count>0?inventory.getResource(index):ItemResource.of(stack),next);
        cached=null;
    }
    @Override public void setChanged() {
        var edited=cached;
        if(edited!=null && edited.getCount()!=displayed)set(edited);
        cached=null;
    }
    @Override public ItemStack remove(int amount) {
        if(amount<=0 || tile!=null && !tile.storageAvailable())return ItemStack.EMPTY;
        var resource=inventory.getResource(index);if(resource.isEmpty())return ItemStack.EMPTY;
        try(var tx=Transaction.openRoot()){
            int moved=inventory.extract(index,resource,Math.min(64,amount),tx);tx.commit();cached=null;return resource.toStack(moved);
        }
    }
    @Override public boolean mayPlace(ItemStack stack) { return !stack.isEmpty() && inventory.isValid(index,ItemResource.of(stack)); }
    @Override public boolean mayPickup(Player player) { return (tile==null || tile.storageAvailable()) && inventory.getAmountAsLong(index)>0; }
    @Override public int getMaxStackSize() { return 64; }
    @Override public int getMaxStackSize(ItemStack stack) { return mayPlace(stack)?64:0; }
    @Override public boolean isSameInventory(Slot other) { return other instanceof EssenceConverterSlot s && s.inventory==inventory; }
    void inventoryChanged() { cached=null; }
}
