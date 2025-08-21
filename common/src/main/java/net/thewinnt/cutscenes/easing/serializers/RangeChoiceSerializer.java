package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.RangeChoiceEasing;
import net.thewinnt.cutscenes.util.JsonHelper;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

public class RangeChoiceSerializer implements EasingSerializer<RangeChoiceEasing> {
    public static final RangeChoiceSerializer INSTANCE = new RangeChoiceSerializer();

    private RangeChoiceSerializer() {}

    @Override
    public RangeChoiceEasing fromNetwork(FriendlyByteBuf buf) {
        Easing inRange = Easing.fromNetwork(buf);
        Easing outRange = Easing.fromNetwork(buf);
        double rangeMin = buf.readDouble();
        double rangeMax = buf.readDouble();
        return new RangeChoiceEasing(inRange, outRange, rangeMin, rangeMax);
    }

    @Override
    public RangeChoiceEasing fromJSON(JsonObject json, LoadingContext context) {
        Easing inRange = Easing.loadWrapped(json, "in_range", context);
        Easing outRange = Easing.loadWrapped(json, "out_range", context);
        double rangeMin = JsonHelper.getAsDouble(json, "range_min", context);
        double rangeMax = JsonHelper.getAsDouble(json, "range_max", context);
        return new RangeChoiceEasing(inRange, outRange, rangeMin, rangeMax);
    }
}
