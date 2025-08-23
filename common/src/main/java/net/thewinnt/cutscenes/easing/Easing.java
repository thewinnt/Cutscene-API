package net.thewinnt.cutscenes.easing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An easing smoothly transitions from value 0 to value 1. At least, in places it's meant to be an easing.
 * In other cases, it can be treated as a math function that takes in an argument in range [0, 1] and returns something
 * else from it. To get an idea what an easing (in its intended usage) is, as well as the visual representations of
 * {@link SimpleEasing simple (or legacy) easings}, check out <a href="https://easings.net">easings.net</a>
 * @see SimpleEasing
 */
public interface Easing {
    Map<ResourceLocation, Easing> EASING_MACROS = new HashMap<>();
    Codec<Easing> DISPATCH_CODEC = CutsceneAPI.EASING_SERIALIZERS.byNameCodec().dispatch(Easing::getSerializer, EasingSerializer::codec);
    Codec<Easing> MACRO_CODEC = ResourceLocation.CODEC.flatXmap(
            resourceLocation -> {
                if (EASING_MACROS.containsKey(resourceLocation)) {
                    return DataResult.success(EASING_MACROS.get(resourceLocation));
                } else {
                    return DataResult.error(() -> "Unknown easing macro: " + resourceLocation);
                }
            },
            easing -> DataResult.error(() -> "Cannot convert easing macros to their IDs")
    );
    Codec<Easing> CONSTANT_CODEC = Codec.DOUBLE.flatComapMap(
            ConstantEasing::new,
            easing -> {
                if (easing instanceof ConstantEasing(double value)) {
                    return DataResult.success(value);
                } else {
                    return DataResult.error(() -> "Easing not constant: " + easing);
                }
            }
    );
    Codec<Easing> SIMPLE_CODEC = Codec.STRING.comapFlatMap(s -> {
        if (EasingSerializer.LEGACY_COMPAT.containsKey(s)) {
            return DataResult.success(EasingSerializer.LEGACY_COMPAT.get(s));
        }
        return DataResult.error(() -> "Unknown simple easing: " + s);
    }, easing -> EasingSerializer.LEGACY_COMPAT.inverse().get(easing));
    Codec<Easing> SIMPLE_OR_CONSTANT_CODEC = Codec.either(CONSTANT_CODEC, SIMPLE_CODEC).flatComapMap(
            either -> either.map(Function.identity(), Function.identity()),
            easing -> {
                if (easing instanceof ConstantEasing) {
                    return DataResult.success(Either.left(easing));
                } else if (EasingSerializer.LEGACY_COMPAT.containsValue(easing)) {
                    return DataResult.success(Either.right(easing));
                } else {
                    return DataResult.error(() -> "Not a constant or simple easing: " + easing);
                }
            }
    );
    Codec<Easing> MACRO_OR_DISPATCH_CODEC = Codec.either(MACRO_CODEC, DISPATCH_CODEC)
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);
    Codec<Easing> CODEC = Codec.either(SIMPLE_OR_CONSTANT_CODEC, MACRO_OR_DISPATCH_CODEC).xmap(
            either -> either.map(Function.identity(), Function.identity()),
            easing -> {
                if (easing instanceof ConstantEasing) {
                    return Either.left(easing);
                } else if (EasingSerializer.LEGACY_COMPAT.containsValue(easing)) {
                    return Either.left(easing);
                } else {
                    return Either.right(easing);
                }
            }
    );

    Logger LOGGER = LogUtils.getLogger();
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

    static Easing loadWrapped(JsonObject json, String name, LoadingContext context) {
        return context.wrapLoading(name, () -> fromJSON(json.get(name), context));
    }

    static Easing loadWrapped(JsonObject json, String name, LoadingContext context, Easing fallback) {
        return context.wrapLoading(name, () -> fromJSON(json.get(name), context, fallback));
    }

    static Easing fromJSON(JsonElement json, LoadingContext context) {
        if (json == null || json.isJsonNull()) {
            context.reportError("Missing required easing");
            return null;
        } else if (json.isJsonPrimitive()) {
            return fromJSONPrimitive(json.getAsJsonPrimitive(), context);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            EasingSerializer<?> serializer = CutsceneAPI.EASING_SERIALIZERS.getValue(ResourceLocation.parse(obj.get("type").getAsString()));
            if (serializer == null) {
                context.reportError("Unknown easing type: " + GsonHelper.getAsString(obj, "type"));
                return null;
            }
            return serializer.fromJSON(obj, context);
        } else {
            context.reportError("Invalid object type: " + json);
            return null;
        }
    }


    static Easing fromJSON(@Nullable JsonElement json, LoadingContext context, Easing fallback) {
        if (json == null || json.isJsonNull()) {
            return fallback;
        }
        return fromJSON(json, context);
    }

    static Easing fromJSONPrimitive(JsonPrimitive json, LoadingContext context) {
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

        // then, a preloaded macro
        final ResourceLocation id = ResourceLocation.parse(value);
        if (EASING_MACROS.containsKey(id)) {
            return EASING_MACROS.get(id);
        }

        // then, a macro
        if (context.easings == null) {
            context.reportError("Missing easing macro: " + id);
            return null;
        }
        Easing output = context.easings.resolve(id);
        if (output == null) {
            context.reportError("Missing or invalid easing macro: " + id);
            return null;
        }
        return output;
    }

    static Easing fromNetwork(FriendlyByteBuf buf) {
        return CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
    }
}
