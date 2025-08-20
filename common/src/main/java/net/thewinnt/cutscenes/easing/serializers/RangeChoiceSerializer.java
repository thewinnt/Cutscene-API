package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.RangeChoiceEasing;
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
    public RangeChoiceEasing fromJSON(JsonObject json, LoadingContext loadResolver) {
        Easing inRange = Easing.loadWrapped(json, "in_range", loadResolver);
        Easing outRange = Easing.loadWrapped(json, "out_range", loadResolver);
        double rangeMin = GsonHelper.getAsDouble(json, "range_min");
        double rangeMax = GsonHelper.getAsDouble(json, "range_max");
        return new RangeChoiceEasing(inRange, outRange, rangeMin, rangeMax);
    }
}
