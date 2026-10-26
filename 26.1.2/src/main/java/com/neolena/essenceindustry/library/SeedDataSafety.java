package com.neolena.essenceindustry.library;

import com.mojang.serialization.DynamicOps;
import java.io.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Reserve ample room below the game's two-MiB NBT accounting limit for a filled block item. */
public final class SeedDataSafety {
    public static final long BUDGET = 1_048_576;
    private static final class BuiltinLookup { static final HolderLookup.Provider VALUE = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
    public static HolderLookup.Provider builtinLookup() { return BuiltinLookup.VALUE; }
    public static DynamicOps<Tag> ops(HolderLookup.Provider lookup) { return RegistryOps.create(NbtOps.INSTANCE, lookup); }
    public static long tagWeight(Tag tag) {
        try {
            var bytes = new ByteArrayOutputStream();
            NbtIo.writeAnyTag(tag, new DataOutputStream(bytes));
            if (bytes.size() > BUDGET) return BUDGET + 1;
            var accounting = new NbtAccounter(BUDGET, 256);
            NbtIo.readAnyTag(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())), accounting);
            return Math.max(bytes.size(), accounting.getUsage()) + 64;
        } catch (IOException | RuntimeException exception) { return BUDGET + 1; }
    }
    public static long weight(ItemResource seed, HolderLookup.Provider lookup) {
        try {
            var ops = ops(lookup);
            var encoded = SeedVault.Record.CODEC.encodeStart(ops, new SeedVault.Record(seed, Long.MAX_VALUE));
            if (encoded.error().isPresent()) return -1;
            Tag tag = encoded.result().orElseThrow();
            var decoded = SeedVault.Record.CODEC.parse(ops, tag);
            if (decoded.error().isPresent() || !decoded.result().orElseThrow().seed().equals(seed)) return -1;
            return tagWeight(tag);
        } catch (RuntimeException exception) { return -1; }
    }
    private SeedDataSafety() {}
}
