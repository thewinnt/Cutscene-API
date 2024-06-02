package net.thewinnt.cutscenes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.LerpEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record DynamicColor(Easing r, Easing g, Easing b, Easing a) {
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

    public static DynamicColor fromJSON(@Nonnull JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            Easing r = Easing.fromJSON(GsonHelper.getNonNull(obj, "r"));
            Easing g = Easing.fromJSON(GsonHelper.getNonNull(obj, "g"));
            Easing b = Easing.fromJSON(GsonHelper.getNonNull(obj, "b"));
            Easing a = Easing.fromJSON(obj.get("a"), ConstantEasing.ONE);
            return new DynamicColor(r, g, b, a);
        } else if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            Easing r = Easing.fromJSON(array.get(0));
            Easing g = Easing.fromJSON(array.get(1));
            Easing b = Easing.fromJSON(array.get(2));
            Easing a = Easing.fromJSON(JsonHelper.getFromArraySafe(array, 3), ConstantEasing.ONE);
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

    public static DynamicColor fromJSON(@Nullable JsonElement json, DynamicColor fallback) {
        if (json == null || json.isJsonNull()) {
            return fallback;
        }
        try {
            return fromJSON(json);
        } catch (RuntimeException e) {
            CutsceneAPI.LOGGER.warn("Exception loading DynamicColor, returning fallback: ", e);
            return fallback;
        }
    }
}
