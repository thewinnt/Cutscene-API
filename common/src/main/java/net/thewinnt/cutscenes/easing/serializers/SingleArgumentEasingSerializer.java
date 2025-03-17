package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.DoubleArgumentEasing;
import net.thewinnt.cutscenes.easing.types.SingleArgumentEasing;
import net.thewinnt.cutscenes.util.LoadResolver;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public record SingleArgumentEasingSerializer(DoubleUnaryOperator operation) implements EasingSerializer<SingleArgumentEasing> {
    private static final Map<DoubleUnaryOperator, MapCodec<SingleArgumentEasing>> CODECS = new IdentityHashMap<>();

    public SingleArgumentEasingSerializer {
        CODECS.put(operation, RecordCodecBuilder.mapCodec(instance -> instance.group(
            Easing.CODEC.fieldOf("arg").forGetter(SingleArgumentEasing::argument),
            MapCodec.unit(operation).forGetter(SingleArgumentEasing::operation)
        ).apply(instance, SingleArgumentEasing::new)));
    }

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

    @Override
    public MapCodec<SingleArgumentEasing> codec() {
        return CODECS.get(operation);
    }
}
