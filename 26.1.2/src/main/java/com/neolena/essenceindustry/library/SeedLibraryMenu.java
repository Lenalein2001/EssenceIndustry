package com.neolena.essenceindustry.library;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Display slots are immutable representatives, never backing inventory stacks. */
public final class SeedLibraryMenu extends AbstractContainerMenu {
    public static final int CROPS = 20, VARIANTS = 4, DISPLAY = CROPS + VARIANTS;
    private final SeedLibraryTile tile;
    private final ContainerLevelAccess access;
    private final SimpleContainer display = new SimpleContainer(DISPLAY);
    private final ContainerData data = new SimpleContainerData(14 + VARIANTS * 4);
    private long seenRevision = -1;
    private long seenVariants = -1;
    private long seenRanking = -1;
    private boolean dirty = true;
    private String query = "";
    private Item selected = Items.AIR;
    private int cropPage, variantPage;
    private List<Item> crops = List.of();
    private List<SeedVault.Record> variants = List.of();
    private record Ranked(SeedVault.Record record, SeedRank rank) {}
    public SeedLibraryMenu(int id, Inventory player, RegistryFriendlyByteBuf buffer) { this(id, player, buffer.readBlockPos(), null); }
    public SeedLibraryMenu(int id, Inventory player, SeedLibraryTile tile) { this(id, player, tile.getBlockPos(), tile); }
    private SeedLibraryMenu(int id, Inventory player, BlockPos pos, SeedLibraryTile tile) {
        super(SeedLibrary.MENU.get(), id); this.tile = tile;
        access = ContainerLevelAccess.create(player.player.level(), pos);
        addDataSlots(data);
        for (int i = 0; i < DISPLAY; i++) {
            int x = i < CROPS ? 15 + i % 5 * 18 : 124;
            int y = i < CROPS ? 56 + i / 5 * 18 : 55 + (i - CROPS) * 18;
            addSlot(new Slot(display, i, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
                @Override public boolean isFake() { return true; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(player, 9 + row * 9 + col, 79 + col * 18, 161 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(player, col, 79 + col * 18, 219));
        refresh();
    }
    private void stat(int field, int value) { int base = field == 6 ? 12 + VARIANTS * 4 : field * 2; data.set(base, value & 65535); data.set(base + 1, value >>> 16); }
    public int stat(int field) { int base = field == 6 ? 12 + VARIANTS * 4 : field * 2; return (data.get(base) & 65535) | (data.get(base + 1) & 65535) << 16; }
    public long count(int row) {
        long result = 0;
        for (int part = 0; part < 4; part++) result |= (long)(data.get(12 + row * 4 + part) & 65535) << (part * 16);
        return result;
    }
    private void count(int row, long count) { for (int part = 0; part < 4; part++) data.set(12 + row * 4 + part, (int)(count >>> (part * 16)) & 65535); }
    public void filter(String query) {
        if (tile == null) return;
        this.query = query.substring(0, Math.min(64, query.length())).toLowerCase(Locale.ROOT).strip();
        cropPage = 0; variantPage = 0; dirty = true; broadcastChanges();
    }
    private static int pages(int size, int width) { return Math.max(1, (size + width - 1) / width); }
    private void refresh() {
        boolean rankingChanged = seenRanking != SeedRank.revision();
        if (tile == null || (!dirty && seenRevision == tile.revision() && !rankingChanged)) return;
        seenRevision = tile.revision();
        if (!dirty && !rankingChanged && seenVariants == tile.vault().variantRevision()) {
            // Automated count changes only update the four visible quantities.
            // No catalogue scan, component stringification, sorting or icon rebuild.
            for (int row = 0; row < VARIANTS; row++) {
                var shown = display.getItem(CROPS + row);
                count(row, shown.isEmpty() ? 0 : tile.vault().count(ItemResource.of(shown)));
            }
            return;
        }
        seenVariants = tile.vault().variantRevision(); seenRanking = SeedRank.revision(); dirty = false;
        var records = tile.quarantined() ? java.util.List.<SeedVault.Record>of() : tile.vault().records();
        stat(6, tile.quarantined() ? 1 : tile.safeToMove() ? 0 : 2);
        crops = records.stream().map(record -> record.seed().getItem()).distinct()
                .filter(item -> (item.getName(item.getDefaultInstance()).getString() + " " + BuiltInRegistries.ITEM.getKey(item)).toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString())).toList();
        if (!crops.contains(selected)) { selected = crops.isEmpty() ? Items.AIR : crops.getFirst(); variantPage = 0; }
        cropPage = Math.clamp(cropPage, 0, pages(crops.size(), CROPS) - 1);
        variants = records.stream().filter(record -> record.seed().getItem() == selected)
                .map(record -> new Ranked(record, SeedRank.of(record.seed())))
                .sorted(Comparator.comparing(Ranked::rank, SeedRank.BEST_FIRST)).map(Ranked::record).toList();
        variantPage = Math.clamp(variantPage, 0, pages(variants.size(), VARIANTS) - 1);
        for (int i = 0; i < CROPS; i++) {
            int index = cropPage * CROPS + i;
            display.setItem(i, index < crops.size() ? crops.get(index).getDefaultInstance() : ItemStack.EMPTY);
        }
        for (int i = 0; i < VARIANTS; i++) {
            int index = variantPage * VARIANTS + i;
            display.setItem(CROPS + i, index < variants.size() ? variants.get(index).seed().toStack(1) : ItemStack.EMPTY);
            count(i, index < variants.size() ? variants.get(index).count() : 0);
        }
        stat(0, cropPage); stat(1, pages(crops.size(), CROPS)); stat(2, variantPage);
        stat(3, pages(variants.size(), VARIANTS)); stat(4, crops.size()); stat(5, variants.size());
    }
    @Override public void broadcastChanges() { refresh(); super.broadcastChanges(); }
    @Override public void broadcastFullState() { refresh(); super.broadcastFullState(); }
    @Override public boolean stillValid(Player player) {
        return (tile == null || tile.present())
                && stillValid(access, player, SeedLibrary.BLOCK.get());
    }
    @Override public boolean canDragTo(Slot slot) { return slot.index >= DISPLAY && super.canDragTo(slot); }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return slot.index >= DISPLAY && super.canTakeItemForPickAll(stack, slot); }
    private int deposit(net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> source, ItemResource resource, int limit) {
        if (tile == null || resource.isEmpty() || limit <= 0 || !tile.storageAvailable()) return 0;
        try (var tx = Transaction.openRoot()) {
            int accepted = tile.vault().insert(resource, limit, tx);
            if (source.extract(resource, accepted, tx) != accepted) return 0;
            tx.commit(); return accepted;
        }
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (tile == null || index < DISPLAY || index >= slots.size() || !stillValid(player)) return ItemStack.EMPTY;
        var slot = slots.get(index); var original = slot.getItem().copy();
        if (original.isEmpty()) return ItemStack.EMPTY;
        var source = net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper.of(player).getSlot(slot.getContainerSlot());
        if (deposit(source, ItemResource.of(original), original.getCount()) == 0) return ItemStack.EMPTY;
        broadcastChanges(); return original;
    }
    private void withdraw(ItemResource resource, int requested, Player player, boolean toInventory) {
        int amount = (int)Math.min((long)Math.min(requested, resource.getMaxStackSize()), tile.vault().count(resource));
        if (amount <= 0) return;
        var destination = toInventory ? net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper.of(player).getMainSlots()
                : net.neoforged.neoforge.transfer.item.CarriedSlotWrapper.of(this);
        try (var tx = Transaction.openRoot()) {
            int inserted = destination.insert(resource, amount, tx);
            if (tile.vault().extract(resource, inserted, tx) == inserted) tx.commit();
        }
    }
    @Override public void clicked(int index, int button, ContainerInput input, Player player) {
        if (index >= 0 && index < DISPLAY) {
            if (tile == null || !stillValid(player) || (input != ContainerInput.PICKUP && input != ContainerInput.QUICK_MOVE) || button < 0 || button > 1) return;
            var shown = slots.get(index).getItem();
            if (!getCarried().isEmpty()) deposit(net.neoforged.neoforge.transfer.item.CarriedSlotWrapper.of(this), ItemResource.of(getCarried()), button == 1 ? 1 : getCarried().getCount());
            else if (!shown.isEmpty()) {
                if (index < CROPS) { selected = shown.getItem(); variantPage = 0; dirty = true; }
                else withdraw(ItemResource.of(shown), button == 1 ? 1 : shown.getMaxStackSize(), player, input == ContainerInput.QUICK_MOVE);
            }
            broadcastChanges(); return;
        }
        if (tile != null && !stillValid(player)) return;
        super.clicked(index, button, input, player);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (tile == null || !stillValid(player) || button < 0 || button > 4) return false;
        switch (button) {
            case 0 -> cropPage--;
            case 1 -> cropPage++;
            case 2 -> variantPage--;
            case 3 -> variantPage++;
            case 4 -> {
                for (int i = DISPLAY; i < slots.size(); i++) {
                    var slot = slots.get(i); var stack = slot.getItem();
                    if (!stack.isEmpty()) deposit(net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper.of(player).getSlot(slot.getContainerSlot()), ItemResource.of(stack), stack.getCount());
                }
            }
        }
        dirty = true; broadcastChanges(); return true;
    }
}

