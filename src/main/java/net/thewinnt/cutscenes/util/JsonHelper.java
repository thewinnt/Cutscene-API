package net.thewinnt.cutscenes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.minecraft.world.phys.Vec3;

public class JsonHelper {
    public static Vec3 vec3FromJson(JsonObject json, String name) {
        JsonElement element = json.get(name);
        JsonArray array;
        if (element == null || element instanceof JsonNull) {
            return null;
        } else {
            array = (JsonArray)element;
        }
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }

    public static Vec3 vec3FromJson(JsonElement json) {
        if (!json.isJsonArray()) return null;
        JsonArray array = json.getAsJsonArray();
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }
}
