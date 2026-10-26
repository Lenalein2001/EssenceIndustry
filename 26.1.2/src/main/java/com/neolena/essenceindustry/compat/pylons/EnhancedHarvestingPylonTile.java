package com.neolena.essenceindustry.compat.pylons;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import net.neoforged.neoforge.common.ticket.AABBTicket;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.permutated.pylons.ModRegistry;
import net.permutated.pylons.compat.harvest.HarvestCompat;
import net.permutated.pylons.machines.base.AbstractPylonTile;

/** Uses Pylons' public crop integrations, with bounded, rotating work and retained overflow. */
public final class EnhancedHarvestingPylonTile extends AbstractPylonTile {
    private final List<ItemStack> pending = new ArrayList<>();
    private com.mojang.serialization.Dynamic<?> recovery, nodeRecovery;
    private boolean unloaded, meEnabled, transferring;
    private final PylonNetwork network = EnhancedHarvestingPylon.networkFactory == null ? null : EnhancedHarvestingPylon.networkFactory.apply(this);
    public PylonNetwork network() { return network; }
    public boolean meEnabled() { return meEnabled; }
    public int meStatus() { return quarantined() ? 4 : network == null ? 3 : network.status(); }
    public void toggleMe() { if (network != null && !quarantined() && !transferring && !isRemoved()) { meEnabled = !meEnabled; setChanged(); network.settingsChanged(); if (level != null) level.invalidateCapabilities(worldPosition); } }
    public boolean quarantined() { return recovery != null; }
    public boolean safeToMove() { return !transferring && com.neolena.essenceindustry.item.InventoryRecovery.safe(recovery); }
    private final net.neoforged.neoforge.transfer.transaction.SnapshotJournal<List<ItemStack>> pendingJournal = new net.neoforged.neoforge.transfer.transaction.SnapshotJournal<>() {
        @Override protected List<ItemStack> createSnapshot() { return List.copyOf(pending); }
        @Override protected void revertToSnapshot(List<ItemStack> snapshot) { pending.clear(); pending.addAll(snapshot); }
        @Override protected void onRootCommit(List<ItemStack> previous) { setChanged(); }
    };
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> output;
    private int cursor;
    private AABBTicket waterTicket;

    public EnhancedHarvestingPylonTile(BlockPos pos, BlockState state) { super(EnhancedHarvestingPylon.TILE.get(), pos, state); }
    @Override protected byte[] getRange() { return new byte[]{3, 5, 7, 9, 13, 17, 25, 33}; }
    @Override protected boolean isItemValid(ItemResource item) { return false; }
    @Override protected boolean canAccessInventory() { return false; }
    @Override public void removeChunkloads() {}

    // Watering is passive, including when harvesting is disabled or its output is full.
    // A water ticket lets farmland's own random tick hydrate it without a second field scan.
    private void refreshWaterTicket() {
        if (!(level instanceof ServerLevel) || isRemoved()) return;
        int radius = getSelectedRange() / 2;
        // Cover both normal placement above soil and a pylon recessed into the soil.
        // Recessed placement must not require actual water/waterlogging to hydrate its neighbors.
        int soilY = worldPosition.getY() - 1;
        var bounds = new AABB(worldPosition.getX() - radius, soilY, worldPosition.getZ() - radius,
                worldPosition.getX() + radius + 1, soilY + 2, worldPosition.getZ() + radius + 1);
        if (waterTicket != null && waterTicket.axisAlignedBB.equals(bounds)) {
            if (!waterTicket.isValid()) waterTicket.validate();
            return;
        }
        clearWaterTicket();
        var master = new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4);
        var others = new ArrayList<ChunkPos>();
        for (int x = (worldPosition.getX() - radius) >> 4; x <= (worldPosition.getX() + radius) >> 4; x++) {
            for (int z = (worldPosition.getZ() - radius) >> 4; z <= (worldPosition.getZ() + radius) >> 4; z++) {
                var chunk = new ChunkPos(x, z);
                if (!chunk.equals(master)) others.add(chunk);
            }
        }
        waterTicket = FarmlandWaterManager.addCustomTicket(level, new AABBTicket(bounds), master, others.toArray(ChunkPos[]::new));
    }
    private void clearWaterTicket() {
        if (waterTicket != null) { waterTicket.invalidate(); waterTicket = null; }
    }
    @Override public void onLoad() { unloaded = false; super.onLoad(); if (network != null) network.start(); refreshWaterTicket(); }
    @Override public void onChunkUnloaded() { unloaded = true; if (network != null) network.stop(); clearWaterTicket(); super.onChunkUnloaded(); }
    @Override public void setRemoved() { if (network != null) network.stop(); clearWaterTicket(); super.setRemoved(); }
    @Override public void clearRemoved() { super.clearRemoved(); refreshWaterTicket(); }
    @Override public void setBlockState(BlockState state) { super.setBlockState(state); refreshWaterTicket(); }
    @Override public void handleRangePacket() { super.handleRangePacket(); refreshWaterTicket(); }

    @Override public void tick() {
        refreshWaterTicket();
        if (!(level instanceof ServerLevel server) || unloaded || recovery != null || isRemoved() || !server.isLoaded(worldPosition) || server.getBlockEntity(worldPosition) != this || !canTick(EnhancedHarvestingPylon.WORK_DELAY.get())) return;
        if (transferring) return;
        if (output == null) output = BlockCapabilityCache.create(Capabilities.Item.BLOCK, server, worldPosition.above(), Direction.DOWN);
        var inventory = output.getCapability();
        boolean usesMe = meEnabled && network != null;
        if (usesMe ? network.status() != 2 || !flushMe() : inventory == null || !flush(inventory)) return;

        int width = getSelectedRange();
        int area = width * width;
        // Both supported soil heights, sharing one bounded scan/harvest budget.
        int positions = area * 2;
        int checks = Math.min(positions, EnhancedHarvestingPylon.SCAN_BUDGET.get());
        int harvested = 0;
        for (int checked = 0; checked < checks && harvested < EnhancedHarvestingPylon.HARVEST_BUDGET.get(); checked++) {
            cursor = Math.floorMod(cursor, positions);
            int column = cursor / 2;
            int y = worldPosition.getY() + cursor % 2;
            var pos = new BlockPos(worldPosition.getX() + column % width - width / 2, y, worldPosition.getZ() + column / width - width / 2);
            cursor = (cursor + 1) % positions;
            if (!server.isLoaded(pos)) continue;
            if (harvest(server, pos)) {
                harvested++;
                setChanged();
                // Only one harvest can be pending, so a full chest cannot build an unbounded backlog.
                if (usesMe ? !flushMe() : !flush(inventory)) break;
            }
        }
    }

    private boolean harvest(ServerLevel server, BlockPos pos) {
        var state = server.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop) {
            if (crop.getMaxAge() <= 0 || !crop.isMaxAge(state)) return false;
            var drops = Block.getDrops(state, server, pos, null);
            var seed = state.getCloneItemStack(server, pos, true);
            if (!server.setBlockAndUpdate(pos, crop.getStateForAge(0))) return false;
            // Reserve one seed for the replanted crop, even if the loot table has multiple seed stacks.
            boolean reserved = seed.isEmpty();
            for (var drop : drops) {
                if (!reserved && drop.is(seed.getItem())) { drop.shrink(1); reserved = true; }
                if (!drop.isEmpty()) pending.add(drop.copy());
            }
            return true;
        }
        if (HarvestCompat.hasCompat(state.getBlock())) {
            var compat = HarvestCompat.getCompat(state.getBlock());
            if (!compat.isHarvestable(state)) return false;
            for (var drop : compat.harvest(server, pos, state)) if (!drop.isEmpty()) pending.add(drop.copy());
            return true;
        }
        var recipe = ModRegistry.HARVESTING_REGISTRY.findRecipe(state.getBlock());
        if (recipe.isEmpty()) return false;
        var rule = recipe.get();
        int age = state.getValue(rule.getAgeProperty());
        if (age != rule.getMaxAge() || age <= rule.getMinAge()) return false;
        if (!server.setBlock(pos, state.setValue(rule.getAgeProperty(), rule.getMaxAge() > 1 ? 1 : 0), 2)) return false;
        var drop = rule.getOutput().create();
        if (!drop.isEmpty()) pending.add(drop);
        return true;
    }

    /** One bounded ME offer. Unknown callback outcomes stay locked for reconciliation. */
    public int exportPending(java.util.function.ToLongBiFunction<ItemResource,Integer> sink) {
        if (!meEnabled || unloaded || isRemoved() || transferring || recovery != null || pending.isEmpty()
                || !(level instanceof ServerLevel server) || server.getBlockEntity(worldPosition) != this
                || net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction() != null) return 0;
        var first = pending.getFirst();
        var ops = com.neolena.essenceindustry.library.SeedDataSafety.ops(level.registryAccess());
        var unresolved = new net.minecraft.nbt.CompoundTag();
        unresolved.put("ambiguousMEOffer", ItemStack.CODEC.listOf().encodeStart(ops, List.copyOf(pending)).getOrThrow());
        recovery = new com.mojang.serialization.Dynamic<>(ops, unresolved);
        transferring = true;
        try {
            setChanged();
            long accepted = sink.applyAsLong(ItemResource.of(first), first.getCount());
            if (accepted < 0 || accepted > first.getCount()) throw new IllegalStateException("Invalid ME harvest insertion result");
            if (accepted == first.getCount()) pending.removeFirst();
            else if (accepted > 0) pending.set(0, first.copyWithCount(first.getCount() - (int)accepted));
            recovery = null; setChanged(); return (int)accepted;
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Harvester at {} locked after ambiguous ME insertion; pending offers retained for reconciliation", worldPosition, error);
            setChanged(); return 0;
        } finally { transferring = false; }
    }
    private boolean flushMe() {
        if (network == null || network.status() != 2) return false;
        for (int budget=0; budget<16 && !pending.isEmpty(); budget++) {
            int offered=pending.getFirst().getCount();
            if(exportPending(network::insert)<offered || quarantined())return false;
        }
        return pending.isEmpty();
    }
    private boolean flush(ResourceHandler<ItemResource> inventory) {
        while (!pending.isEmpty()) {
            var first = pending.getFirst();
            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                int inserted = inventory.insert(ItemResource.of(first), first.getCount(), tx);
                if (inserted < 0 || inserted > first.getCount()) throw new IllegalStateException("Invalid harvest destination accepted amount");
                if (inserted == 0) return false;
                pendingJournal.updateSnapshots(tx);
                if (inserted == first.getCount()) pending.removeFirst();
                else pending.set(0, first.copyWithCount(first.getCount() - inserted));
                tx.commit();
                if (inserted < first.getCount()) return false;
            }
        }
        return true;
    }
    @Override protected void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        data.putBoolean("meOutput", meEnabled);
        if (network != null) network.save(data);
        else if (nodeRecovery != null) data.store("meNode",com.mojang.serialization.Codec.PASSTHROUGH,nodeRecovery);
        if (recovery == null) data.store("pendingHarvest", ItemStack.CODEC.listOf(), pending);
        else data.store("pendingHarvest", com.mojang.serialization.Codec.PASSTHROUGH, recovery);
        data.putInt("scanCursor", cursor);
    }
    @Override protected void loadAdditional(ValueInput data) {
        super.loadAdditional(data);
        meEnabled = data.getBooleanOr("meOutput", false);
        nodeRecovery = data.read("meNode",com.mojang.serialization.Codec.PASSTHROUGH).orElse(null);
        if (network != null) { network.load(data); network.settingsChanged(); }
        var raw = data.read("pendingHarvest", com.mojang.serialization.Codec.PASSTHROUGH);
        if (raw.isPresent()) loadPending(raw.get()); else pending.clear();
        cursor = data.getIntOr("scanCursor", 0);
    }
    private void loadPending(com.mojang.serialization.Dynamic<?> raw) {
        var decoded = ItemStack.CODEC.listOf().parse(raw);
        if (decoded.error().isPresent()) {
            recovery = raw;
            org.slf4j.LoggerFactory.getLogger(EnhancedHarvestingPylonTile.class).error("Harvester at {} has unreadable pending drops; original data retained and harvesting locked", worldPosition);
        } else { pending.clear(); pending.addAll(decoded.result().orElseThrow()); recovery = null; }
    }
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder data) {
        super.collectImplicitComponents(data);
        var settings = new net.minecraft.nbt.CompoundTag(); settings.putBoolean("meOutput",meEnabled);
        data.set(EnhancedHarvestingPylon.SETTINGS.get(),net.minecraft.world.item.component.CustomData.of(settings));
        com.neolena.essenceindustry.item.InventoryRecovery.collect(data, "pendingHarvest", recovery);
    }
    @Override protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter data) {
        super.applyImplicitComponents(data);
        var settings = data.get(EnhancedHarvestingPylon.SETTINGS.get());
        if(settings != null){meEnabled=settings.copyTag().getBooleanOr("meOutput",false);if(network!=null)network.settingsChanged();}
        var raw = com.neolena.essenceindustry.item.InventoryRecovery.read(data, "pendingHarvest", level == null ? com.neolena.essenceindustry.library.SeedDataSafety.builtinLookup() : level.registryAccess());
        if (raw != null) loadPending(raw);
    }
    @Override public void dropItems() {
        super.dropItems();
        if (recovery == null && level != null && !level.isClientSide()) {
            for (var drop : pending) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), drop);
            pending.clear();
        }
    }
}
