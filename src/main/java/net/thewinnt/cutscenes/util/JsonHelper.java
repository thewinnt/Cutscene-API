package net.thewinnt.cutscenes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;

import javax.annotation.Nullable;

/** A class containing some helpful functions for JSON parsing. */
public class JsonHelper {
    /**
     * Gets a Vec3 from a JSON object, if it's written in the form of [x, y, z]
     * @param json The JSON object to look in
     * @param name The name of the field
     * @return the Vec3, if it's there and written correctly, or null otherwise
     * @throws IndexOutOfBoundsException if the vector array is shorter than 3 numbers
     */
    public static Vec3 vec3FromJson(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element == null || element instanceof JsonNull) {
            return null;
        } else if (element instanceof JsonArray array) {
            return new Vec3(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        } else {
            return null;
        }
    }

    /**
     * Gets a Vec3 from a JSON element, if it's written in the form of [x, y, z]
     * @param json The JSON element to read from
     * @return the Vec3, if it's written correctly, or null otherwise
     * @throws IndexOutOfBoundsException if the vector array is shorter than 3 numbers
     */
    public static Vec3 vec3FromJson(JsonElement json) {
        if (!json.isJsonArray()) return null;
        JsonArray array = json.getAsJsonArray();
        return new Vec3(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    /**
     * Returns a point provider from a JSON object. If it's an inlined vector, like this: {@code "point": [1, 2, 3]},
     * returns a static provider. Otherwise, looks for a {@code type} field and returns the PointProvider corresponding
     * to that type.
     * @param json The JSON object to look for
     * @param name The name of the field
     * @return a point provider
     */
    public static PointProvider pointFromJson(JsonObject json, String name) {
        Vec3 test = vec3FromJson(json, name);
        if (test != null) return new StaticPointProvider(test);
        JsonObject obj;
        try {
            obj = GsonHelper.getAsJsonObject(json, name, null);
        } catch (JsonSyntaxException e) {
            obj = null;
        }
        if (obj == null) return null;
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(obj, "type"));
        PointSerializer<?> serializer = CutsceneManager.getPointType(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown point type: " + type);
        }
        return serializer.fromJSON(obj);
    }

    /**
     * Returns a point provider from a JSON object. If it's an inlined vector, like this: {@code "point": [1, 2, 3]},
     * returns a static provider. Otherwise, looks for a {@code type} field and returns the PointProvider corresponding
     * to that type.
     * @param json The JSON object to look for
     * @return a point provider
     */
    public static PointProvider pointFromJson(JsonElement json) {
        Vec3 test = vec3FromJson(json);
        if (test != null) return new StaticPointProvider(test);
        if (json.isJsonNull()) return null;
        JsonObject obj = json.getAsJsonObject();
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(obj, "type"));
        PointSerializer<?> serializer = CutsceneManager.getPointType(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown point type: " + type);
        }
        return serializer.fromJSON(obj);
    }

    /**
     * Gets a 4-long float array from a JSON object, if it's written in the form of [r, g, b, a] or "#RRGGBBAA"
     * @param json The JSON object to look in
     * @param name The name of the field
     * @param defaultAlpha Value of output[3], if it's not specified
     * @return the float array, if it's there and written correctly, or null otherwise
     * @throws IndexOutOfBoundsException if the color array is shorter than 3 numbers
     */
    public static float[] getColor(JsonObject json, String name, float defaultAlpha) {
        JsonElement element = json.get(name);
        if (element == null || element instanceof JsonNull) {
            return null;
        } else if (element instanceof JsonArray array) {
            if (array.size() < 4) {
                return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), defaultAlpha};
            } else {
                return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat()};
            }
        } else if (element instanceof JsonPrimitive string && string.isString()) {
            String value = string.getAsString();
            int r = Integer.valueOf(value.substring(0, 2), 16);
            int g = Integer.valueOf(value.substring(2, 4), 16);
            int b = Integer.valueOf(value.substring(4, 6), 16);
            int a;
            if (value.length() > 7) {
                a = Integer.valueOf(value.substring(6, 8), 16);
                return new float[]{r/255f, g/255f, b/255f, a/255f};
            } else {
                return new float[]{r/255f, g/255f, b/255f, defaultAlpha};
            }
        } else {
            return null;
        }
    }

    /**
     * Returns a JSON object from another object, or null if it's absent or null
     */
    public static @Nullable JsonObject getNullableObject(JsonObject json, String name) {
        if (json == null) return null;
        if (!json.has(name)) return null;
        JsonElement element = json.get(name);
        if (element.isJsonNull()) return null;
        return element.getAsJsonObject();
    }
}
