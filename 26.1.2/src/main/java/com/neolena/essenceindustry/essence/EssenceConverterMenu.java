package com.neolena.essenceindustry.essence;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.item.*;

public final class EssenceConverterMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final EssenceConverterTile tile;
    private final ContainerData data;
    public EssenceConverterMenu(int id, Inventory player, RegistryFriendlyByteBuf buffer) {
        this(id, player, buffer.readBlockPos(), null);
    }
    public EssenceConverterMenu(int id, Inventory player, EssenceConverterTile tile) { this(id, player, tile.getBlockPos(), tile); }
    private EssenceConverterMenu(int id, Inventory player, BlockPos pos, EssenceConverterTile tile) {
        super(EssenceConverter.MENU.get(), id);
        this.tile = tile;
        access = ContainerLevelAccess.create(player.player.level(), pos);
        var handler = tile == null ? new ItemStacksResourceHandler(6) {
            @Override public boolean isValid(int index, ItemResource resource) { return EssenceConverterTile.essence(index) != Items.AIR && resource.getItem() == EssenceConverterTile.essence(index); }
        } : tile.inventory();
        if (tile == null) {
            data = new SimpleContainerData(34);
            data.set(0, 1); data.set(1, EssenceConverterTile.essence(5) == Items.AIR ? 4 : 5);
        } else data = new ContainerData() {
            @Override public int get(int index) { return index >= 10 ? (int)((tile.inventory().getAmountAsLong((index-10)/4) >>> (16*((index-10)%4))) & 65535L) : index == 0 ? (tile.upgrading() ? 1 : 0) : index == 1 ? tile.highest() : index == 8 ? (tile.meEnabled() ? 1 : 0) : index == 9 ? tile.meStatus() : tile.reserve(index - 2); }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 34; }
        };
        addDataSlots(data);
        for (int index = 0; index < 6; index++) addSlot(new EssenceConverterSlot(handler, index, tile));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(player, 9 + row * 9 + column, 79 + column * 18, 185 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(player, column, 79 + column * 18, 243));
    }
    public boolean upgrading() { return data.get(0) == 1; }
    public int highest() { return data.get(1); }
    public boolean meEnabled() { return data.get(8) == 1; }
    public int meStatus() { return data.get(9); }
    public int reserve(int tier) { return data.get(tier + 2); }
    public long count(int tier) { long n=0;for(int part=0;part<4;part++)n|=((long)data.get(10+tier*4+part)&65535L)<<(16*part);return n; }
    public void setReserve(int tier, int amount) { if (tile != null) tile.setReserve(tier, amount); }
    public int target() { return upgrading() ? highest() : 0; }
    @Override public void removed(Player player) {
        super.removed(player);
        if (tile != null) for (var slot : slots) if (slot instanceof EssenceConverterSlot converterSlot) tile.unwatchSlot(converterSlot);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (tile == null || !stillValid(player)) return false;
        if (button == 2) { tile.toggleMe(); return true; }
        if (button != 0 && button != 1) return false;
        if (tile.upgrading() != (button == 0)) tile.toggleMode();
        return true;
    }
    @Override public boolean stillValid(Player player) { return (tile == null || tile.storageAvailable()) && stillValid(access, player, EssenceConverter.BLOCK.get()); }
    @Override public void clicked(int index, int button, ContainerInput input, Player player) {
        if (tile != null && !stillValid(player)) return;
        if (tile != null && index >= 0 && index < 6 && input == ContainerInput.PICKUP && (button==0 || button==1) && !getCarried().isEmpty()
                && tile.inventory().isValid(index,ItemResource.of(getCarried()))) {
            var carried=getCarried();
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                int moved=tile.inventory().insert(index,ItemResource.of(carried),button==1?1:carried.getCount(),tx);
                tx.commit();carried.shrink(moved);setCarried(carried);
            }
            return;
        }
        super.clicked(index, button, input, player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var source = slot.getItem(); var original = source.copy();
        if (tile != null && index >= 6) {
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                int moved=tile.inventory().insert(ItemResource.of(source),source.getCount(),tx);
                if(moved==0)return ItemStack.EMPTY;
                tx.commit();source.shrink(moved);slot.set(source);slot.onTake(player,source);return original;
            }
        }
        if (!(index < 6 ? moveItemStackTo(source, 6, slots.size(), true) : moveItemStackTo(source, 0, 6, false))) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source);
        return original;
    }
}
