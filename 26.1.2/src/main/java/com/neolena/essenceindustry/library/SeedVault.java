package com.neolena.essenceindustry.library;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.*;

/** Exact item-and-component keys; normal item stack sizes only apply on withdrawal. */
public final class SeedVault implements ResourceHandler<ItemResource> {
    public record Record(ItemResource seed, long count) {
        public static final Codec<Record> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemResource.CODEC.fieldOf("seed").forGetter(Record::seed),
                Codec.LONG.validate(count -> count > 0 ? com.mojang.serialization.DataResult.success(count) : com.mojang.serialization.DataResult.error(() -> "Seed count must be positive")).fieldOf("count").forGetter(Record::count)).apply(i, Record::new));
    }
    private final Map<ItemResource, Counter> bySeed = new HashMap<>();
    private final List<Counter> counters = new ArrayList<>();
    private final Runnable changed;
    private final List<java.util.function.Consumer<ItemResource>> listeners = new ArrayList<>();
    public void addListener(java.util.function.Consumer<ItemResource> listener) { listeners.add(listener); }
    private long variantRevision;
    private long dataWeight;
    private final java.util.function.Supplier<net.minecraft.core.HolderLookup.Provider> lookup;
    public long dataWeight() { return dataWeight; }
    public boolean safeToMove() { return dataWeight <= SeedDataSafety.BUDGET; }
    public long variantRevision() { return variantRevision; }
    private final SnapshotJournal<Integer> structure = new SnapshotJournal<>() {
        @Override protected Integer createSnapshot() { return counters.size(); }
        @Override protected void revertToSnapshot(Integer size) {
            while (counters.size() > size) { var removed = counters.removeLast(); bySeed.remove(removed.seed); dataWeight -= removed.weight; }
        }
    };
    private final class Counter extends SnapshotJournal<Long> {
        final ItemResource seed;
        long count;
        int index;
        final long weight;
        Counter(ItemResource seed, long count, long weight) { this.seed = seed; this.count = count; this.index = counters.size(); this.weight = weight; }
        @Override protected Long createSnapshot() { return count; }
        @Override protected void revertToSnapshot(Long value) { count = value; }
        @Override protected void onRootCommit(Long previous) {
            if (previous == count && count != 0) return;
            if (previous != count && (previous == 0 || count == 0)) variantRevision++;
            // Prune only after the root commits. Rollback never moves indexed slots.
            // Swap removal keeps future bus scans proportional to live variants.
            if (count == 0 && bySeed.get(seed) == this) {
                bySeed.remove(seed); dataWeight -= weight;
                var last = counters.removeLast();
                if (last != this) { counters.set(index, last); last.index = index; }
            }
            if (previous != count) {
                changed.run();
                for (var listener : listeners) listener.accept(seed);
            }
        }
    }
    public SeedVault(Runnable changed) { this(changed, SeedDataSafety::builtinLookup); }
    public SeedVault(Runnable changed, java.util.function.Supplier<net.minecraft.core.HolderLookup.Provider> lookup) { this.changed = changed; this.lookup = lookup; }
    private static final class SeedItems {
        static final Set<Item> ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
        static { CropRegistry.getInstance().getCrops().forEach(crop -> ITEMS.add(crop.getSeedsItem())); }
    }
    public static boolean isSeed(ItemResource resource) {
        if (resource.isEmpty()) return false;
        // Provider namespace, item class and tag membership are not assumptions.
        return SeedItems.ITEMS.contains(resource.getItem());
    }
    public List<Record> records() {
        return counters.stream().filter(c -> c.count > 0).map(c -> new Record(c.seed, c.count)).toList();
    }
    public long count(ItemResource resource) { var counter = bySeed.get(resource); return counter == null ? 0 : counter.count; }
    public void load(List<Record> records) {
        // Validate/aggregate first; an invalid load must not clear existing storage.
        var aggregate = new java.util.LinkedHashMap<ItemResource, Long>();
        for (var record : records) {
            if (record.seed().isEmpty() || record.count() <= 0) throw new IllegalArgumentException("Invalid saved seed record");
            aggregate.merge(record.seed(), record.count(), Math::addExact);
        }
        counters.clear(); bySeed.clear(); dataWeight = 0; variantRevision++;
        records = aggregate.entrySet().stream().map(entry -> new Record(entry.getKey(), entry.getValue())).toList();
        for (var record : records) {
            // Preserve existing persisted records even if a crop is later disabled.
            var existing = bySeed.get(record.seed());
            if (existing == null) {
                long weight = SeedDataSafety.weight(record.seed(), lookup.get());
                if (weight < 0) weight = SeedDataSafety.BUDGET + 1;
                var counter = new Counter(record.seed(), record.count(), weight); dataWeight += weight;
                bySeed.put(record.seed(), counter); counters.add(counter);
            } else existing.count = Math.addExact(existing.count, record.count());
        }
        for (var listener : listeners) listener.accept(ItemResource.EMPTY);
    }
    // One append index plus live variants. Previously cached indices may move after a commit;
    // indexed transfers always check the full resource identity before touching counts.
    @Override public int size() { return counters.size() + 1; }
    @Override public ItemResource getResource(int index) { return index >= 0 && index < counters.size() && counters.get(index).count > 0 ? counters.get(index).seed : ItemResource.EMPTY; }
    @Override public long getAmountAsLong(int index) { return index >= 0 && index < counters.size() ? counters.get(index).count : 0; }
    @Override public long getCapacityAsLong(int index, ItemResource resource) { return resource.isEmpty() || isValid(index, resource) ? Long.MAX_VALUE : 0; }
    @Override public boolean isValid(int index, ItemResource resource) { return index >= 0 && index <= counters.size() && isSeed(resource); }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!isValid(index, resource) || (index < counters.size() && !counters.get(index).seed.equals(resource))) return 0;
        return insert(resource, amount, transaction);
    }
    @Override public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !isSeed(resource)) return 0;
        var counter = bySeed.get(resource);
        if (counter == null) {
            long weight = SeedDataSafety.weight(resource, lookup.get());
            if (weight <= 0 || weight > SeedDataSafety.BUDGET - dataWeight) return 0;
            structure.updateSnapshots(transaction);
            counter = new Counter(resource, 0, weight); counters.add(counter); bySeed.put(resource, counter); dataWeight += weight;
        }
        int accepted = (int)Math.min((long)amount, Long.MAX_VALUE - counter.count);
        if (accepted > 0) { counter.updateSnapshots(transaction); counter.count += accepted; }
        return accepted;
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index < 0 || index >= counters.size() || !counters.get(index).seed.equals(resource)) return 0;
        return extract(resource, amount, transaction);
    }
    @Override public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var counter = bySeed.get(resource);
        if (counter == null || amount == 0) return 0;
        int taken = (int)Math.min((long)amount, counter.count);
        if (taken > 0) { counter.updateSnapshots(transaction); counter.count -= taken; }
        return taken;
    }
}
