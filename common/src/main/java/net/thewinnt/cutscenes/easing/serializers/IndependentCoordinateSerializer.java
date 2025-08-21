package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.IndependentCoordinateEasing;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.JsonHelper;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

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
    public IndependentCoordinateEasing fromJSON(JsonObject json, LoadingContext context) {
        IndependentCoordinateEasing.Axis inAxis = IndependentCoordinateEasing.Axis.fromJSON(json, "in_axis", context);
        IndependentCoordinateEasing.Axis outAxis = IndependentCoordinateEasing.Axis.fromJSON(json, "out_axis", context);
        CoordinateProvider inCoord = CoordinateProvider.loadWrapped(json, "coordinate", context);
        return new IndependentCoordinateEasing(inAxis, outAxis, inCoord);
    }
}
