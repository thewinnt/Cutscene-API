package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.LerpEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.LoadResolver;

public class LerpEasingSerializer implements EasingSerializer<LerpEasing> {
    public static final LerpEasingSerializer INSTANCE = new LerpEasingSerializer();

    private LerpEasingSerializer() {}

    @Override
    public LerpEasing fromNetwork(FriendlyByteBuf buf) {
        Easing delta = Easing.fromNetwork(buf);
        Easing from = Easing.fromNetwork(buf);
        Easing to = Easing.fromNetwork(buf);
        return new LerpEasing(delta, from, to);
    }

    @Override
    public LerpEasing fromJSON(JsonObject json) {
        Easing delta = Easing.fromJSON(json.get("delta"), SimpleEasing.LINEAR);
        Easing from = Easing.fromJSON(json.get("from"));
        Easing to = Easing.fromJSON(json.get("to"));
        return new LerpEasing(delta, from, to);
    }

    @Override
    public LerpEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        Easing delta = Easing.fromJSON(json.get("delta"), context);
        Easing from = Easing.fromJSON(json.get("from"), context);
        Easing to = Easing.fromJSON(json.get("to"), context);
        return new LerpEasing(delta, from, to);
    }
}
