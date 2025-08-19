package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.DoubleArgumentEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

public record DoubleArgumentEasingSerializer(DoubleBinaryOperator operation) implements EasingSerializer<DoubleArgumentEasing> {
    private static final Map<DoubleBinaryOperator, MapCodec<DoubleArgumentEasing>> CODECS = new IdentityHashMap<>();

    public DoubleArgumentEasingSerializer {
        CODECS.put(operation, RecordCodecBuilder.mapCodec(instance -> instance.group(
            Easing.CODEC.fieldOf("arg1").forGetter(DoubleArgumentEasing::arg1),
            Easing.CODEC.fieldOf("arg2").forGetter(DoubleArgumentEasing::arg2),
            MapCodec.unit(operation).forGetter(DoubleArgumentEasing::operation)
        ).apply(instance, DoubleArgumentEasing::new)));
    }

    @Override
    public DoubleArgumentEasing fromNetwork(FriendlyByteBuf buf) {
        Easing arg1 = Easing.fromNetwork(buf);
        Easing arg2 = Easing.fromNetwork(buf);
        return new DoubleArgumentEasing(arg1, arg2, operation);
    }

    @Override
    public DoubleArgumentEasing fromJSON(JsonObject json, LoadingContext context) {
        Easing arg1 = Easing.fromJSON(json.get("arg1"), context);
        Easing arg2 = Easing.fromJSON(json.get("arg2"), context);
        return new DoubleArgumentEasing(arg1, arg2, operation);
    }

    @Override
    public MapCodec<DoubleArgumentEasing> codec() {
        return CODECS.get(operation);
    }
}
