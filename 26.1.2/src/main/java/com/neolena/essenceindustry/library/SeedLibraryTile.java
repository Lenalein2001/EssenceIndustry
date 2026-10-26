package com.neolena.essenceindustry.library;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class SeedLibraryTile extends BlockEntity {
    Object ae2Storage; // Keep optional AE2 classes out of common-side field signatures.
    private long revision;
    private boolean unloaded;
    private Dynamic<?> recovery;
    private final SeedVault vault = new SeedVault(() -> { revision++; setChanged(); }, () -> level == null ? SeedDataSafety.builtinLookup() : level.registryAccess());
    private final net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> automation = new com.neolena.essenceindustry.item.GuardedItemStorage(vault, this::storageAvailable);
    public SeedLibraryTile(BlockPos pos, BlockState state) { super(SeedLibrary.TILE.get(), pos, state); }
    public boolean present() { return !unloaded && !isRemoved() && level instanceof net.minecraft.server.level.ServerLevel server && server.isLoaded(worldPosition) && server.getBlockEntity(worldPosition) == this; }
    public boolean storageAvailable() { return present() && recovery == null; }
    public boolean quarantined() { return recovery != null; }
    public boolean safeToMove() { return recovery == null ? vault.safeToMove() : SeedDataSafety.tagWeight(recovery.convert(NbtOps.INSTANCE).getValue()) <= SeedDataSafety.BUDGET; }
    @Override public void onLoad() { unloaded = false; super.onLoad(); }
    @Override public void onChunkUnloaded() { unloaded = true; super.onChunkUnloaded(); }
    public SeedVault vault() { return vault; }
    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> automation() { return automation; }
    public long revision() { return revision; }
    private void loadRaw(Dynamic<?> raw) {
        try {
            var decoded = SeedVault.Record.CODEC.listOf().parse(raw);
            if (decoded.error().isEmpty()) { vault.load(decoded.result().orElseThrow()); recovery = null; revision++; return; }
        } catch (RuntimeException ignored) { /* Keep the original data, not an empty replacement. */ }
        recovery = raw; revision++;
        org.slf4j.LoggerFactory.getLogger(SeedLibraryTile.class).error("Seed Library at {} has unreadable seed data; original data retained and transfers locked", worldPosition);
    }
    @Override protected void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        if (recovery == null) data.store("seeds", SeedVault.Record.CODEC.listOf(), vault.records());
        else data.store("seeds", Codec.PASSTHROUGH, recovery);
    }
    @Override protected void loadAdditional(ValueInput data) {
        super.loadAdditional(data);
        var raw = data.read("seeds", Codec.PASSTHROUGH);
        if (raw.isPresent()) loadRaw(raw.get());
        else { vault.load(List.of()); recovery = null; revision++; }
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(SeedLibrary.CONTENTS.get(), null); components.set(SeedLibrary.RECOVERY.get(), null);
        if (recovery == null) components.set(SeedLibrary.CONTENTS.get(), vault.records());
        else {
            var tag = new CompoundTag(); tag.put("seeds", recovery.convert(NbtOps.INSTANCE).getValue().copy());
            components.set(SeedLibrary.RECOVERY.get(), CustomData.of(tag));
        }
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        var raw = components.get(SeedLibrary.RECOVERY.get());
        if (raw != null && raw.copyTag().get("seeds") instanceof Tag tag) {
            loadRaw(new Dynamic<>(SeedDataSafety.ops(level == null ? SeedDataSafety.builtinLookup() : level.registryAccess()), tag));
        } else { vault.load(components.getOrDefault(SeedLibrary.CONTENTS.get(), List.of())); recovery = null; revision++; }
    }
    @Override public void removeComponentsFromTag(ValueOutput data) { super.removeComponentsFromTag(data); data.discard("seeds"); }
}
