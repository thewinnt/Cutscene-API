package net.thewinnt.cutscenes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;

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
            return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
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
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
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
        return serializer.fromJSON(obj);
    }
}
