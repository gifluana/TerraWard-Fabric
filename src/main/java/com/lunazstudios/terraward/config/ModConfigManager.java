package com.lunazstudios.terraward.config;

import com.lunazstudios.terraward.TerraWard;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModConfigManager {

    private static Map<MinecraftServer, ModConfig> configMap = new HashMap<>();

    /**
     * Ensure the directory containing the given path exists.
     * @param path The path to ensure the directory exists for.
     *             The directory is created if it does not exist.
     *             If the directory cannot be created, an error is printed to the console.
     *             If the directory already exists, nothing happens.
     */
    private static void ensureDirectoryExists(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            TerraWard.LOGGER.warn("An error occurred while creating directories", e);
        }
    }

    /**
     * This method registers the events for the ModConfigManager.
     */
    public static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Path configPath = server.getSavePath(WorldSavePath.ROOT).resolve("terraward/areas.json");

            ensureDirectoryExists(configPath);

            ModConfig config = new ModConfig(configPath);
            configMap.put(server, config);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ModConfig config = configMap.remove(server);
            if (config != null) {
                config.save();
            }
        });
    }

    /**
     * Get the ModConfig for the given server.
     * @param server The server to get the ModConfig for.
     */
    public static ModConfig getConfig(MinecraftServer server) {
        return configMap.get(server);
    }
}
