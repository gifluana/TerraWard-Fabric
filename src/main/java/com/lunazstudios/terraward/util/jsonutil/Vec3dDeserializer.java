package com.lunazstudios.terraward.util.jsonutil;

import com.google.gson.*;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Type;

public class Vec3dDeserializer implements JsonDeserializer<Vec3d> {

    /**
     * Helper method deserialize Vec3D from json.
     */
    @Override
    public Vec3d deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        return new Vec3d(x, y, z);
    }
}
