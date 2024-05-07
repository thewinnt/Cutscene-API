package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.LoadResolver;

public record SimpleEasingSerializer(SimpleEasing easing) implements EasingSerializer<SimpleEasing> {

    @Override
    public SimpleEasing fromNetwork(FriendlyByteBuf buf) {
        return easing;
    }

    @Override
    public SimpleEasing fromJSON(JsonObject json) {
        return easing;
    }

    @Override
    public SimpleEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        return easing;
    }
}
