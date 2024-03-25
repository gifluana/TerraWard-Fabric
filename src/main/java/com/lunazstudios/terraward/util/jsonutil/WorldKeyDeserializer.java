package com.lunazstudios.terraward.util.jsonutil;

import com.google.gson.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.lang.reflect.Type;

public class WorldKeyDeserializer implements JsonDeserializer<RegistryKey<World>> {
    @Override
    public RegistryKey<World> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject worldKeyObject = json.getAsJsonObject();

        JsonObject registryObject = worldKeyObject.getAsJsonObject("registry");
        String registryNamespace = registryObject.get("namespace").getAsString();
        String registryPath = registryObject.get("path").getAsString();

        JsonObject valueObject = worldKeyObject.getAsJsonObject("value");
        String valueNamespace = valueObject.get("namespace").getAsString();
        String valuePath = valueObject.get("path").getAsString();

        return RegistryKey.of(
                RegistryKey.ofRegistry(new Identifier(registryNamespace, registryPath)),
                new Identifier(valueNamespace, valuePath)
        );
    }
}
