package com.neolena.essenceindustry.library;

import java.util.Comparator;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Shared UI and automation ordering, evaluated once per exact seed variant. */
public record SeedRank(ItemResource seed, SeedTraits traits, String tie) {
    private static long revision;
    public static long revision() { return revision; }
    static void register() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) revision++;
        });
    }
    public static final Comparator<SeedRank> BEST_FIRST = Comparator
            .comparingLong((SeedRank r) -> r.traits().production()).reversed()
            .thenComparing(Comparator.comparingInt((SeedRank r) -> r.traits().yield()).reversed())
            .thenComparing(Comparator.comparingInt((SeedRank r) -> r.traits().growth()).reversed())
            .thenComparing(Comparator.comparingInt((SeedRank r) -> r.traits().resistance()).reversed())
            .thenComparing(Comparator.comparingInt((SeedRank r) -> r.traits().mutability()).reversed())
            .thenComparing(SeedRank::tie);

    public static SeedRank of(ItemResource seed) {
        return new SeedRank(seed, SeedTraits.read(seed.toStack(1)), seed.getComponentsPatch().toString());
    }
}
