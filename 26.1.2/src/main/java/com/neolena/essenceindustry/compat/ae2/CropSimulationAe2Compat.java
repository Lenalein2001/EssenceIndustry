package com.neolena.essenceindustry.compat.ae2;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.ticking.*;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.MachineSource;
import com.neolena.essenceindustry.simulation.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CropSimulationAe2Compat {
    public static void register(IEventBus bus) {
        CropSimulation.networkFactory = Bridge::new;
        bus.addListener(CropSimulationAe2Compat::capabilities);
    }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, CropSimulation.TILE.get(),
                (tile, context) -> (Bridge)tile.network());

    }
    public static final class Bridge implements SimulationNetwork, IInWorldGridNodeHost, appeng.api.networking.security.IActionHost {
        private final CropSimulationTile tile;
        private IManagedGridNode node;
        private boolean queued, initialized;
        private long lastImport = Long.MIN_VALUE;
        private net.minecraft.nbt.CompoundTag savedNode = new net.minecraft.nbt.CompoundTag();
        Bridge(CropSimulationTile tile) { this.tile = tile; }
        private void makeNode() {
            node = GridHelper.createManagedNode(tile, new IGridNodeListener<CropSimulationTile>() {
                public void onSaveChanges(CropSimulationTile owner, IGridNode ignored) { owner.setChanged(); }
                public void onStateChanged(CropSimulationTile owner, IGridNode ignored, State state) { changed(); }
            }).setTagName("meNode").setInWorldNode(true).setVisualRepresentation(CropSimulation.BLOCK.get())
                .setIdlePowerUsage(1).setFlags(GridFlags.REQUIRE_CHANNEL);
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
        public void changed() {}
        public void settingsChanged() { if (tile.meEnabled()) { start(); changed(); } else stop(); }
        public void load(ValueInput data) {
            data.read("meNode", com.mojang.serialization.Codec.PASSTHROUGH).ifPresent(raw -> savedNode.put("meNode", raw.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue().copy()));
        }
        public void save(ValueOutput data) { if (node != null && initialized) node.serialize(data); else data.store(savedNode); }
        public int status() { return !tile.meEnabled() ? 0 : node != null && node.isActive() ? 2 : 1; }
        public long insert(net.neoforged.neoforge.transfer.item.ItemResource resource, int amount) {
            if (node == null || !node.isActive()) return 0;
            var grid = node.getGrid();
            return StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(),
                    AEItemKey.of(resource), amount, new MachineSource(this), Actionable.MODULATE);
        }
    }
    private CropSimulationAe2Compat() {}
}
