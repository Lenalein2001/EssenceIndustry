package com.neolena.essenceindustry.compat.pylons;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.permutated.pylons.machines.base.AbstractPylonContainer;

public final class EnhancedHarvestingPylonMenu extends AbstractPylonContainer {
    private int meStatus;
    public int meStatus() { return meStatus; }
    @Override public boolean clickMenuButton(net.minecraft.world.entity.player.Player player, int button) {
        if (button != 100 || !player.mayBuild() || !stillValid(player)) return false;
        if (player.level().getBlockEntity(blockPos) instanceof EnhancedHarvestingPylonTile tile) { tile.toggleMe(); return true; }
        return false;
    }
    public EnhancedHarvestingPylonMenu(int id, Inventory inventory, FriendlyByteBuf data) { super(EnhancedHarvestingPylon.MENU.get(), id, inventory, data);
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            public int get() { return inventory.player.level().getBlockEntity(blockPos) instanceof EnhancedHarvestingPylonTile tile ? tile.meStatus() : meStatus; }
            public void set(int value) { meStatus = value; }
        }); }
    @Override protected Supplier<Block> getBlock() { return EnhancedHarvestingPylon.BLOCK::get; }
    @Override protected int getInventorySize() { return 0; }
    @Override public void registerHandlerSlots(ItemStacksResourceHandler inventory) {}
    @Override public boolean shouldRenderRange() { return true; }
}
