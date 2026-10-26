package com.neolena.essenceindustry.mixin;

import com.neolena.essenceindustry.migration.LegacyCompatibility;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** ServerAboutToStartEvent runs after configs load; migrate at the hook's entry instead. */
@Mixin(value = ServerLifecycleHooks.class, remap = false)
public abstract class ServerConfigMigrationMixin {
    @Inject(method = "handleServerAboutToStart", at = @At("HEAD"))
    private static void essenceindustry$migrateConfigs(MinecraftServer server, CallbackInfo ci) {
        LegacyCompatibility.migrateConfigs(server.getWorldPath(LevelResource.ROOT).resolve("serverconfig"));
    }
}
