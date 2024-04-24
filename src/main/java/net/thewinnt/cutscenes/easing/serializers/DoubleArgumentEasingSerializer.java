package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.DoubleArgumentEasing;

import java.util.function.DoubleBinaryOperator;

public record DoubleArgumentEasingSerializer(DoubleBinaryOperator operation) implements EasingSerializer<DoubleArgumentEasing> {
    @Override
    public DoubleArgumentEasing fromNetwork(FriendlyByteBuf buf) {
        Easing arg1 = Easing.fromNetwork(buf);
        Easing arg2 = Easing.fromNetwork(buf);
        return new DoubleArgumentEasing(arg1, arg2, operation);
    }

    @Override
    public DoubleArgumentEasing fromJSON(JsonObject json) {
        Easing arg1 = Easing.fromJSON(json.get("arg1"));
        Easing arg2 = Easing.fromJSON(json.get("arg2"));
        return new DoubleArgumentEasing(arg1, arg2, operation);
    }
}
