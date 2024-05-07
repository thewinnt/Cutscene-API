package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SingleArgumentEasing;
import net.thewinnt.cutscenes.util.LoadResolver;

import java.util.function.DoubleUnaryOperator;

public record SingleArgumentEasingSerializer(DoubleUnaryOperator operation) implements EasingSerializer<SingleArgumentEasing> {
    @Override
    public SingleArgumentEasing fromNetwork(FriendlyByteBuf buf) {
        return new SingleArgumentEasing(Easing.fromNetwork(buf), operation);
    }

    @Override
    public SingleArgumentEasing fromJSON(JsonObject json) {
        return new SingleArgumentEasing(Easing.fromJSON(json.get("arg")), operation);
    }

    @Override
    public SingleArgumentEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        return new SingleArgumentEasing(Easing.fromJSON(json.get("arg"), context), operation);
    }
}
