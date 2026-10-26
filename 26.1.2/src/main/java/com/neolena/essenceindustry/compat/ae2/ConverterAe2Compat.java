package com.neolena.essenceindustry.compat.ae2;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.ticking.*;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.MachineSource;
import com.neolena.essenceindustry.essence.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ConverterAe2Compat {
    public static void register(IEventBus bus) {
        EssenceConverter.networkFactory = Bridge::new;
        bus.addListener(ConverterAe2Compat::capabilities);
    }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, EssenceConverter.TILE.get(),
                (tile, context) -> (Bridge)tile.network());
        event.registerBlockEntity(AECapabilities.ME_STORAGE, EssenceConverter.TILE.get(),
                (tile, side) -> ((Bridge)tile.network()).storage(side));
    }
    public static final class Bridge implements ConverterNetwork, IInWorldGridNodeHost, IGridTickable, appeng.api.networking.security.IActionHost, appeng.api.storage.IStorageProvider {
        private final EssenceConverterTile tile;
        private IManagedGridNode node;
        private boolean queued, initialized;
        private long lastImport = Long.MIN_VALUE;
        private long listedRevision = Long.MIN_VALUE;
        private boolean listedAvailable;
        private net.minecraft.nbt.CompoundTag savedNode = new net.minecraft.nbt.CompoundTag();
        private final StorageView[] views = new StorageView[7];
        public appeng.api.storage.MEStorage storage(Direction side) {
            int index = side == null ? 6 : side.ordinal();
            if (views[index] == null) views[index] = new StorageView(tile,false);
            return views[index];
        }
        Bridge(EssenceConverterTile tile) { this.tile = tile; }
        public void mountInventories(appeng.api.storage.IStorageMounts mounts) { mounts.mount(new StorageView(tile,true)); }
        private void makeNode() {
            node = GridHelper.createManagedNode(tile, new IGridNodeListener<EssenceConverterTile>() {
                public void onSaveChanges(EssenceConverterTile owner, IGridNode ignored) { owner.setChanged(); }
                public void onStateChanged(EssenceConverterTile owner, IGridNode ignored, State state) { changed(); }
            }).setTagName("meNode").setInWorldNode(true).setVisualRepresentation(EssenceConverter.BLOCK.get())
                .setIdlePowerUsage(1).setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, this).addService(appeng.api.storage.IStorageProvider.class,this);
            node.deserialize(TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,
                    tile.getLevel().registryAccess(), savedNode));
        }
        public IManagedGridNode managedNode() { return node; }
        public IGridNode getActionableNode() { return node == null ? null : node.getNode(); }
        public IGridNode getGridNode(Direction side) { return tile.meEnabled() ? getActionableNode() : null; }
        public void start() {
            if (!tile.meEnabled() || tile.getLevel() == null || tile.getLevel().isClientSide() || queued || initialized) return;
            if (node == null) makeNode();
            queued = true;
            var scheduledNode = node;
            GridHelper.onFirstTick(tile, t -> {
                if (node != scheduledNode) return;
                queued = false;
                if (t.meEnabled() && !initialized && !t.isRemoved() && t.getLevel() != null && t.getLevel().getBlockEntity(t.getBlockPos()) == t) {
                    initialized = true; node.create(t.getLevel(), t.getBlockPos());
                }
            });
        }
        public void stop() {
            if (node != null) {
                if (initialized && tile.getLevel() != null) {
                    var out = TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, tile.getLevel().registryAccess());
                    node.serialize(out); savedNode = out.buildResult();
                }
                node.destroy(); node = null;
            }
            queued = initialized = false;
        }
        public void changed() { if (node != null) node.ifPresent((grid, n) -> {
            long revision=tile.storageRevision();boolean available=tile.storageAvailable();
            if(revision!=listedRevision || available!=listedAvailable){listedRevision=revision;listedAvailable=available;grid.getStorageService().invalidateCache();}
            grid.getTickManager().alertDevice(n);
        }); }
        public void settingsChanged() { if (tile.meEnabled()) { start(); changed(); } else stop(); }
        public void load(ValueInput data) {
            data.read("meNode", com.mojang.serialization.Codec.PASSTHROUGH).ifPresent(raw -> savedNode.put("meNode", raw.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue().copy()));
        }
        public void save(ValueOutput data) { if (node != null && initialized) node.serialize(data); else data.store(savedNode); }
        public int status() { return !tile.meEnabled() ? 0 : node != null && node.isActive() ? 2 : 1; }
        public TickingRequest getTickingRequest(IGridNode n) { return new TickingRequest(5, 40, true); }
        public TickRateModulation tickingRequest(IGridNode n, int elapsed) {
            if (!tile.meEnabled() || !tile.storageAvailable()) return TickRateModulation.SLEEP;
            if (!n.isActive()) return TickRateModulation.SLEEP;
            var grid = n.getGrid();
            var source = new MachineSource(this);
            int moved = tile.exportResult((resource, amount) -> StorageHelper.poweredInsert(grid.getEnergyService(),
                    grid.getStorageService().getInventory(), AEItemKey.of(resource), amount, source, Actionable.MODULATE));
            long now = tile.getLevel().getGameTime();
            if (!tile.quarantined() && (lastImport == Long.MIN_VALUE || now - lastImport >= 20)) {
                lastImport = now;
                // Six known keys only, no network inventory enumeration or catch-up work.
                for (int tier = 0; tier <= tile.highest() && !tile.quarantined(); tier++) {
                    if (tier == tile.target()) continue;
                    if (StorageHelper.poweredExtraction(grid.getEnergyService(), grid.getStorageService().getInventory(),
                            AEItemKey.of(EssenceConverterTile.essence(tier)), 1, source, Actionable.SIMULATE) == 0) continue;
                    moved += tile.importEssence(tier, (resource, amount) -> StorageHelper.poweredExtraction(grid.getEnergyService(),
                            grid.getStorageService().getInventory(), AEItemKey.of(resource), amount, source, Actionable.MODULATE));
                }
            }
            return tile.quarantined() ? TickRateModulation.SLEEP : moved == 0 ? TickRateModulation.SLOWER : TickRateModulation.URGENT;
        }
    }
    /** All tiers are requestable; automatic export still uses only the result tier.
     * External views are disabled in direct ME mode to prevent mounting the same stock twice. */
    private static final class StorageView implements appeng.api.storage.MEStorage, appeng.me.storage.ITickingMonitor {
        private final EssenceConverterTile tile;
        private final boolean direct;
        private boolean available() { return tile.storageAvailable() && (direct ? tile.meEnabled() : !tile.meEnabled()); }
        private long revision = Long.MIN_VALUE;
        StorageView(EssenceConverterTile tile,boolean direct) { this.tile = tile;this.direct=direct; }
        public net.minecraft.network.chat.Component getDescription() { return net.minecraft.network.chat.Component.translatable("block.essenceindustry.essence_converter"); }
        public int getEstimatedStackCount() { return 6; }
        public void getAvailableStacks(appeng.api.stacks.KeyCounter out) {
            if(available())for(int i=0;i<6;i++)if(!tile.inventory().getResource(i).isEmpty()) out.add(AEItemKey.of(tile.inventory().getResource(i)),tile.inventory().getAmountAsLong(i));
        }
        public TickRateModulation onTick() {
            long next = tile.storageRevision(); if (next == revision) return TickRateModulation.SLOWER;
            revision = next; return TickRateModulation.URGENT;
        }
        public long insert(appeng.api.stacks.AEKey key,long amount,Actionable mode,appeng.api.networking.security.IActionSource source) { return transfer(key,amount,mode,true,source); }
        public long extract(appeng.api.stacks.AEKey key,long amount,Actionable mode,appeng.api.networking.security.IActionSource source) { return transfer(key,amount,mode,false,source); }
        private long transfer(appeng.api.stacks.AEKey key,long amount,Actionable mode,boolean insert,appeng.api.networking.security.IActionSource source) {
            if (amount < 0) throw new IllegalArgumentException("Negative transfer amount");
            if(direct && source.machine().orElse(null)==tile.network())return 0;
            if (amount == 0 || !available() || !(key instanceof AEItemKey item)) return 0;
            if (insert && tile.meEnabled() && item.getItem() == EssenceConverterTile.essence(tile.target())) return 0;
            var resource = item.toResource();
            int tier=-1;for(int i=0;i<6;i++)if(tile.inventory().isValid(i,resource)){tier=i;break;}
            if(tier<0)return 0;
            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.open(net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction())) {
                long moved = insert ? tile.inventory().insertLong(tier,resource,amount,tx) : tile.inventory().extractLong(tier,resource,amount,tx);
                if (mode == Actionable.MODULATE) tx.commit(); return moved;
            }
        }
    }
    private ConverterAe2Compat() {}
}
