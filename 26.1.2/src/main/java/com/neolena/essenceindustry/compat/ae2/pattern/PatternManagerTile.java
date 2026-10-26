package com.neolena.essenceindustry.compat.ae2.pattern;

import appeng.api.config.*;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.networking.ticking.*;
import appeng.api.stacks.*;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.MachineSource;
import appeng.util.SettingsFrom;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neolena.essenceindustry.item.InventoryRecovery;
import com.neolena.essenceindustry.library.SeedDataSafety;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class PatternManagerTile extends AENetworkedBlockEntity implements ICraftingProvider, IGridTickable, IStorageWatcherNode {
    public record Saved(List<PatternOutputQueue.Saved> pending, List<String> disabled, boolean locked) {
        public Saved {
            pending = List.copyOf(pending);
            disabled = disabled.stream().map(com.neolena.essenceindustry.migration.LegacyCompatibility::canonicalId).distinct().toList();
        }
        public static final Codec<Saved> CODEC = RecordCodecBuilder.create(i -> i.group(PatternOutputQueue.Saved.CODEC.listOf(0, PatternOutputQueue.MAX_KEYS).fieldOf("pending").forGetter(Saved::pending), Codec.STRING.listOf(0, 4096).fieldOf("disabled").forGetter(Saved::disabled), Codec.BOOL.fieldOf("locked").forGetter(Saved::locked)).apply(i, Saved::new));
    }
    private static final Set<PatternManagerTile> LOADED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<IGrid, Set<PatternManagerTile>> GROUPS = new IdentityHashMap<>();
    private static final Map<MinecraftServer, Budget> BUDGETS = new WeakHashMap<>();
    private static final class Budget { int tick = -1, admissions, flushes; }
    private final PatternOutputQueue output = new PatternOutputQueue();
    private final Set<String> disabled = new HashSet<>();
    private EssencePatternCatalog.Snapshot catalog;
    private Map<AEItemKey, EssencePatternCatalog.Entry> enabled = Map.of();
    private List<IPatternDetails> advertised = List.of();
    private List<EssencePatternCatalog.Entry> visible = List.of();
    private IStackWatcher storageWatcher;
    private Set<AEKey> watchedEssences = Set.of();
    private final Set<AEKey> presentEssences = new HashSet<>();
    private boolean availabilityDirty, resettingWatcher;
    private Dynamic<?> recovery;
    private boolean pushing;
    private boolean leader;
    private IGrid membership;
    private boolean allOutputMetadataFits;
    private long revision;
    private long acceptedTick = Long.MIN_VALUE;
    private long dirtyTick = Long.MIN_VALUE;
    @Override public void saveChanges() {
        if (level == null || level.isClientSide()) { super.saveChanges(); return; }
        if (dirtyTick != level.getGameTime()) { dirtyTick = level.getGameTime(); super.saveChanges(); }
    }
    public PatternManagerTile(BlockPos pos, BlockState state) {
        super(EssencePatternManager.TILE.get(), pos, state);
        getMainNode().setIdlePowerUsage(2).setFlags(GridFlags.REQUIRE_CHANNEL).addService(ICraftingProvider.class, this).addService(IGridTickable.class, this).addService(IStorageWatcherNode.class, this);
    }
    public PatternOutputQueue pending() { return output; }
    public boolean leader() { return leader; }
    public long revision() { return revision; }
    /** Execution lookup only; never used by AE2's craftable-item catalogue/planner. */
    public static ICraftingProvider executionProvider(IGrid grid, IPatternDetails pattern) {
        var members = GROUPS.get(grid);
        if (members == null || pattern == null) return null;
        for (var tile : members) if (tile.leader && tile.usable() && tile.getMainNode().isActive()) {
            var entry = tile.enabled.get(pattern.getDefinition());
            return entry != null && entry.pattern().equals(pattern) ? tile : null;
        }
        return null;
    }
    @Override protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, new appeng.me.helpers.BlockEntityNodeListener<PatternManagerTile>() {
            @Override public void onGridChanged(PatternManagerTile owner, IGridNode node) { owner.membershipChanged(); }
        });
    }
    private void membershipChanged() {
        IGrid next = LOADED.contains(this) && !isRemoved() ? getMainNode().getGrid() : null;
        boolean changed = membership != next;
        if (membership != next) {
            var old = membership; membership = next;
            if (old != null) { var set = GROUPS.get(old); if (set != null) { set.remove(this); if (set.isEmpty()) GROUPS.remove(old); } elect(old); }
            if (next != null) GROUPS.computeIfAbsent(next, k -> Collections.newSetFromMap(new IdentityHashMap<>())).add(this);
        }
        if (next == null && leader) { leader = false; publish(); }
        elect(next);
        if (changed) resetStorageWatcher();
    }
    private static void elect(IGrid grid) {
        var set = GROUPS.get(grid); if (set == null) return;
        PatternManagerTile chosen = null;
        for (var tile : set) if (tile.getMainNode().isActive() && !tile.isRemoved()) {
            if (chosen == null || tile.level.dimension().identifier().toString().compareTo(chosen.level.dimension().identifier().toString()) < 0
                || (tile.level.dimension() == chosen.level.dimension() && tile.worldPosition.compareTo(chosen.worldPosition) < 0)) chosen = tile;
        }
        for (var tile : List.copyOf(set)) if (tile.leader != (tile == chosen)) { tile.leader = tile == chosen; tile.publish(); }
    }
    @Override public void onMainNodeStateChanged(IGridNodeListener.State reason) { membershipChanged(); wake(); }
    public boolean quarantined() { return recovery != null || output.locked(); }
    public boolean transferring() { return pushing || output.transferring(); }
    public boolean usable() { return !isRemoved() && level != null && level.getBlockEntity(worldPosition) == this && !quarantined(); }
    private Budget budget() {
        var server = ((ServerLevel)level).getServer(); var b = BUDGETS.computeIfAbsent(server, k -> new Budget());
        if (b.tick != server.getTickCount()) { b.tick = server.getTickCount(); b.admissions = 0; b.flushes = 0; } return b;
    }
    public static void clearServer(MinecraftServer server) { for (var tile : List.copyOf(LOADED)) if (tile.level == null || tile.level.getServer() == server) { LOADED.remove(tile); tile.membershipChanged(); } BUDGETS.remove(server); }
    public static void recipesChanged(MinecraftServer server) {
        // Player joins also fire this event; identity check prevents redundant rebuild/refresh.
        var next = EssencePatternCatalog.get(server);
        for (var tile : List.copyOf(LOADED)) if (tile.level != null && tile.level.getServer() == server && tile.catalog != next && !tile.isRemoved()) tile.refresh();
    }
    @Override public void onReady() { LOADED.add(this); super.onReady(); membershipChanged(); refresh(); wake(); }
    @Override public void setRemoved() { LOADED.remove(this); super.setRemoved(); membershipChanged(); }
    @Override public void onChunkUnloaded() { LOADED.remove(this); super.onChunkUnloaded(); membershipChanged(); }
    private void refresh() {
        if (!(level instanceof ServerLevel server)) return;
        catalog = EssencePatternCatalog.get(server.getServer());
        var map = new LinkedHashMap<AEItemKey, EssencePatternCatalog.Entry>();
        for (var e : catalog.entries()) if (!disabled.contains(e.id())) map.put(e.pattern().getDefinition(), e);
        // Keep admission identities independent of visibility: a crafting CPU can hold
        // the last essence after planning, before delivering its already-accepted job.
        enabled = Map.copyOf(map);
        var keys = new HashSet<AEKey>();
        for (var e : catalog.entries()) keys.addAll(e.inputs().keySet());
        watchedEssences = Set.copyOf(keys);
        resetStorageWatcher();
        // Pay the serialization budget check on catalog/config changes, not on each craft.
        var possible = new HashSet<AEItemKey>(); for (var e : map.values()) possible.add(e.output());
        for (var saved : output.save()) possible.add(AEItemKey.of(saved.sample()));
        var ops = SeedDataSafety.ops(level.registryAccess());
        var weights = new HashMap<AEItemKey, Long>();
        for (var key : possible) weights.put(key, SeedDataSafety.tagWeight(PatternOutputQueue.Saved.CODEC.encodeStart(ops, new PatternOutputQueue.Saved(key.toStack(), PatternOutputQueue.MAX_PER_KEY)).getOrThrow()));
        var worst = possible.stream().sorted(Comparator.comparingLong((AEItemKey k) -> weights.get(k)).reversed()).limit(PatternOutputQueue.MAX_KEYS).map(k -> new PatternOutputQueue.Saved(k.toStack(), PatternOutputQueue.MAX_PER_KEY)).toList();
        allOutputMetadataFits = InventoryRecovery.safe(new Dynamic<>(ops, Saved.CODEC.encodeStart(ops, new Saved(worst, disabled.stream().sorted().toList(), false)).getOrThrow()));
        publish();
    }
    @Override public void updateWatcher(IStackWatcher watcher) {
        storageWatcher = watcher;
        resetStorageWatcher();
    }
    private void resetStorageWatcher() {
        resettingWatcher = true;
        try {
            presentEssences.clear();
            if (storageWatcher != null) {
                storageWatcher.reset();
                storageWatcher.setWatchAll(false);
                for (var key : watchedEssences) storageWatcher.add(key);
            }
            var grid = getMainNode().getGrid();
            if (grid != null && storageWatcher != null) {
                var inventory = grid.getStorageService().getCachedInventory();
                for (var key : watchedEssences) if (inventory.get(key) > 0) presentEssences.add(key);
            }
        } finally { resettingWatcher = false; }
        availabilityDirty = true;
        wake();
    }
    @Override public void onStackChange(AEKey key, long amount) {
        if (resettingWatcher || !watchedEssences.contains(key)) return;
        // Ordinary count changes do not rebuild the list or notify the crafting service.
        boolean changed = amount > 0 ? presentEssences.add(key) : presentEssences.remove(key);
        if (changed) { availabilityDirty = true; wake(); }
    }
    private void updateAvailability() {
        if (!availabilityDirty) return;
        availabilityDirty = false;
        var next = catalog == null ? List.<EssencePatternCatalog.Entry>of() : catalog.entries().stream()
                .filter(e -> presentEssences.containsAll(e.inputs().keySet())).toList();
        var patterns = next.stream().filter(e -> !disabled.contains(e.id())).map(EssencePatternCatalog.Entry::pattern).toList();
        boolean changed = !visible.equals(next) || !advertised.equals(patterns);
        visible = next; advertised = patterns;
        if (changed) publish();
    }
    private void publish() { revision++; ICraftingProvider.requestUpdate(getMainNode()); }
    public List<EssencePatternCatalog.Entry> entries() { return visible; }
    public boolean enabled(String id) { return !disabled.contains(id); }
    public void toggle(String id) {
        if (!usable() || transferring() || catalog == null || catalog.entries().stream().noneMatch(e -> e.id().equals(id))) return;
        boolean wasDisabled = disabled.remove(id);
        if (!wasDisabled) { if (disabled.size() >= 4096) return; disabled.add(id); }
        if (!InventoryRecovery.safe(raw())) { if (wasDisabled) disabled.add(id); else disabled.remove(id); return; }
        saveChanges(); refresh();
    }
    @Override public List<IPatternDetails> getAvailablePatterns() { return quarantined() || !leader ? List.of() : advertised; }
    // Prefer direct essence conversions over ordinary compression/decompression paths.
    // AE2's missing-item simulation accepts missing inputs on its first path rather
    // than comparing alternatives, so a low priority can hide available essence
    // recipes whenever an unrelated branch prevents a complete plan.
    @Override public int getPatternPriority() { return 100; }
    @Override public boolean isBusy() { return !usable() || transferring() || !leader || !getMainNode().isActive() || budget().admissions >= 256; }
    @Override public boolean pushPattern(IPatternDetails pattern, KeyCounter[] holders) {
        if (pattern == null || holders == null || isBusy()) return false;
        if (catalog != EssencePatternCatalog.get(((ServerLevel)level).getServer())) { refresh(); return false; }
        var entry = enabled.get(pattern.getDefinition());
        if (entry == null || !entry.pattern().equals(pattern) || holders.length != pattern.getInputs().length || !output.canAdd(entry.output(), entry.amount())) return false;
        if (!allOutputMetadataFits && output.count(entry.output()) == 0) {
            var records = new ArrayList<>(output.save()); records.add(new PatternOutputQueue.Saved(entry.output().toStack(), entry.amount()));
            var ops = SeedDataSafety.ops(level.registryAccess());
            var prospective = new Dynamic<>(ops, Saved.CODEC.encodeStart(ops, new Saved(records, disabled.stream().sorted().toList(), false)).getOrThrow());
            if (!InventoryRecovery.safe(prospective)) return false;
        }
        var actual = new HashMap<AEKey, Long>();
        try { for (var holder : holders) { if (holder == null) return false; for (var stack : holder) { if (stack.getLongValue() <= 0) return false; actual.merge(stack.getKey(), stack.getLongValue(), Math::addExact); } } }
        catch (ArithmeticException error) { return false; }
        if (!actual.equals(entry.inputs())) return false;
        pushing = true;
        try {
            boolean empty = output.empty();
            if (!output.add(entry.output(), entry.amount())) return false;
            // AE2 transfers ownership on true. Do not extract from ME or modify its counters.
            budget().admissions++; if (empty) acceptedTick = level.getGameTime(); saveChanges(); wake(); return true;
        } finally { pushing = false; }
    }
    private void wake() { getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice(n)); }
    @Override public TickingRequest getTickingRequest(IGridNode node) { return new TickingRequest(1, 40, output.empty() && !availabilityDirty); }
    @Override public TickRateModulation tickingRequest(IGridNode node, int elapsed) {
        // Coalesce all zero/nonzero transitions from one insertion/extraction burst.
        // This must run even with no queued output, then the manager sleeps again.
        updateAvailability();
        if (!usable() || output.empty()) return TickRateModulation.SLEEP;
        if (transferring() || acceptedTick == level.getGameTime()) return TickRateModulation.URGENT;
        if (!node.isActive()) return TickRateModulation.IDLE;
        var b = budget(); int limit = Math.min(16, 128 - b.flushes); if (limit <= 0) return TickRateModulation.SLOWER;
        var grid = node.getGrid(); long before = total();
        try { b.flushes += output.flush((key, amount) -> StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), key, amount, new MachineSource(this), Actionable.MODULATE), limit); }
        catch (RuntimeException error) { b.flushes += limit; ICraftingProvider.requestUpdate(getMainNode()); org.slf4j.LoggerFactory.getLogger(getClass()).error("Pattern manager output at {} quarantined after ambiguous ME insertion", worldPosition, error); }
        if (before != total() || quarantined()) saveChanges();
        return output.empty() || quarantined() ? TickRateModulation.SLEEP : before == total() ? TickRateModulation.SLOWER : TickRateModulation.URGENT;
    }
    public long total() { return output.total(); }
    private Saved state() { return new Saved(output.save(), disabled.stream().sorted().toList(), output.locked()); }
    public boolean safeToMove() { return !transferring() && InventoryRecovery.safe(raw()); }
    private Dynamic<?> raw() {
        if (recovery != null) return recovery;
        var lookup = level == null ? SeedDataSafety.builtinLookup() : level.registryAccess();
        var ops = SeedDataSafety.ops(lookup);
        return new Dynamic<>(ops, Saved.CODEC.encodeStart(ops, state()).getOrThrow());
    }
    private void restore(Dynamic<?> raw) {
        recovery = raw;
        if (!InventoryRecovery.safe(raw)) return;
        var parsed = Saved.CODEC.parse(raw);
        if (parsed.error().isPresent() || !InventoryRecovery.safe(raw)) return;
        var s = parsed.result().orElseThrow();
        if (s.disabled().size() > 4096 || s.disabled().stream().anyMatch(id -> id.length() > 256)) return;
        try { output.load(s.pending()); } catch (IllegalArgumentException error) { return; }
        disabled.clear(); disabled.addAll(s.disabled()); if (s.locked()) output.lock(); recovery = null;
    }
    @Override public void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.store("essenceManager", Codec.PASSTHROUGH, raw()); }
    @Override public void loadTag(ValueInput in) { super.loadTag(in); in.read("essenceManager", Codec.PASSTHROUGH).ifPresent(this::restore); }
    @Override public void exportSettings(SettingsFrom mode, DataComponentMap.Builder data, Player player) {
        super.exportSettings(mode, data, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            if (!safeToMove()) throw new IllegalStateException("Pattern manager cannot move during transfer or with oversized recovery data");
            InventoryRecovery.collect(data, "essenceManager", raw());
        }
    }
    @Override public void importSettings(SettingsFrom mode, DataComponentMap data, Player player) {
        super.importSettings(mode, data, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            var raw = InventoryRecovery.read(data, "essenceManager", level == null ? SeedDataSafety.builtinLookup() : level.registryAccess());
            if (raw != null) restore(raw);
        }
    }
}
