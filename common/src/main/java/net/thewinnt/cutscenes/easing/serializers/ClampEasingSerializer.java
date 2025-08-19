package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ClampEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

public class ClampEasingSerializer implements EasingSerializer<ClampEasing> {
    public static final ClampEasingSerializer INSTANCE = new ClampEasingSerializer();
    public static final MapCodec<ClampEasing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Easing.CODEC.fieldOf("input").forGetter(t -> t.input),
        Easing.CODEC.fieldOf("min").forGetter(t -> t.min),
        Easing.CODEC.fieldOf("max").forGetter(t -> t.max)
    ).apply(instance, ClampEasing::new));

    private ClampEasingSerializer() {}

    @Override
    public ClampEasing fromNetwork(FriendlyByteBuf buf) {
        Easing input = Easing.fromNetwork(buf);
        Easing min = Easing.fromNetwork(buf);
        Easing max = Easing.fromNetwork(buf);
        return new ClampEasing(input, min, max);
    }

    @Override
    public ClampEasing fromJSON(JsonObject json, LoadingContext context) {
        Easing input = Easing.fromJSON(json.get("input"), context);
        Easing min = Easing.fromJSON(json.get("min"), context);
        Easing max = Easing.fromJSON(json.get("max"), context);
        return new ClampEasing(input, min, max);
    }

    @Override
    public MapCodec<ClampEasing> codec() {
        return CODEC;
    }
}
