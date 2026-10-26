package com.neolena.essenceindustry.quantum;

import com.neolena.essenceindustry.library.SeedDataSafety;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class QuantumToolMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final int anchor;
    private final ItemStack bound;
    private boolean loading=true;
    private boolean compactLayout;
    public final SimpleContainer modules;
    public QuantumToolMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf) {this(id,inv,buf.readVarInt());}
    public QuantumToolMenu(int id,Inventory inv,int anchor) {
        super(QuantumTools.MENU.get(),id);
        this.playerInventory=inv;this.anchor=anchor;
        bound=anchor>=0 && anchor<inv.getContainerSize()?inv.getItem(anchor):ItemStack.EMPTY;
        modules=new SimpleContainer(QuantumLoadout.SLOTS) {
            @Override public void setChanged() {
                super.setChanged();
                if(!loading && !inv.player.level().isClientSide() && stillValid(inv.player)) {
                    QuantumLoadout.write(bound,getItems());inv.setChanged();
                }
            }
        };
        if(bound.getItem() instanceof QuantumToolItem && bound.getOrDefault(QuantumTools.MODULES.get(),net.minecraft.world.item.component.ItemContainerContents.EMPTY).getSlots()<=QuantumLoadout.SLOTS) {
            var contents=QuantumLoadout.read(bound);for(int i=0;i<contents.size();i++)modules.setItem(i,contents.get(i));
        }
        loading=false;
        for(int y=0;y<QuantumLoadout.ROWS;y++)for(int x=0;x<QuantumLoadout.COLUMNS;x++) {
            addSlot(moduleSlot(y*QuantumLoadout.COLUMNS+x,24+x*18,39+y*18));
        }
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)addPlayerSlot(inv,9+y*9+x,111+x*18,209+y*18);
        for(int x=0;x<9;x++)addPlayerSlot(inv,x,111+x*18,267);
    }
    private Slot moduleSlot(int moduleIndex,int x,int y) {
        return new Slot(modules,moduleIndex,x,y) {
            @Override public int getMaxStackSize(){return 1;}
            @Override public boolean mayPlace(ItemStack s){return accepts(moduleIndex,s);}
        };
    }
    private void addPlayerSlot(Inventory inv,int index,int x,int y) {
        addSlot(playerSlot(inv,index,x,y));
    }
    private Slot playerSlot(Inventory inv,int inventoryIndex,int x,int y) {
        return new Slot(inv,inventoryIndex,x,y) {
            @Override public boolean mayPickup(Player p){return inventoryIndex!=anchor && super.mayPickup(p);}
            @Override public boolean mayPlace(ItemStack s){return inventoryIndex!=anchor && super.mayPlace(s);}
        };
    }
    /** Only display coordinates change; the bound item, containers and packet slot indices stay intact. */
    public void layoutClient(boolean compact) {
        if(!playerInventory.player.level().isClientSide() || compactLayout==compact)return;
        compactLayout=compact;
        for(int i=0;i<QuantumLoadout.SLOTS;i++)replaceDisplaySlot(i,moduleSlot(i,(compact?12:24)+i%QuantumLoadout.COLUMNS*18,(compact?31:39)+i/QuantumLoadout.COLUMNS*18));
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)replaceDisplaySlot(QuantumLoadout.SLOTS+y*9+x,playerSlot(playerInventory,9+y*9+x,(compact?79:111)+x*18,(compact?157:209)+y*18));
        for(int x=0;x<9;x++)replaceDisplaySlot(QuantumLoadout.SLOTS+27+x,playerSlot(playerInventory,x,(compact?79:111)+x*18,compact?215:267));
    }
    private void replaceDisplaySlot(int index,Slot slot){slot.index=index;slots.set(index,slot);}
    private boolean accepts(int index,ItemStack s) {
        if(!stillValid(playerInventory.player) || s.isEmpty() || !(s.getItem() instanceof QuantumModule) || s.getCount()!=1)return false;
        var items=new java.util.ArrayList<>(modules.getItems());items.set(index,s);
        if(!QuantumLoadout.valid(((QuantumToolItem)tool().getItem()).staff,items,tool()))return false;
        // Reject packet-heavy module components before vanilla removes the cursor/player stack.
        long weight=SeedDataSafety.weight(net.neoforged.neoforge.transfer.item.ItemResource.of(s),playerInventory.player.level().registryAccess());
        if(weight<=0 || weight>SeedDataSafety.BUDGET/128)return false;
        long total=0;for(var module:items)if(!module.isEmpty()){long n=SeedDataSafety.weight(net.neoforged.neoforge.transfer.item.ItemResource.of(module),playerInventory.player.level().registryAccess());if(n<=0)return false;total+=n;}
        return total<=SeedDataSafety.BUDGET*3/4;
    }
    public boolean staff(){return tool().getItem() instanceof QuantumToolItem q && q.staff;}
    public ItemStack tool(){return playerInventory.player.level().isClientSide() && anchor>=0 && anchor<playerInventory.getContainerSize()?playerInventory.getItem(anchor):bound;}
    @Override public boolean stillValid(Player p) {
        return p.isAlive() && p.mayBuild() && anchor>=0 && anchor<playerInventory.getContainerSize() && (p.level().isClientSide() || playerInventory.getItem(anchor)==bound) && tool().getCount()==1 && tool().getItem() instanceof QuantumToolItem;
    }
    @Override public void clicked(int slot,int button,ContainerInput input,Player p) {
        if(!stillValid(p))return;
        if(slot>=0 && slot<slots.size() && slots.get(slot).container==playerInventory && slots.get(slot).getContainerSlot()==anchor)return;
        if(input==ContainerInput.SWAP && button==anchor)return;
        super.clicked(slot,button,input,p);
    }
    @Override public ItemStack quickMoveStack(Player p,int index) {
        if(!stillValid(p) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.mayPickup(p) || !slot.hasItem())return ItemStack.EMPTY;
        var s=slot.getItem();var original=s.copy();
        if(!(index<QuantumLoadout.SLOTS?moveItemStackTo(s,QuantumLoadout.SLOTS,slots.size(),true):moveItemStackTo(s,0,QuantumLoadout.SLOTS,false)))return ItemStack.EMPTY;
        if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return original;
    }
}
