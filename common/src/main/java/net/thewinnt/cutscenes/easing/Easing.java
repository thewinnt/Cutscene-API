package net.thewinnt.cutscenes.easing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * An easing smoothly transitions from value 0 to value 1. At least, in places it's meant to be an easing.
 * In other cases, it can be treated as a math function that takes in an argument in range [0, 1] and returns something
 * else from it. To get an idea what an easing (in its intended usage) is, as well as the visual representations of
 * {@link SimpleEasing simple (or legacy) easings}, check out <a href="https://easings.net">easings.net</a>
 * @see SimpleEasing
 */
public interface Easing {
    Logger LOGGER = LogUtils.getLogger();
    Map<ResourceLocation, Easing> EASING_MACROS = new HashMap<>();
    /**
     * Returns the eased value from given t
     * @param t the initial progress (linear)
     * @return the eased value
     */
    double get(double t);

    /**
     * @return a serializer that creates easings of this type
     */
    EasingSerializer<?> getSerializer();

    /**
     * @deprecated use static {@link Easing#toNetwork(Easing, FriendlyByteBuf)} instead
     */
    @Deprecated
    void toNetwork(FriendlyByteBuf buf);

    static void toNetwork(Easing easing, FriendlyByteBuf buf) {
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easing.getSerializer()));
        easing.toNetwork(buf);
    }

    static Easing fromJSON(@NotNull JsonElement json) {
        if (json.isJsonPrimitive()) {
            return fromJSONPrimitive(json.getAsJsonPrimitive());
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            EasingSerializer<?> serializer = CutsceneAPI.EASING_SERIALIZERS.get(ResourceLocation.parse(GsonHelper.getAsString(obj, "type")));
            if (serializer == null) {
                throw new IllegalArgumentException("Unknown easing type: " + GsonHelper.getAsString(obj, "type"));
            }
            return serializer.fromJSON(obj);
        }
        throw new IllegalArgumentException("Cannot get Easing from JSON: " + json);
    }

    static Easing fromJSON(@NotNull JsonElement json, LoadResolver<Easing> context) {
        if (json.isJsonPrimitive()) {
            return fromJSONPrimitive(json.getAsJsonPrimitive(), context);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            EasingSerializer<?> serializer = CutsceneAPI.EASING_SERIALIZERS.get(ResourceLocation.parse(obj.get("type").getAsString()));
            if (serializer == null) {
                throw new IllegalArgumentException("Unknown easing type: " + GsonHelper.getAsString(obj, "type"));
            }
            return serializer.fromJSON(obj, context);
        }
        throw new IllegalArgumentException("Cannot get Easing from JSON: " + json);
    }


    static Easing fromJSON(@Nullable JsonElement json, Easing fallback) {
        if (json == null || json.isJsonNull()) {
            return fallback;
        }
        try {
            return fromJSON(json);
        } catch (RuntimeException e) {
            LOGGER.warn("Exception loading easing, returning fallback: ", e);
            return fallback;
        }
    }

    static Easing fromJSONPrimitive(JsonPrimitive json) {
        // if it's a number, return that first
        try {
            return new ConstantEasing(json.getAsDouble());
        } catch (NumberFormatException ignored) {}

        // if it's a string, try returning a constant first
        String value = json.getAsString();
        if ("t".equals(value)) return SimpleEasing.LINEAR;
        if ("pi".equals(value)) return ConstantEasing.PI;
        if ("e".equals(value)) return ConstantEasing.E;

        // then, a legacy easing
        if (EasingSerializer.LEGACY_COMPAT.containsKey(value)) {
            return EasingSerializer.LEGACY_COMPAT.get(value);
        }

        // then, a macro
        ResourceLocation test = ResourceLocation.parse(value);
        if (EASING_MACROS.containsKey(test)) {
            return EASING_MACROS.get(test);
        } else {
            // if nothing is found, throw an exception
            throw new IllegalArgumentException("Invalid or unknown easing: " + json);
        }
    }

    static Easing fromJSONPrimitive(JsonPrimitive json, LoadResolver<Easing> context) {
        // if it's a number, return that first
        try {
            return new ConstantEasing(json.getAsDouble());
        } catch (NumberFormatException ignored) {}

        // if it's a string, try returning a constant first
        String value = json.getAsString();
        if ("t".equals(value)) return SimpleEasing.LINEAR;
        if ("pi".equals(value)) return ConstantEasing.PI;
        if ("e".equals(value)) return ConstantEasing.E;

        // then, a legacy easing
        if (EasingSerializer.LEGACY_COMPAT.containsKey(value)) {
            return EasingSerializer.LEGACY_COMPAT.get(value);
        }

        // then, a macro
        ResourceLocation test = ResourceLocation.parse(value);
        return context.resolve(test);
    }

    static Easing fromNetwork(FriendlyByteBuf buf) {
        return CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
    }
}
