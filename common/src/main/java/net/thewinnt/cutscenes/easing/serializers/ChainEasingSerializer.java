package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ChainEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

public class ChainEasingSerializer implements EasingSerializer<ChainEasing> {
    public static final ChainEasingSerializer INSTANCE = new ChainEasingSerializer();
    public static final MapCodec<ChainEasing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Easing.CODEC.fieldOf("argument").forGetter(t -> t.argumentProvider),
        Easing.CODEC.fieldOf("easing").forGetter(t -> t.easing)
    ).apply(instance, ChainEasing::new));

    private ChainEasingSerializer() {}

    @Override
    public ChainEasing fromNetwork(FriendlyByteBuf buf) {
        Easing argumentProvider = Easing.fromNetwork(buf);
        Easing easing = Easing.fromNetwork(buf);
        return new ChainEasing(argumentProvider, easing);
    }

    @Override
    public ChainEasing fromJSON(JsonObject json, LoadingContext context) {
        Easing argumentProvider = Easing.loadWrapped(json, "argument", context);
        Easing easing = Easing.loadWrapped(json, "easing", context);
        return new ChainEasing(argumentProvider, easing);
    }

    @Override
    public MapCodec<ChainEasing> codec() {
        return CODEC;
    }
}
