package com.lunazstudios.terraward.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.util.jsonutil.Vec3dDeserializer;
import com.lunazstudios.terraward.util.jsonutil.Vec3dSerializer;
import com.lunazstudios.terraward.util.jsonutil.WorldKeyDeserializer;
import com.lunazstudios.terraward.util.jsonutil.WorldKeySerializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {

    /**
     * The Gson instance used to serialize and deserialize the config.
     * This Gson instance is configured to handle Vec3d and RegistryKey objects.
     * It is also configured to pretty print the JSON output.
     */
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Vec3d.class, new Vec3dSerializer())
            .registerTypeAdapter(Vec3d.class, new Vec3dDeserializer())
            .registerTypeAdapter(RegistryKey.class, new WorldKeySerializer())
            .registerTypeAdapter(RegistryKey.class, new WorldKeyDeserializer())
            .setPrettyPrinting()
            .create();

    private final Path configPath;
    private Map<String, ProtectedArea> areas;

    /**
     * Create a new ModConfig instance.
     */
    public ModConfig(Path configPath) {
        this.configPath = configPath;
        this.areas = new HashMap<>();
        load();
    }

    /**
     * Load the config from the file at the configPath.
     */
    private void load() {
        try {
            if (!Files.exists(configPath)) {
                save();
            } else {
                try (FileReader reader = new FileReader(configPath.toFile())) {
                    Type configType = new TypeToken<Map<String, ProtectedArea>>() {}.getType();
                    this.areas = gson.fromJson(reader, configType);
                    if (this.areas == null) {
                        this.areas = new HashMap<>();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the config to the file at the configPath.
     */
    public void save() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            gson.toJson(this.areas, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new area to the config.
     * If an area with the same name already exists, it is not added.
     */
    public void addArea(ProtectedArea area) {
        if (areas.containsKey(area.getName())) {
            return;
        }
        areas.put(area.getName(), area);
        save();
    }

    /**
     * Remove an area from the config.
     * If an area with the given name does not exist, nothing happens.
     * @param name The name of the area to remove.
     *             The name is case-sensitive.
     *             The name is the unique identifier of the area.
     */
    public boolean removeArea(String name) {
        if (!areas.containsKey(name)) {
            return false;
        }
        areas.remove(name);
        save();
        return true;
    }

    /**
     * Get an area from the config by name.
     * If an area with the given name does not exist, null is returned.
     * @param name The name of the area to get.
     *             The name is case-sensitive.
     *             The name is the unique identifier of the area.
     */
    public ProtectedArea getArea(String name) {
        return areas.get(name);
    }

    /**
     * Get all areas in the config.
     * The areas are returned as a collection.
     */
    public Collection<ProtectedArea> getAllAreas() {
        return areas.values();
    }

    /**
     * Find the highest priority area that contains the given location.
     * If no area contains the location, null is returned.
     * @param x The x-coordinate of the location.
     */
    public ProtectedArea findAreaByLocation(int x, int y, int z) {
        ProtectedArea highestPriorityArea = null;

        for (ProtectedArea area : areas.values()) {
            if (area.contains(x, y, z)) {
                if (highestPriorityArea == null || area.getPriority() > highestPriorityArea.getPriority()) {
                    highestPriorityArea = area;
                }
            }
        }

        return highestPriorityArea;
    }
}
