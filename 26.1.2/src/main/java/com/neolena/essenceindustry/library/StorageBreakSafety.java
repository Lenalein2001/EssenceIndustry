package com.neolena.essenceindustry.library;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/** Oversized legacy inventories stay in-world rather than becoming an unreadable item packet. */
public final class StorageBreakSafety {
    public static void register() { NeoForge.EVENT_BUS.addListener(StorageBreakSafety::exploding); }
    public static boolean unsafe(BlockEntity tile) {
        if(tile instanceof com.neolena.essenceindustry.nuclear.ReactorTile reactor)return !reactor.safeToMove();
        if (ModList.get().isLoaded("ae2") && tile instanceof com.neolena.essenceindustry.array.ArrayTile array) return !array.empty();
        if(tile instanceof com.neolena.essenceindustry.simulation.CropSimulationTile simulator)return !simulator.safeToMove();
        if(tile instanceof com.neolena.essenceindustry.seedcraft.SeedCrafterTile crafter)return !crafter.safeToMove();
        if (ModList.get().isLoaded("ae2") && tile instanceof com.neolena.essenceindustry.compat.ae2.pattern.PatternManagerTile manager) return !manager.safeToMove();
        if (tile instanceof SeedLibraryTile library) return !library.safeToMove();
        if (tile instanceof com.neolena.essenceindustry.essence.EssenceConverterTile converter) return !converter.safeToMove();
        return ModList.get().isLoaded("pylons") && com.neolena.essenceindustry.compat.pylons.EnhancedHarvestingPylonBlock.unsafeRecovery(tile);
    }
    public static boolean denyPlayerRemoval(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player) {
        if (!unsafe(level.getBlockEntity(pos))) return false;
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && serverPlayer.connection != null)
            serverPlayer.sendOverlayMessage(Component.translatable("message.essenceindustry.storage_too_large"));
        return true;
    }
    public static boolean protectedFromExplosion(BlockEntity tile) {
        if(tile instanceof com.neolena.essenceindustry.nuclear.ReactorTile reactor)return reactor.hasContents() || !reactor.safeToMove();
        if (ModList.get().isLoaded("ae2") && tile instanceof com.neolena.essenceindustry.array.ArrayTile array) return !array.empty();
        if(tile instanceof com.neolena.essenceindustry.simulation.CropSimulationTile simulator)return simulator.quarantined() || !simulator.safeToMove();
        if(tile instanceof com.neolena.essenceindustry.seedcraft.SeedCrafterTile crafter)return crafter.quarantined() || !crafter.safeToMove();
        if (ModList.get().isLoaded("ae2") && tile instanceof com.neolena.essenceindustry.compat.ae2.pattern.PatternManagerTile manager) return !manager.pending().empty() || manager.quarantined() || !manager.safeToMove();
        if (tile instanceof SeedLibraryTile library) return library.quarantined() || !library.safeToMove();
        if (tile instanceof com.neolena.essenceindustry.essence.EssenceConverterTile converter) return converter.quarantined() || !converter.safeToMove() || java.util.stream.IntStream.range(0,6).anyMatch(i -> converter.inventory().getAmountAsLong(i)>0);
        return ModList.get().isLoaded("pylons") && com.neolena.essenceindustry.compat.pylons.EnhancedHarvestingPylonBlock.protectedRecovery(tile);
    }
    private static void exploding(ExplosionEvent.Detonate event) { event.getAffectedBlocks().removeIf(pos -> protectedFromExplosion(event.getLevel().getBlockEntity(pos))); }
    private StorageBreakSafety() {}
}
