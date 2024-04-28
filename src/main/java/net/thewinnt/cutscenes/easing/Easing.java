package net.thewinnt.cutscenes.easing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public interface Easing {
    Map<ResourceLocation, Easing> EASING_MACROS = new HashMap<>();
    /**
     * Returns the eased value from given t
     * @param t the initial progress (linear)
     * @return the eased value
     */
    double get(double t);

    EasingSerializer<?> getSerializer();

    /**
     * @deprecated use {@link Easing#toNetwork(Easing, FriendlyByteBuf)} instead
     */
    @Deprecated
    void toNetwork(FriendlyByteBuf buf);

    static void toNetwork(Easing easing, FriendlyByteBuf buf) {
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easing.getSerializer()));
        easing.toNetwork(buf);
    }

    static Easing fromJSON(@Nonnull JsonElement json) {
        if (json.isJsonPrimitive()) {
            return fromJSONPrimitive(json.getAsJsonPrimitive());
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            EasingSerializer<?> serializer = CutsceneAPI.EASING_SERIALIZERS.get(new ResourceLocation(obj.get("type").getAsString()));
            return serializer.fromJSON(obj);
        }
        throw new IllegalArgumentException("Cannot get Easing from JSON: " + json);
    }

    static Easing fromJSON(@Nullable JsonElement json, Easing fallback) {
        if (json == null) {
            return fallback;
        }
        try {
            return fromJSON(json);
        } catch (RuntimeException e) {
            CutsceneAPI.LOGGER.warn("Exception loading easing, returning fallback: ", e);
            return fallback;
        }
    }

    static Easing fromJSONPrimitive(JsonPrimitive json) {
        String value = json.getAsString();
        if ("t".equals(value)) return SimpleEasing.LINEAR;
        if ("pi".equals(value)) return ConstantEasing.PI;
        if ("e".equals(value)) return ConstantEasing.E;
        if (EasingSerializer.LEGACY_COMPAT.containsKey(value)) {
            return EasingSerializer.LEGACY_COMPAT.get(value);
        }
        ResourceLocation test = new ResourceLocation(value);
        if (EASING_MACROS.containsKey(test)) {
            return EASING_MACROS.get(test);
        }
        return new ConstantEasing(json.getAsDouble());
    }

    static Easing fromNetwork(FriendlyByteBuf buf) {
        return CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
    }
}
