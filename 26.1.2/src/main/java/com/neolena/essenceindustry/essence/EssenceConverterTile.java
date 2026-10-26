package com.neolena.essenceindustry.essence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.*;

public final class EssenceConverterTile extends BlockEntity {
    public static final int TIERS = 6;
    private static final class TierItems {
        static final Item[] ITEMS = java.util.stream.Stream.of("mysticalagriculture:inferium_essence", "mysticalagriculture:prudentium_essence",
                "mysticalagriculture:tertium_essence", "mysticalagriculture:imperium_essence", "mysticalagriculture:supremium_essence",
                "mysticalagradditions:insanium_essence").map(id -> BuiltInRegistries.ITEM.getValue(Identifier.parse(id))).toArray(Item[]::new);
    }
    private com.mojang.serialization.Dynamic<?> recovery;
    private com.mojang.serialization.Dynamic<?> reserveRecovery;
    private com.mojang.serialization.Dynamic<?> exportRecovery, importRecovery, nodeRecovery;
    private boolean meEnabled, exporting;
    private long storageRevision;
    public long storageRevision() { return storageRevision; }
    @Override public void setChanged() { storageRevision++; super.setChanged(); }
    private final ConverterNetwork network = EssenceConverter.networkFactory == null ? null : EssenceConverter.networkFactory.apply(this);
    public ConverterNetwork network() { return network; }
    public boolean meEnabled() { return meEnabled; }
    public int meStatus() { return quarantined() ? 4 : network == null ? 3 : network.status(); }
    public void toggleMe() {
        if (!storageAvailable() || network == null) return;
        meEnabled = !meEnabled; setChanged(); network.settingsChanged();
        if (level != null) level.invalidateCapabilities(worldPosition);
    }
    private final int[] reserves = new int[TIERS];
    public int reserve(int tier) { return reserves[tier]; }
    public boolean setReserve(int tier, int amount) {
        if (!storageAvailable() || tier < 0 || tier > highest() || amount < 0 || amount > 64) return false;
        if (reserves[tier] != amount) { reserves[tier] = amount; setChanged(); wake(); }
        return true;
    }
    public boolean quarantined() { return recovery != null || reserveRecovery != null || exportRecovery != null || importRecovery != null; }
    public boolean safeToMove() {
        if (exporting || working) return false;
        // Normal settings are exactly six bounded integers; avoid serialization on normal removal checks.
        if (recovery == null && reserveRecovery == null && exportRecovery == null && importRecovery == null) return true;
        var safety = com.neolena.essenceindustry.library.SeedDataSafety.BUDGET;
        long inventoryWeight = recovery == null ? 0 : com.neolena.essenceindustry.library.SeedDataSafety.tagWeight(recovery.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue());
        long reserveWeight = com.neolena.essenceindustry.library.SeedDataSafety.tagWeight(reserveData().convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue());
        long exportWeight = exportRecovery == null ? 0 : com.neolena.essenceindustry.library.SeedDataSafety.tagWeight(exportRecovery.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue());
        long importWeight = importRecovery == null ? 0 : com.neolena.essenceindustry.library.SeedDataSafety.tagWeight(importRecovery.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue());
        return inventoryWeight <= safety && reserveWeight <= safety - inventoryWeight && exportWeight <= safety - inventoryWeight - reserveWeight && importWeight <= safety - inventoryWeight - reserveWeight - exportWeight;
    }
    private boolean scheduled;
    private boolean working;
    private final java.util.Set<EssenceConverterSlot> openSlots = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    private final EssenceStorage inventory = new EssenceStorage(() -> {
        setChanged();
        for (var view : openSlots) view.inventoryChanged();
        if (!working) wake();
    });
    private final ResourceHandler<ItemResource> automation = new ResourceHandler<>() {
        @Override public int size() { return TIERS; }
        @Override public ItemResource getResource(int index) { return inventory.getResource(index); }
        @Override public long getAmountAsLong(int index) { return inventory.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return inventory.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return inventory.isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return inventory.insert(index, resource, amount, transaction); }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return index == target() ? inventory.extract(index, resource, (int)Math.min((long)amount, Math.max(0L, inventory.getAmountAsLong(index) - reserve(index))), transaction) : 0;
        }
    };
    public EssenceConverterTile(BlockPos pos, BlockState state) { super(EssenceConverter.TILE.get(), pos, state); }
    public static Item essence(int tier) { return TierItems.ITEMS[tier]; }
    public int highest() { return essence(5) == Items.AIR ? 4 : 5; }
    public boolean upgrading() { return getBlockState().getValue(EssenceConverterBlock.UPGRADE); }
    public int target() { return upgrading() ? highest() : 0; }
    public EssenceStorage inventory() { return inventory; }
    private final ResourceHandler<ItemResource> guardedAutomation = new com.neolena.essenceindustry.item.GuardedItemStorage(automation, this::storageAvailable);
    private boolean unloaded;
    public boolean storageAvailable() { return !exporting && !quarantined() && !unloaded && !isRemoved() && level instanceof ServerLevel server && server.isLoaded(worldPosition) && server.getBlockEntity(worldPosition) == this; }
    public ResourceHandler<ItemResource> automation() { return guardedAutomation; }
    void watchSlot(EssenceConverterSlot slot) { openSlots.add(slot); }
    void unwatchSlot(EssenceConverterSlot slot) { openSlots.remove(slot); }
    public void toggleMode() {
        if (storageAvailable() && level instanceof ServerLevel) {
            level.setBlock(worldPosition, getBlockState().setValue(EssenceConverterBlock.UPGRADE, !upgrading()), 3);
            setChanged(); wake();
        }
    }
    private void wake() {
        if (network != null) network.changed();
        if (level instanceof ServerLevel server && !isRemoved() && !scheduled) {
            scheduled = true;
            server.scheduleTick(worldPosition, getBlockState().getBlock(), 5);
        }
    }
    @Override public void onLoad() { unloaded = false; super.onLoad(); if (network != null) network.start(); wake(); }
    @Override public void onChunkUnloaded() { unloaded = true; scheduled = false; if (network != null) network.stop(); super.onChunkUnloaded(); }
    @Override public void setRemoved() { if (network != null) network.stop(); super.setRemoved(); }
    public boolean hasExport() {
        int slot = target();
        return !quarantined() && inventory.getResource(slot).equals(ItemResource.of(essence(slot))) && inventory.getAmountAsLong(slot) > reserve(slot);
    }
    /** One bounded offer. Reserve the source before AE2 callbacks can reach any converter capability. */
    public int exportResult(java.util.function.ToLongBiFunction<ItemResource, Integer> sink) {
        if (!meEnabled || !storageAvailable() || working || !hasExport() || Transaction.getCurrentOpenedTransaction() != null) return 0;
        int slot = target(); long original = inventory.getAmountAsLong(slot); int offered = (int)Math.min(64L, original - reserve(slot));
        var resource = inventory.getResource(slot);
        var ops = com.neolena.essenceindustry.library.SeedDataSafety.ops(level.registryAccess());
        var pending = new com.mojang.serialization.Dynamic<>(ops, ItemStack.CODEC.encodeStart(ops, resource.toStack(offered)).getOrThrow());
        exporting = true; working = true;
        try {
            try (var tx = Transaction.openRoot()) {
                if (inventory.extract(slot, resource, offered, tx) != offered) return 0;
                tx.commit();
            }
            exportRecovery = pending;
            setChanged();
            long accepted = sink.applyAsLong(resource, offered);
            if (accepted < 0 || accepted > offered) throw new IllegalStateException("Invalid ME insertion result " + accepted);
            long remaining = inventory.getAmountAsLong(slot);
            if (remaining != original - offered || (remaining == 0 ? !inventory.getResource(slot).isEmpty() : !inventory.getResource(slot).equals(resource)))
                throw new IllegalStateException("Reserved ME export source changed during insertion");
            int remainder = offered - (int)accepted;
            if (remainder > 0) {
                boolean restored = false;
                try (var tx = Transaction.openRoot()) {
                    if (inventory.insert(slot, resource, remainder, tx) == remainder) { tx.commit(); restored = true; }
                }
                // Legacy saves can contain stacks above today's capacity. Restoring
                // our own reserved source must preserve that count, not reject it as
                // new input. The snapshot check above prevents overwriting a change.
                if (!restored) inventory.set(slot, resource, original - (int)accepted);
            }
            exportRecovery = null; setChanged(); if (accepted > 0) wake();
            return (int)accepted;
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(EssenceConverterTile.class).error("Converter at {} locked after ambiguous ME export; original offered items retained as recovery data", worldPosition, error);
            setChanged(); return 0;
        } finally { working = false; exporting = false; if(network!=null)network.changed(); }
    }
    /** Bounded pull; lock every public capability before invoking nontransactional ME storage. */
    public int importEssence(int tier, java.util.function.ToLongBiFunction<ItemResource,Integer> source) {
        if (!meEnabled || !storageAvailable() || working || tier < 0 || tier > highest() || tier == target()
                || Transaction.getCurrentOpenedTransaction() != null) return 0;
        var resource = ItemResource.of(essence(tier));
        if (!inventory.getResource(tier).isEmpty() && !inventory.getResource(tier).equals(resource)) return 0;
        long original = inventory.getAmountAsLong(tier);
        int offered = (int)Math.min(64L, Math.max(0L, inventory.getCapacityAsLong(tier, resource) - original));
        if (offered == 0) return 0;
        var ops = com.neolena.essenceindustry.library.SeedDataSafety.ops(level.registryAccess());
        exporting = working = true;
        // An exception from a third-party extraction has an unknown outcome. Never retry it.
        importRecovery = new com.mojang.serialization.Dynamic<>(ops, ItemStack.CODEC.encodeStart(ops, resource.toStack(offered)).getOrThrow());
        try {
            setChanged();
            long taken = source.applyAsLong(resource, offered);
            if (taken < 0 || taken > offered) throw new IllegalStateException("Invalid ME extraction result " + taken);
            if (inventory.getAmountAsLong(tier) != original) throw new IllegalStateException("Import destination changed during extraction");
            if (taken > 0) {
                importRecovery = new com.mojang.serialization.Dynamic<>(ops, ItemStack.CODEC.encodeStart(ops, resource.toStack((int)taken)).getOrThrow());
                try (var tx = Transaction.openRoot()) {
                    if (inventory.insert(tier, resource, (int)taken, tx) != taken) throw new IllegalStateException("Reserved import space refused items");
                    tx.commit();
                }
            }
            importRecovery = null; setChanged();
            return (int)taken;
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(EssenceConverterTile.class).error("Converter at {} locked after ambiguous ME import; request retained for reconciliation", worldPosition, error);
            setChanged(); return 0;
        } finally { exporting = working = false; if (!quarantined()) wake(); }
    }
    private int convertible(int from, int to) {
        if (!inventory.getResource(from).equals(ItemResource.of(essence(from)))) return 0;
        var target = inventory.getResource(to);
        if (!target.isEmpty() && !target.equals(ItemResource.of(essence(to)))) return 0;
        long source = Math.max(0L, inventory.getAmountAsLong(from) - reserve(from));
        long space = Math.max(0L, inventory.getCapacityAsLong(to, ItemResource.of(essence(to))) - inventory.getAmountAsLong(to));
        return (int)Math.min(Integer.MAX_VALUE / 4L, upgrading() ? Math.min(source / 4, space) : Math.min(source, space / 4));
    }
    /** At most five tier moves per scheduled batch, with transactional backpressure. */
    public void work() {
        scheduled = false;
        if (!storageAvailable()) return;
        working = true;
        try {
            for (int step = 0; step < highest(); step++) {
                int from = upgrading() ? step : highest() - step;
                int to = upgrading() ? from + 1 : from - 1;
                int batches = convertible(from, to);
                if (batches <= 0) continue;
                try (var transaction = Transaction.openRoot()) {
                    int taken = upgrading() ? batches * 4 : batches;
                    int made = upgrading() ? batches : batches * 4;
                    var input = inventory.getResource(from);
                    if (inventory.extract(from, input, taken, transaction) == taken
                            && inventory.insert(to, ItemResource.of(essence(to)), made, transaction) == made) transaction.commit();
                }
            }
        } finally { working = false; if (network != null) network.changed(); }
        for (int step = 0; step < highest(); step++) {
            int from = upgrading() ? step : highest() - step;
            if (convertible(from, upgrading() ? from + 1 : from - 1) > 0) { wake(); break; }
        }
    }
    @Override protected void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        data.store("stacks", com.mojang.serialization.Codec.PASSTHROUGH, inventoryData());
        data.store("reserves", com.mojang.serialization.Codec.PASSTHROUGH, reserveData());
        data.putBoolean("meExport", meEnabled);
        if (exportRecovery != null) data.store("meExportPending", com.mojang.serialization.Codec.PASSTHROUGH, exportRecovery);
        if (importRecovery != null) data.store("meImportPending", com.mojang.serialization.Codec.PASSTHROUGH, importRecovery);
        if (network != null) network.save(data); else if (nodeRecovery != null) data.store("meNode", com.mojang.serialization.Codec.PASSTHROUGH, nodeRecovery);
    }
    private com.mojang.serialization.Dynamic<?> reserveData() {
        if (reserveRecovery != null) return reserveRecovery;
        var lookup = level == null ? com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup() : level.registryAccess();
        var ops = com.neolena.essenceindustry.library.SeedDataSafety.ops(lookup);
        return new com.mojang.serialization.Dynamic<>(ops, com.mojang.serialization.Codec.INT.listOf().encodeStart(ops, java.util.Arrays.stream(reserves).boxed().toList()).getOrThrow());
    }
    private void loadReserves(com.mojang.serialization.Dynamic<?> raw) {
        reserveRecovery = raw;
        if (!com.neolena.essenceindustry.item.InventoryRecovery.safe(raw)) return;
        var values = com.mojang.serialization.Codec.INT.listOf().parse(raw);
        if (values.error().isPresent()) return;
        var list = values.result().orElseThrow();
        if (list.size() != TIERS || list.stream().anyMatch(value -> value < 0 || value > 64)) return;
        for (int i = 0; i < TIERS; i++) reserves[i] = list.get(i);
        reserveRecovery = null;
    }
    private com.mojang.serialization.Dynamic<?> inventoryData() {
        if (recovery != null) return recovery;
        var lookup = level == null ? com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup() : level.registryAccess();
        var ops = com.neolena.essenceindustry.library.SeedDataSafety.ops(lookup);
        return new com.mojang.serialization.Dynamic<>(ops, EssenceStorage.Entry.CODEC.listOf().encodeStart(ops, inventory.entries()).getOrThrow());
    }
    private boolean validateSaved(com.mojang.serialization.Dynamic<?> raw) {
        recovery = raw;
        if (!com.neolena.essenceindustry.item.InventoryRecovery.safe(raw)) return false;
        try {
            var bulk = EssenceStorage.Entry.CODEC.listOf().parse(raw);
            if (bulk.result().isPresent()) inventory.load(bulk.result().orElseThrow());
            else {
                // Older releases saved normal ItemStacks. Migrate without truncating counts.
                var legacy = ItemStack.OPTIONAL_CODEC.listOf().parse(raw).getOrThrow();
                if (legacy.size() != TIERS) return false;
                inventory.load(legacy.stream().map(stack -> new EssenceStorage.Entry(stack.copyWithCount(stack.isEmpty()?0:1), stack.getCount())).toList());
            }
            recovery = null; return true;
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(EssenceConverterTile.class).error("Converter at {} has unreadable inventory; original data retained and transfers locked", worldPosition);
            return false;
        }
    }
    @Override protected void loadAdditional(ValueInput data) {
        super.loadAdditional(data);
        meEnabled = data.getBooleanOr("meExport", false);
        exportRecovery = data.read("meExportPending", com.mojang.serialization.Codec.PASSTHROUGH).orElse(null);
        importRecovery = data.read("meImportPending", com.mojang.serialization.Codec.PASSTHROUGH).orElse(null);
        nodeRecovery = data.read("meNode", com.mojang.serialization.Codec.PASSTHROUGH).orElse(null);
        if (network != null) { network.load(data); network.settingsChanged(); }
        java.util.Arrays.fill(reserves, 0); reserveRecovery = null;
        data.read("reserves", com.mojang.serialization.Codec.PASSTHROUGH).ifPresent(this::loadReserves);
        var raw = data.read("stacks", com.mojang.serialization.Codec.PASSTHROUGH);
        raw.ifPresent(this::validateSaved);
    }
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder data) {
        super.collectImplicitComponents(data);
        com.neolena.essenceindustry.item.InventoryRecovery.collect(data, "stacks", inventoryData());
        var tag = new net.minecraft.nbt.CompoundTag(); tag.put("reserves", reserveData().convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue().copy());
        tag.putBoolean("meExport", meEnabled);
        if (exportRecovery != null) tag.put("meExportPending", exportRecovery.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue().copy());
        if (importRecovery != null) tag.put("meImportPending", importRecovery.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue().copy());
        data.set(EssenceConverter.RESERVES.get(), net.minecraft.world.item.component.CustomData.of(tag));
    }
    @Override protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter data) {
        super.applyImplicitComponents(data);
        var settings = data.get(EssenceConverter.RESERVES.get());
        if (settings != null) {
            var settingsTag = settings.copyTag();
            meEnabled = settingsTag.getBooleanOr("meExport", false);
            var imported = settingsTag.get("meImportPending");
            importRecovery = imported == null ? null : new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, imported.copy());
            var pending = settingsTag.get("meExportPending");
            exportRecovery = pending == null ? null : new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, pending.copy());
            if (network != null) network.settingsChanged();
            var rawSettings = settings.copyTag().get("reserves");
            // Missing/invalid fields in an explicitly stored settings component must not enable conversion.
            loadReserves(new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, rawSettings == null ? new net.minecraft.nbt.CompoundTag() : rawSettings));
        }
        var raw = com.neolena.essenceindustry.item.InventoryRecovery.read(data, "stacks", level == null ? com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup() : level.registryAccess());
        if (raw != null) validateSaved(raw);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // Contents travel in the block item. Never spawn one entity per stack of bulk stock.
        super.preRemoveSideEffects(pos, state);
    }
}
