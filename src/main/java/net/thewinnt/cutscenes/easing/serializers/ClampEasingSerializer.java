package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ClampEasing;

public class ClampEasingSerializer implements EasingSerializer<ClampEasing> {
    public static final ClampEasingSerializer INSTANCE = new ClampEasingSerializer();

    private ClampEasingSerializer() {}

    @Override
    public ClampEasing fromNetwork(FriendlyByteBuf buf) {
        Easing input = Easing.fromNetwork(buf);
        Easing min = Easing.fromNetwork(buf);
        Easing max = Easing.fromNetwork(buf);
        return new ClampEasing(input, min, max);
    }

    @Override
    public ClampEasing fromJSON(JsonObject json) {
        Easing input = Easing.fromJSON(json.get("input"));
        Easing min = Easing.fromJSON(json.get("min"));
        Easing max = Easing.fromJSON(json.get("max"));
        return new ClampEasing(input, min, max);
    }
}
