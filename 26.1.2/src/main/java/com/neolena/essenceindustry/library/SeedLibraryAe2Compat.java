package com.neolena.essenceindustry.library;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.me.storage.ITickingMonitor;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** AE2's native storage capability bypasses its per-slot resource-handler scans. */
public final class SeedLibraryAe2Compat {
    public static void register(IEventBus bus) { bus.addListener(SeedLibraryAe2Compat::capabilities); }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.ME_STORAGE, SeedLibrary.TILE.get(), (tile, side) -> {
            if (!(tile.ae2Storage instanceof Bridge)) tile.ae2Storage = new Bridge(tile);
            return ((Bridge)tile.ae2Storage).views[side == null ? 6 : side.ordinal()];
        });
    }
    private static final class Bridge {
        final SeedLibraryTile tile;
        final Map<ItemResource, AEItemKey> keys = new HashMap<>();
        private record Candidate(SeedRank rank, long order) { ItemResource seed() { return rank.seed(); } }
        long nextOrder;
        final Map<ItemResource, Candidate> ranks = new HashMap<>();
        final Map<Item, NavigableSet<Candidate>> crops = new IdentityHashMap<>();
        final Map<Item, ItemResource> best = new IdentityHashMap<>();
        final KeyCounter available = new KeyCounter();
        final View[] views = new View[7];
        boolean initialized;
        long rankingRevision = -1;
        Bridge(SeedLibraryTile tile) {
            this.tile = tile;
            ensureCache();
            tile.vault().addListener(this::changed);
            for (int i = 0; i < views.length; i++) views[i] = new View(this);
        }
        boolean ensureCache() {
            if (initialized && rankingRevision == SeedRank.revision()) return true;
            if (Transaction.getCurrentOpenedTransaction() != null) return false;
            rebuild(); return true;
        }
        void rebuild() {
            initialized = true;
            rankingRevision = SeedRank.revision();
            keys.clear(); ranks.clear(); crops.clear(); best.clear(); available.clear();
            for (var record : tile.vault().records()) {
                add(record.seed());
            }
            for (var item : crops.keySet()) refresh(item);
        }
        void add(ItemResource seed) {
            var rank = new Candidate(SeedRank.of(seed), nextOrder++);
            ranks.put(seed, rank); keys.put(seed, AEItemKey.of(seed));
            crops.computeIfAbsent(seed.getItem(), item -> new TreeSet<>(Comparator.comparing(Candidate::rank, SeedRank.BEST_FIRST).thenComparingLong(Candidate::order))).add(rank);
        }
        void refresh(Item item) {
            var previous = best.remove(item);
            if (previous != null) {
                var key = keys.get(previous);
                if (key != null) available.remove(key);
            }
            var variants = crops.get(item);
            if (variants == null) return;
            // Root-commit callbacks can arrive in either order for a multi-variant transfer.
            // Never advertise an exhausted winner while another callback is still pending.
            while (!variants.isEmpty() && tile.vault().count(variants.first().seed()) == 0) variants.pollFirst();
            if (variants.isEmpty()) { crops.remove(item); return; }
            var seed = variants.first().seed();
            best.put(item, seed); available.set(keys.get(seed), tile.vault().count(seed));
        }
        boolean extractable(ItemResource seed) {
            return ensureCache() && seed.equals(best.get(seed.getItem())) && tile.vault().count(seed) > 0;
        }
        void changed(ItemResource seed) {
            if (!initialized) { ensureCache(); return; }
            if (seed.isEmpty()) { rebuild(); return; }
            long count = tile.vault().count(seed);
            if (count == 0) {
                var rank = ranks.remove(seed);
                var variants = crops.get(seed.getItem());
                if (rank != null && variants != null) variants.remove(rank);
                refresh(seed.getItem()); keys.remove(seed);
            } else if (!ranks.containsKey(seed)) { add(seed); refresh(seed.getItem()); }
            else if (seed.equals(best.get(seed.getItem()))) available.set(keys.get(seed), count);
        }
        boolean valid() { return tile.storageAvailable(); }
    }
    /** Each physical bus gets its own revision cursor; the inventory cache is shared. */
    private static final class View implements MEStorage, ITickingMonitor {
        final Bridge bridge;
        long polledRevision = Long.MIN_VALUE;
        long polledRanking = Long.MIN_VALUE;
        View(Bridge bridge) { this.bridge = bridge; }
        @Override public Component getDescription() { return Component.translatable("block.essenceindustry.seed_library"); }
        @Override public int getEstimatedStackCount() { return bridge.ensureCache() ? bridge.available.size() : 0; }
        @Override public boolean isPreferredStorageFor(AEKey key, IActionSource source) {
            return key instanceof AEItemKey item && bridge.valid() && bridge.tile.vault().count(item.toResource()) > 0;
        }
        @Override public void getAvailableStacks(KeyCounter out) { if (bridge.valid() && bridge.ensureCache()) out.addAll(bridge.available); }
        @Override public TickRateModulation onTick() {
            // No iteration, item copies, transaction simulations or allocations.
            long revision = bridge.tile.revision();
            long ranking = SeedRank.revision();
            if (revision == polledRevision && ranking == polledRanking) return TickRateModulation.SLOWER;
            polledRevision = revision; polledRanking = ranking; return TickRateModulation.URGENT;
        }
        @Override public long insert(AEKey key, long amount, Actionable mode, IActionSource source) { return transfer(key, amount, mode, true); }
        @Override public long extract(AEKey key, long amount, Actionable mode, IActionSource source) { return transfer(key, amount, mode, false); }
        private long transfer(AEKey key, long amount, Actionable mode, boolean insert) {
            if (amount < 0) throw new IllegalArgumentException("Negative transfer amount");
            if (amount == 0 || !(key instanceof AEItemKey item) || !bridge.valid()) return 0;
            var seed = item.toResource(); var vault = bridge.tile.vault();
            if (insert && !SeedVault.isSeed(seed)) return 0;
            if (!insert && !bridge.extractable(seed)) return 0;
            int offered = (int)Math.min(amount, Integer.MAX_VALUE);
            if (mode == Actionable.SIMULATE) {
                if (!insert || vault.count(seed) > 0) return Math.min((long)offered, insert ? Long.MAX_VALUE - vault.count(seed) : vault.count(seed));
                // Admission includes exact persistence and the metadata budget.
                try (var tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) { return vault.insert(seed, offered, tx); }
            }
            try (var tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                int moved = insert ? vault.insert(seed, offered, tx) : vault.extract(seed, offered, tx);
                tx.commit(); return moved;
            }
        }
    }
    private SeedLibraryAe2Compat() {}
}

