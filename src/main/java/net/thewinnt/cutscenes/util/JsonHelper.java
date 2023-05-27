package net.thewinnt.cutscenes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.phys.Vec3;

public class JsonHelper {
    public static Vec3 vec3FromJson(JsonObject json, String name) {
        JsonArray array = json.getAsJsonArray(name);
        if (array == null) return null;
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }

    public static Vec3 vec3FromJson(JsonElement json) {
        if (!json.isJsonArray()) return null;
        JsonArray array = json.getAsJsonArray();
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }
}
