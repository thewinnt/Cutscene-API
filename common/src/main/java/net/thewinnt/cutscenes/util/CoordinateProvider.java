package net.thewinnt.cutscenes.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;

import java.util.function.DoubleUnaryOperator;

public record CoordinateProvider(boolean isAbsolute, Easing value, CoordinateAnchor anchor) {

    private Object getValue() {
        return value;
    }

    public float get(double t, float scale) {
        if (isAbsolute) return (float) anchor.apply(value.get(t) / scale) * scale;
        return (float) anchor.apply(value.get(t)) * scale;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(isAbsolute);
        Easing.toNetwork(value, buf);
        buf.writeEnum(anchor);
    }

    public static CoordinateProvider fromNetwork(FriendlyByteBuf buf) {
        boolean isAbsolute = buf.readBoolean();
        Easing value = Easing.fromNetwork(buf);
        CoordinateAnchor anchor = buf.readEnum(CoordinateAnchor.class);
        return new CoordinateProvider(isAbsolute, value, anchor);
    }

    public static CoordinateProvider fromJSON(JsonElement json, LoadingContext context, Easing fallback) {
        if (json == null || json.isJsonNull()) {
            return new CoordinateProvider(false, fallback, CoordinateAnchor.START);
        }
        if (json.isJsonPrimitive()) {
            return new CoordinateProvider(false, Easing.fromJSON(json, context, fallback), CoordinateAnchor.START);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            boolean absolute = GsonHelper.getAsBoolean(obj, "absolute", false);
            CoordinateAnchor anchor = CoordinateAnchor.valueOf(GsonHelper.getAsString(obj, "anchor", "start").toUpperCase());
            return new CoordinateProvider(absolute, Easing.fromJSON(json, context, fallback), anchor);
        } else {
            CutsceneAPI.LOGGER.warn("Invalid easing format for coordinate provider: {}", json);
            return new CoordinateProvider(false, Easing.fromJSON(json, context, fallback), CoordinateAnchor.START);
        }
    }

    public static CoordinateProvider fromJSON(JsonElement json, LoadingContext context) {
        if (json == null || json.isJsonNull()) {
            context.reportError("Missing required coordinate provider");
            return null;
        } else if (json.isJsonPrimitive()) {
            return new CoordinateProvider(false, context.wrapLoading("easing", () -> Easing.fromJSON(json, context)), CoordinateAnchor.START);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            boolean absolute = GsonHelper.getAsBoolean(obj, "absolute", false);
            CoordinateAnchor anchor = CoordinateAnchor.valueOf(GsonHelper.getAsString(obj, "anchor", "start").toUpperCase());
            return new CoordinateProvider(absolute, context.wrapLoading("easing", () -> Easing.fromJSON(json, context)), anchor);
        } else {
            context.reportError("Invalid object type: " + json);
            return null;
        }
    }

    public static CoordinateProvider loadWrapped(JsonObject json, String name, LoadingContext context) {
        return context.wrapLoading(name, () -> fromJSON(json.get(name), context));
    }

    public static CoordinateProvider loadWrapped(JsonObject json, String name, LoadingContext context, Easing fallback) {
        return context.wrapLoading(name, () -> fromJSON(json.get(name), context, fallback));
    }

    public enum CoordinateAnchor {
        START(x -> x),
        BEFORE_CENTER(x -> 0.5 - x),
        AFTER_CENTER(x -> 0.5 + x),
        END(x -> 1 - x);

        public static final EnumCodec<CoordinateAnchor> CODEC = EnumCodec.forClass(CoordinateAnchor.class);

        private final DoubleUnaryOperator operation;

        private CoordinateAnchor(DoubleUnaryOperator operation) {
            this.operation = operation;
        }

        public double apply(double x) {
            return operation.applyAsDouble(x);
        }
    }
}
