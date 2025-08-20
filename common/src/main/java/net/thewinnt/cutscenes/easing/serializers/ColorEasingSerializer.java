package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ColorEasing;
import net.thewinnt.cutscenes.easing.types.ColorEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

public class ColorEasingSerializer implements EasingSerializer<ColorEasing> {
    public static final ColorEasingSerializer INSTANCE = new ColorEasingSerializer();
    public static final MapCodec<ColorEasing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Easing.CODEC.fieldOf("delta").forGetter(ColorEasing::delta),
            Easing.CODEC.fieldOf("from").forGetter(ColorEasing::from),
            Easing.CODEC.fieldOf("to").forGetter(ColorEasing::to)
    ).apply(instance, ColorEasing::new));

    private ColorEasingSerializer() {}

    @Override
    public ColorEasing fromNetwork(FriendlyByteBuf buf) {
        Easing delta = Easing.fromNetwork(buf);
        Easing from = Easing.fromNetwork(buf);
        Easing to = Easing.fromNetwork(buf);
        return new ColorEasing(delta, from, to);
    }

    @Override
    public ColorEasing fromJSON(JsonObject json, LoadingContext context) {
        Easing delta = Easing.loadWrapped(json, "delta", context);
        Easing from = Easing.loadWrapped(json, "from", context);
        Easing to = Easing.loadWrapped(json, "to", context);
        return new ColorEasing(delta, from, to);
    }

    @Override
    public MapCodec<ColorEasing> codec() {
        return CODEC;
    }
}
