package com.lunazstudios.terraward.util.jsonutil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.lang.reflect.Type;

/**
 * Helper method serialize RegistryKey<World> to json.
 */
public class WorldKeySerializer implements JsonSerializer<RegistryKey<World>> {
    @Override
    public JsonElement serialize(RegistryKey<World> src, Type typeOfSrc, JsonSerializationContext context) {
        Identifier registryIdentifier = src.getRegistry();
        Identifier valueIdentifier = src.getValue();

        JsonObject registryObject = new JsonObject();
        registryObject.addProperty("namespace", registryIdentifier.getNamespace());
        registryObject.addProperty("path", registryIdentifier.getPath());

        JsonObject valueObject = new JsonObject();
        valueObject.addProperty("namespace", valueIdentifier.getNamespace());
        valueObject.addProperty("path", valueIdentifier.getPath());

        JsonObject worldKeyObject = new JsonObject();
        worldKeyObject.add("registry", registryObject);
        worldKeyObject.add("value", valueObject);

        return worldKeyObject;
    }
}
