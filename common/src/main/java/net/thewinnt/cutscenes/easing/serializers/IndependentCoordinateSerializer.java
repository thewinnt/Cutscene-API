package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.IndependentCoordinateEasing;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.LoadResolver;

import java.util.Locale;

public class IndependentCoordinateSerializer implements EasingSerializer<IndependentCoordinateEasing> {
    public static final IndependentCoordinateSerializer INSTANCE = new IndependentCoordinateSerializer();

    private IndependentCoordinateSerializer() {}

    @Override
    public IndependentCoordinateEasing fromNetwork(FriendlyByteBuf buf) {
        IndependentCoordinateEasing.Axis inAxis = buf.readEnum(IndependentCoordinateEasing.Axis.class);
        IndependentCoordinateEasing.Axis outAxis = buf.readEnum(IndependentCoordinateEasing.Axis.class);
        CoordinateProvider inCoord = CoordinateProvider.fromNetwork(buf);
        return new IndependentCoordinateEasing(inAxis, outAxis, inCoord);
    }

    @Override
    public IndependentCoordinateEasing fromJSON(JsonObject json) {
        IndependentCoordinateEasing.Axis inAxis = IndependentCoordinateEasing.Axis.valueOf(GsonHelper.getAsString(json, "in_axis").toUpperCase(Locale.ROOT));
        IndependentCoordinateEasing.Axis outAxis = IndependentCoordinateEasing.Axis.valueOf(GsonHelper.getAsString(json, "out_axis").toUpperCase(Locale.ROOT));
        CoordinateProvider inCoord = CoordinateProvider.fromJSON(json.get("coordinate"));
        return new IndependentCoordinateEasing(inAxis, outAxis, inCoord);
    }

    @Override
    public IndependentCoordinateEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        return fromJSON(json);
    }
}
