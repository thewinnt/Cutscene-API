package net.thewinnt.cutscenes.effect.configuration;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.FloatProvider;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.util.chardelays.DelayProvider;

public record AppearingTextConfiguration(
    Component text,
    CoordinateProvider rx,
    CoordinateProvider ry,
    CoordinateProvider width,
    boolean dropShadow,
    SoundEvent soundbite,
    DelayProvider delays,
    FloatProvider pitch
) {
    public static record CoordinateProvider(boolean isAbsolute, Easing value) {
        public int get(double t, int scale) {
            if (isAbsolute) return (int)value.get(t);
            return (int)(value.get(t) * scale);
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeBoolean(isAbsolute);
            Easing.toNetwork(value, buf);
        }

        public static CoordinateProvider fromNetwork(FriendlyByteBuf buf) {
            boolean isAbsolute = buf.readBoolean();
            Easing value = Easing.fromNetwork(buf);
            return new CoordinateProvider(isAbsolute, value);
        }

        public static CoordinateProvider fromJSON(JsonElement json, Easing fallback) {
            if (json.isJsonPrimitive()) {
                return new CoordinateProvider(false, Easing.fromJSON(json, fallback));
            } else if (json.isJsonObject()) {
                boolean absolute = GsonHelper.getAsBoolean(json.getAsJsonObject(), "absolute", false);
                return new CoordinateProvider(absolute, Easing.fromJSON(json, fallback));
            } else {
                CutsceneAPI.LOGGER.warn("Invalid easing format for coordinate provider: {}", json);
                return new CoordinateProvider(false, Easing.fromJSON(json, fallback));
            }
        }

        public static CoordinateProvider fromJSON(JsonElement json) {
            if (json.isJsonPrimitive()) {
                return new CoordinateProvider(false, Easing.fromJSON(json));
            } else if (json.isJsonObject()) {
                boolean absolute = GsonHelper.getAsBoolean(json.getAsJsonObject(), "absolute", false);
                return new CoordinateProvider(absolute, Easing.fromJSON(json));
            } else {
                throw new IllegalArgumentException("Illegal JSON for non-fallback CoordinateProvider");
            }
        }
    }
}
