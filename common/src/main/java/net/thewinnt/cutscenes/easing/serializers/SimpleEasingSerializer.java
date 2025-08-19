package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

public record SimpleEasingSerializer(SimpleEasing easing, MapCodec<SimpleEasing> codec) implements EasingSerializer<SimpleEasing> {
    public SimpleEasingSerializer(SimpleEasing easing) {
        this(easing, MapCodec.unit(easing));
    }

    @Override
    public SimpleEasing fromNetwork(FriendlyByteBuf buf) {
        return easing;
    }

    @Override
    public SimpleEasing fromJSON(JsonObject json, LoadingContext context) {
        return easing;
    }
}
