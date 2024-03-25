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
import net.minecraft.world.World;

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
    private static ModConfig instance;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Vec3d.class, new Vec3dSerializer())
            .registerTypeAdapter(Vec3d.class, new Vec3dDeserializer())
            .registerTypeAdapter(RegistryKey.class, new WorldKeySerializer())
            .registerTypeAdapter(RegistryKey.class, new WorldKeyDeserializer())
            .setPrettyPrinting()
            .create();

    private static final Path configPath = Path.of("config/terraward.json");
    private Map<String, ProtectedArea> areas = new HashMap<>();

    private ModConfig() {

    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
            instance.load();
        }
        return instance;
    }

    private void load() {
        try {
            if (!Files.exists(configPath)) {
                save();
            } else {
                try (FileReader reader = new FileReader(configPath.toFile())) {
                    Type configType = new TypeToken<ModConfig>() {}.getType();
                    ModConfig config = gson.fromJson(reader, configType);
                    this.areas = config.areas;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArea(ProtectedArea area) {
        if (areas.containsKey(area.getName())) {
            return;
        }
        areas.put(area.getName(), area);
        save();
    }

    public void removeArea(String name) {
        if (!areas.containsKey(name)) {

            return;
        }
        areas.remove(name);
        save();
    }

    public ProtectedArea getArea(String name) {
        return areas.get(name);
    }

    public Collection<ProtectedArea> getAllAreas() {
        return areas.values();
    }

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
