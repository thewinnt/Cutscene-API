package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

public record SimpleEasingSerializer(SimpleEasing easing) implements EasingSerializer<SimpleEasing> {

    @Override
    public SimpleEasing fromNetwork(FriendlyByteBuf buf) {
        return easing;
    }

    @Override
    public SimpleEasing fromJSON(JsonObject json) {
        return easing;
    }
}
