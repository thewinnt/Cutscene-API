package net.thewinnt.cutscenes.easing.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CompoundEasing implements Easing {
    private final List<TimedEasingEntry> times;

    public CompoundEasing(List<TimedEasingEntry> times) {
        if (times.isEmpty()) {
            throw new IllegalArgumentException("A CompoundEasing must have at least 1 argument");
        }
        this.times = times;
    }

    @Override
    public double get(double t) {
        TimedEasingEntry entry = null;
        double startTime = 0;
        double endTime = 1;
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i).time > t) {
                entry = times.get(i - 1);
                startTime = entry.time;
                endTime = times.get(i).time;
                break;
            }
        }
        if (entry == null) {
            entry = times.get(times.size() - 1);
            startTime = entry.time;
            endTime = 1;
        }
        return entry.easing().get((t - startTime) / (endTime - startTime));
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.COMPOUND;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(times.size());
        for (TimedEasingEntry i : times) {
            buf.writeDouble(i.time());
            buf.writeDouble(i.easing().minValue());
            buf.writeDouble(i.easing().maxValue());
            Easing.toNetwork(i.easing().easing(), buf);
        }
    }

    public record RangeAppliedEasing(double minValue, double maxValue, Easing easing) {
        public double get(double t) {
            t = minValue + t * (maxValue - minValue);
            return easing.get(t);
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeDouble(minValue);
            buf.writeDouble(maxValue);
            Easing.toNetwork(easing, buf);
        }

        public static RangeAppliedEasing fromNetwork(FriendlyByteBuf buf) {
            double minValue = buf.readDouble();
            double maxValue = buf.readDouble();
            Easing easing = Easing.fromNetwork(buf);
            return new RangeAppliedEasing(minValue, maxValue, easing);
        }

        public static RangeAppliedEasing fromJSON(JsonElement json) {
            if (json.isJsonPrimitive()) {
                Easing easing = Easing.fromJSONPrimitive(json.getAsJsonPrimitive());
                return new RangeAppliedEasing(0, 1, easing);
            }
            JsonObject obj = json.getAsJsonObject();
            double minValue = GsonHelper.getAsDouble(obj, "from", 0);
            double maxValue = GsonHelper.getAsDouble(obj, "to", 1);
            Easing easing = Easing.fromJSON(obj.get("easing"));
            return new RangeAppliedEasing(minValue, maxValue, easing);
        }
    }

    public record TimedEasingEntry(double time, RangeAppliedEasing easing) implements Comparable<Double> {
        @Override
        public int compareTo(@NotNull Double o) {
            return time - o < 0 ? -1 : time == o ? 0 : 1;
        }
    }
}
