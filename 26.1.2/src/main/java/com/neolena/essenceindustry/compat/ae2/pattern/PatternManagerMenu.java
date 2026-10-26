package com.neolena.essenceindustry.compat.ae2.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import java.util.*;

public final class PatternManagerMenu extends AbstractContainerMenu {
    public static final int ROWS = 6;
    private final PatternManagerTile tile;
    private final ContainerLevelAccess access;
    private final SimpleContainer display = new SimpleContainer(ROWS * 2);
    private final ContainerData data;
    private String query = "";
    private int page;
    private List<EssencePatternCatalog.Entry> filtered = List.of();
    private boolean dirty = true;
    private long revision = -1;
    public PatternManagerMenu(int id, Inventory player, RegistryFriendlyByteBuf buffer) { this(id, player, buffer.readBlockPos(), null); }
    public PatternManagerMenu(int id, Inventory player, PatternManagerTile tile) { this(id, player, tile.getBlockPos(), tile); }
    private PatternManagerMenu(int id, Inventory player, BlockPos pos, PatternManagerTile tile) {
        super(EssencePatternManager.MENU.get(), id); this.tile = tile;
        access = ContainerLevelAccess.create(player.player.level(), pos);
        data = new SimpleContainerData(ROWS + 5); addDataSlots(data);
        for (int row = 0; row < ROWS; row++) for (int column = 0; column < 2; column++) {
            final int index = row * 2 + column;
            addSlot(new Slot(display, index, 14 + column * 34, 55 + row * 24) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player p) { return false; }
            });
        }
    }
    public int stat(int id) { return data.get(id); }
    public void filter(String text) { if (text.length() <= 64) { query = text.toLowerCase(Locale.ROOT).strip(); page = 0; dirty = true; } }
    private void update() {
        if (tile == null) return;
        filtered = tile.entries().stream().filter(e -> query.isEmpty() || e.id().toLowerCase(Locale.ROOT).contains(query)
            || e.output().toStack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)
            || e.inputs().keySet().stream().anyMatch(k -> ((appeng.api.stacks.AEItemKey)k).toStack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query))).toList();
        page = Math.min(page, Math.max(0, (filtered.size() - 1) / ROWS));
        data.set(0, page); data.set(1, Math.max(1, (filtered.size() + ROWS - 1) / ROWS)); data.set(2, filtered.size());
        for (int row = 0; row < ROWS; row++) {
            int index = page * ROWS + row;
            if (index >= filtered.size()) { display.setItem(row * 2, ItemStack.EMPTY); display.setItem(row * 2 + 1, ItemStack.EMPTY); data.set(5 + row, -1); continue; }
            var e = filtered.get(index); var first = e.inputs().entrySet().stream().sorted(Comparator.comparing(x -> x.getKey().toString())).findFirst().orElseThrow();
            var input = ((appeng.api.stacks.AEItemKey)first.getKey()).toStack((int)(long)first.getValue());
            var lore = new ArrayList<Component>(); lore.add(Component.literal(e.id()));
            for (var ingredient : e.inputs().entrySet()) lore.add(Component.literal(ingredient.getValue() + " x ").append(((appeng.api.stacks.AEItemKey)ingredient.getKey()).toStack().getHoverName()));
            input.set(DataComponents.LORE, new ItemLore(lore)); display.setItem(row * 2, input);
            display.setItem(row * 2 + 1, e.output().toStack((int)e.amount())); data.set(5 + row, tile.enabled(e.id()) ? 1 : 0);
        }
        dirty = false;
        revision = tile.revision();
    }
    @Override public void broadcastChanges() {
        if (tile != null) { if (dirty || revision != tile.revision()) update(); data.set(3, tile.quarantined() ? 2 : tile.getMainNode().isActive() ? tile.leader() ? 1 : 3 : 0); data.set(4, tile.pending().keys()); }
        super.broadcastChanges();
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (tile == null || !stillValid(player)) return false;
        if (button == 0) page = Math.max(0, page - 1);
        else if (button == 1) page = Math.min(Math.max(0, (filtered.size() - 1) / ROWS), page + 1);
        else if (button >= 10 && button < 10 + ROWS) {
            int index = page * ROWS + button - 10; if (index >= filtered.size()) return false;
            tile.toggle(filtered.get(index).id());
        } else return false;
        dirty = true; return true;
    }
    @Override public boolean stillValid(Player player) { return (tile == null || !tile.isRemoved()) && stillValid(access, player, EssencePatternManager.BLOCK.get()); }
    @Override public void clicked(int slot, int button, ContainerInput input, Player player) { /* Display-only slots never transfer items, including creative clone. */ }
    @Override public ItemStack quickMoveStack(Player p, int slot) { return ItemStack.EMPTY; }
}
