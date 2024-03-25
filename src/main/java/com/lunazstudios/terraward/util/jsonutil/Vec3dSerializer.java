package com.lunazstudios.terraward.util.jsonutil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Type;

public class Vec3dSerializer implements JsonSerializer<Vec3d> {
    @Override
    public JsonElement serialize(Vec3d src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", src.x);
        jsonObject.addProperty("y", src.y);
        jsonObject.addProperty("z", src.z);
        return jsonObject;
    }
}
