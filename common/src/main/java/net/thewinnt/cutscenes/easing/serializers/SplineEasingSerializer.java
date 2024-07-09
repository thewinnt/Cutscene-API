package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SplineEasing;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.util.LoadResolver;

public class SplineEasingSerializer implements EasingSerializer<SplineEasing> {
    public static final SplineEasingSerializer INSTANCE = new SplineEasingSerializer();

    private SplineEasingSerializer() {}

    @Override
    public SplineEasing fromNetwork(FriendlyByteBuf buf) {
        return new SplineEasing(CutsceneNetworkHandler.readArray(buf, Easing[]::new, Easing::fromNetwork));
    }

    @Override
    public SplineEasing fromJSON(JsonObject json) {
        JsonArray easings = GsonHelper.getAsJsonArray(json, "points");
        Easing[] data = new Easing[easings.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = Easing.fromJSON(easings.get(i));
        }
        return new SplineEasing(data);
    }

    @Override
    public SplineEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        JsonArray easings = GsonHelper.getAsJsonArray(json, "points");
        Easing[] data = new Easing[easings.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = Easing.fromJSON(easings.get(i), context);
        }
        return new SplineEasing(data);
    }
}
