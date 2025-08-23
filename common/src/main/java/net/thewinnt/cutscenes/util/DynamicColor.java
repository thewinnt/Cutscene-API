package net.thewinnt.cutscenes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public record DynamicColor(Easing r, Easing g, Easing b, Easing a) {
    public static final DynamicColor BLACK = new DynamicColor(ConstantEasing.ZERO, ConstantEasing.ZERO, ConstantEasing.ZERO, ConstantEasing.ONE);
    public static final DynamicColor WHITE = new DynamicColor(ConstantEasing.ONE, ConstantEasing.ONE, ConstantEasing.ONE, ConstantEasing.ONE);
    public static final Codec<DynamicColor> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Easing.CODEC.fieldOf("r").forGetter(DynamicColor::r),
        Easing.CODEC.fieldOf("g").forGetter(DynamicColor::g),
        Easing.CODEC.fieldOf("b").forGetter(DynamicColor::b),
        Easing.CODEC.fieldOf("a").orElse(ConstantEasing.ONE).forGetter(DynamicColor::a)
    ).apply(instance, DynamicColor::new));
    public static final Codec<DynamicColor> STRING_CODEC = Codec.string(6, 8).flatXmap(
        string -> {
            int r = Integer.valueOf(string.substring(0, 2), 16);
            int g = Integer.valueOf(string.substring(2, 4), 16);
            int b = Integer.valueOf(string.substring(4, 6), 16);
            if (string.length() > 7) {
                int a = Integer.valueOf(string.substring(6, 8), 16);
                return DataResult.success(new DynamicColor(new ConstantEasing(r/255.0), new ConstantEasing(g/255.0), new ConstantEasing(b/255.0), new ConstantEasing(a/255.0)));
            } else {
                return DataResult.success(new DynamicColor(new ConstantEasing(r/255.0), new ConstantEasing(g/255.0), new ConstantEasing(b/255.0), ConstantEasing.ONE));
            }
        },
        color -> DataResult.error(() -> "Cannot convert a DynamicColor to string")
    );
    public static final Codec<DynamicColor> LIST_CODEC = Easing.CODEC.listOf(3, 4).xmap(
        easings -> {
            if (easings.size() == 3) {
                return new DynamicColor(easings.get(0), easings.get(1), easings.get(2), ConstantEasing.ONE);
            }
            return new DynamicColor(easings.get(0), easings.get(1), easings.get(2), easings.get(3));
        },
        color -> List.of(color.r, color.g, color.b, color.a)
    );
    public static final Codec<DynamicColor> CODEC = Codec.either(
        STRING_CODEC,
        Codec.either(DIRECT_CODEC, LIST_CODEC).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left)
    ).xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);

    public int toARGB(double t) {
        int r = (int) Mth.clamp(this.r.get(t) * 255, 0, 255);
        int g = (int) Mth.clamp(this.g.get(t) * 255, 0, 255);
        int b = (int) Mth.clamp(this.b.get(t) * 255, 0, 255);
        int a = (int) Mth.clamp(this.a.get(t) * 255, 0, 255);
        return FastColor.ARGB32.color(a, r, g, b);
    }

    public float[] sample(double t) {
        return new float[]{(float) r.get(t), (float) g.get(t), (float) b.get(t), (float) a.get(t)};
    }

    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(r, buf);
        Easing.toNetwork(g, buf);
        Easing.toNetwork(b, buf);
        Easing.toNetwork(a, buf);
    }

    public static DynamicColor fromNetwork(FriendlyByteBuf buf) {
        Easing r = Easing.fromNetwork(buf);
        Easing g = Easing.fromNetwork(buf);
        Easing b = Easing.fromNetwork(buf);
        Easing a = Easing.fromNetwork(buf);
        return new DynamicColor(r, g, b, a);
    }

    public static DynamicColor loadWrapped(JsonObject json, String name, LoadingContext context) {
        return context.wrapLoading(name, () -> fromJSON(json.get(name), context));
    }

    public static DynamicColor loadWrapped(JsonObject json, String name, LoadingContext context, DynamicColor fallback) {
        return context.wrapLoading(name, () -> fromJSON(json.get(name), context, fallback));
    }

    public static DynamicColor fromJSON(@NotNull JsonElement json, LoadingContext context) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            Easing r = Easing.loadWrapped(obj, "r", context);
            Easing g = Easing.loadWrapped(obj, "g", context);
            Easing b = Easing.loadWrapped(obj, "b", context);
            Easing a = Easing.loadWrapped(obj, "a", context, ConstantEasing.ONE);
            return new DynamicColor(r, g, b, a);
        } else if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            Easing r = context.wrapLoading("r (#0)", () -> Easing.fromJSON(array.get(0), context));
            Easing g = context.wrapLoading("g (#1)", () -> Easing.fromJSON(array.get(1), context));
            Easing b = context.wrapLoading("b (#2)", () -> Easing.fromJSON(array.get(2), context));
            Easing a = context.wrapLoading("a (#3)", () -> Easing.fromJSON(JsonHelper.getFromArraySafe(array, 3), context, ConstantEasing.ONE));
            return new DynamicColor(r, g, b, a);
        } else {
            String color = json.getAsString();
            int r = Integer.valueOf(color.substring(0, 2), 16);
            int g = Integer.valueOf(color.substring(2, 4), 16);
            int b = Integer.valueOf(color.substring(4, 6), 16);
            if (color.length() > 7) {
                int a = Integer.valueOf(color.substring(6, 8), 16);
                return new DynamicColor(new ConstantEasing(r/255.0), new ConstantEasing(g/255.0), new ConstantEasing(b/255.0), new ConstantEasing(a/255.0));
            } else {
                return new DynamicColor(new ConstantEasing(r/255.0), new ConstantEasing(g/255.0), new ConstantEasing(b/255.0), ConstantEasing.ONE);
            }
        }
    }

    public static DynamicColor fromJSON(@Nullable JsonElement json, LoadingContext context, DynamicColor fallback) {
        if (json == null || json.isJsonNull()) {
            return fallback;
        }
        try {
            return fromJSON(json, context);
        } catch (RuntimeException e) {
            CutsceneAPI.LOGGER.warn("Exception loading DynamicColor, returning fallback: ", e);
            return fallback;
        }
    }
}
