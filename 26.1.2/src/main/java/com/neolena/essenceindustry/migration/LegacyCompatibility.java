package com.neolena.essenceindustry.migration;

import com.neolena.essenceindustry.EssenceIndustry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.LoggerFactory;

/** Read compatibility is permanent; new registrations and writes use the new namespace. */
public final class LegacyCompatibility {
    public static final String OLD_NAMESPACE = "mysticalagricultureextraseeds";
    private static final Map<String, String> CONFIG_FILES = Map.of(
            "mysticalagricultureextraseeds-server.toml", "essenceindustry-server.toml",
            "mysticalagricultureextraseeds-array-server.toml", "essenceindustry-array-server.toml",
            "extra-seeds-harvesting-server.toml", "essenceindustry-harvesting-server.toml",
            "extra-seeds-powah-common.toml", "essenceindustry-powah-common.toml");

    public static void register(IEventBus bus) {
        // Deferred registers finish at NORMAL priority. Cover every registry, including components,
        // block entity types and serializers, before any saved stacks or chunks are decoded.
        bus.addListener(EventPriority.LOWEST, LegacyCompatibility::aliases);
        migrateConfigs(FMLPaths.CONFIGDIR.get());
        migrateConfigs(FMLPaths.GAMEDIR.get().resolve("defaultconfigs"));
    }

    private static void aliases(RegisterEvent event) {
        var registry = event.getRegistry();
        int count = 0;
        for (Identifier id : registry.keySet()) {
            if (!id.getNamespace().equals(EssenceIndustry.MOD_ID)) continue;
            registry.addAlias(Identifier.fromNamespaceAndPath(OLD_NAMESPACE, id.getPath()), id);
            count++;
        }
        // Retired standalone graphite rods become reusable moderators. No duplicate
        // block, item or visual assets remain, and migration never creates extra fuel.
        if (event.getRegistryKey().equals(Registries.BLOCK) || event.getRegistryKey().equals(Registries.ITEM)) {
            var moderator = EssenceIndustry.id("reactor_moderator");
            registry.addAlias(EssenceIndustry.id("reactor_control_rod"), moderator);
            registry.addAlias(Identifier.fromNamespaceAndPath(OLD_NAMESPACE, "reactor_control_rod"), moderator);
        }
        if (count > 0) LoggerFactory.getLogger(LegacyCompatibility.class).debug(
                "Registered {} legacy aliases in {}", count, event.getRegistryKey().identifier());
    }

    public static String canonicalId(String value) {
        if (value.equals(EssenceIndustry.MOD_ID + ":reactor_control_rod") || value.equals(OLD_NAMESPACE + ":reactor_control_rod"))
            return EssenceIndustry.MOD_ID + ":reactor_moderator";
        String prefix = OLD_NAMESPACE + ":";
        String essencePrefix = "mysticalagriculture:essence/" + OLD_NAMESPACE + "/";
        if (value.startsWith(essencePrefix))
            return "mysticalagriculture:essence/" + EssenceIndustry.MOD_ID + "/" + value.substring(essencePrefix.length());
        return value.startsWith(prefix) ? EssenceIndustry.MOD_ID + value.substring(OLD_NAMESPACE.length()) : value;
    }

    /** Copy once, never replace an existing new config or delete the original. Fail before defaults load. */
    public static void migrateConfigs(Path directory) {
        for (var entry : CONFIG_FILES.entrySet()) {
            Path oldFile = directory.resolve(entry.getKey());
            Path newFile = directory.resolve(entry.getValue());
            if (!Files.isRegularFile(oldFile) || Files.exists(newFile)) continue;
            Path staging = null;
            try {
                staging = Files.createTempFile(directory, "essenceindustry-config-", ".tmp");
                Files.copy(oldFile, staging, StandardCopyOption.REPLACE_EXISTING);
                Files.move(staging, newFile); // No REPLACE_EXISTING: preserve a concurrent new config too.
                LoggerFactory.getLogger(LegacyCompatibility.class).info("Preserved settings: {} -> {}", oldFile, newFile);
            } catch (IOException error) {
                // Do not silently reset a world's production limits if its config cannot be copied.
                throw new IllegalStateException("Could not migrate Essence & Industry config " + oldFile, error);
            } finally {
                if (staging != null) try {
                    Files.deleteIfExists(staging);
                } catch (IOException error) {
                    LoggerFactory.getLogger(LegacyCompatibility.class).warn("Could not remove config staging file {}", staging, error);
                }
            }
        }
    }

    private LegacyCompatibility() {}
}
