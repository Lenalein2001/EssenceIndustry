package com.neolena.essenceindustry.compat.ae2.pattern;

import appeng.api.stacks.AEItemKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.function.ToLongBiFunction;
import net.minecraft.world.item.ItemStack;

/** One owner, bounded metadata and checked counts. No inventory capability exposes pending obligations. */
public final class PatternOutputQueue {
    public static final int MAX_KEYS = 128;
    public static final long MAX_PER_KEY = 1_000_000_000L;
    public record Saved(ItemStack sample, long amount) {
        public static final Codec<Saved> CODEC = RecordCodecBuilder.create(i -> i.group(ItemStack.CODEC.fieldOf("item").forGetter(Saved::sample), Codec.LONG.validate(n -> n > 0 && n <= MAX_PER_KEY ? com.mojang.serialization.DataResult.success(n) : com.mojang.serialization.DataResult.error(() -> "Invalid pending count")).fieldOf("amount").forGetter(Saved::amount)).apply(i, Saved::new));
    }
    private final LinkedHashMap<AEItemKey, Long> pending = new LinkedHashMap<>();
    private boolean locked, transferring;
    public boolean locked() { return locked; }
    public boolean transferring() { return transferring; }
    public void lock() { locked = true; }
    public boolean empty() { return pending.isEmpty(); }
    public long count(AEItemKey key) { return pending.getOrDefault(key, 0L); }
    public int keys() { return pending.size(); }
    public long total() { long count = 0; for (long amount : pending.values()) count += amount; return count; }
    public boolean canAdd(AEItemKey key, long amount) {
        return !locked && !transferring && key != null && amount > 0 && amount <= MAX_PER_KEY
                && (pending.containsKey(key) || pending.size() < MAX_KEYS) && count(key) <= MAX_PER_KEY - amount;
    }
    public boolean add(AEItemKey key, long amount) {
        if (!canAdd(key, amount)) return false;
        pending.put(key, count(key) + amount); return true;
    }
    public List<Saved> save() { return pending.entrySet().stream().map(e -> new Saved(e.getKey().toStack(), e.getValue())).toList(); }
    public void load(List<Saved> records) {
        if (transferring) throw new IllegalStateException("Cannot replace an active output queue");
        var next = new LinkedHashMap<AEItemKey, Long>();
        if (records.size() > MAX_KEYS) throw new IllegalArgumentException("Too many pending output keys");
        for (var record : records) {
            if (record.sample().isEmpty() || record.sample().getCount() != 1 || record.amount() <= 0 || record.amount() > MAX_PER_KEY) throw new IllegalArgumentException("Invalid output obligation");
            var key = AEItemKey.of(record.sample());
            if (next.putIfAbsent(key, record.amount()) != null) throw new IllegalArgumentException("Duplicate output obligation");
        }
        pending.clear(); pending.putAll(next);
    }
    /** Exceptions/invalid return counts are ambiguous: retain the obligation and quarantine instead of replaying. */
    public int flush(ToLongBiFunction<AEItemKey, Long> sink, int limit) {
        if (locked || transferring) return 0;
        transferring = true; int attempts = 0;
        try {
            // Visit each candidate at most once per batch. A full destination for the
            // first few keys must not starve later keys that ME can still accept.
            int candidates = Math.min(Math.max(0, limit), pending.size());
            while (attempts < candidates) {
                var entry = pending.firstEntry(); long amount = entry.getValue(); attempts++;
                long accepted;
                try { accepted = sink.applyAsLong(entry.getKey(), amount); }
                catch (RuntimeException error) { locked = true; throw error; }
                if (accepted < 0 || accepted > amount) { locked = true; throw new IllegalStateException("Invalid ME insertion count"); }
                // Keep ownership until the sink reports acceptance; save/reentrant
                // callbacks and ambiguous failures must still see the obligation.
                if (accepted == amount) pending.remove(entry.getKey());
                else pending.putLast(entry.getKey(), amount - accepted);
            }
        } finally { transferring = false; }
        return attempts;
    }
}
