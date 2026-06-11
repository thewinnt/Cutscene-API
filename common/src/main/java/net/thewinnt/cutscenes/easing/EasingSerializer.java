package net.thewinnt.cutscenes.easing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.serializers.*;
import net.thewinnt.cutscenes.easing.types.*;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public interface EasingSerializer<T extends Easing> {
    BiMap<String, Easing> LEGACY_COMPAT = HashBiMap.create();
    Map<String, SimpleEasingSerializer> SIMPLE_EASINGS = new HashMap<>();
    Map<DoubleUnaryOperator, SingleArgumentEasingSerializer> SINGLE_ARGUMENT_EASINGS = new HashMap<>();
    Map<DoubleBinaryOperator, DoubleArgumentEasingSerializer> DOUBLE_ARGUMENT_EASINGS = new HashMap<>();

    EasingSerializer<SimpleEasing> LINEAR = registerSimple(Identifier.parse("cutscenes:linear"), SimpleEasing.LINEAR);
    EasingSerializer<SimpleEasing> IN_SINE = registerSimple(Identifier.parse("cutscenes:in_sine"), SimpleEasing.IN_SINE);
    EasingSerializer<SimpleEasing> OUT_SINE = registerSimple(Identifier.parse("cutscenes:out_sine"), SimpleEasing.OUT_SINE);
    EasingSerializer<SimpleEasing> IN_OUT_SINE = registerSimple(Identifier.parse("cutscenes:in_out_sine"), SimpleEasing.IN_OUT_SINE);
    EasingSerializer<SimpleEasing> IN_QUAD = registerSimple(Identifier.parse("cutscenes:in_quad"), SimpleEasing.IN_QUAD);
    EasingSerializer<SimpleEasing> OUT_QUAD = registerSimple(Identifier.parse("cutscenes:out_quad"), SimpleEasing.OUT_QUAD);
    EasingSerializer<SimpleEasing> IN_OUT_QUAD = registerSimple(Identifier.parse("cutscenes:in_out_quad"), SimpleEasing.IN_OUT_QUAD);
    EasingSerializer<SimpleEasing> IN_CUBIC = registerSimple(Identifier.parse("cutscenes:in_cubic"), SimpleEasing.IN_CUBIC);
    EasingSerializer<SimpleEasing> OUT_CUBIC = registerSimple(Identifier.parse("cutscenes:out_cubic"), SimpleEasing.OUT_CUBIC);
    EasingSerializer<SimpleEasing> IN_OUT_CUBIC = registerSimple(Identifier.parse("cutscenes:in_out_cubic"), SimpleEasing.IN_OUT_CUBIC);
    EasingSerializer<SimpleEasing> IN_QUART = registerSimple(Identifier.parse("cutscenes:in_quart"), SimpleEasing.IN_QUART);
    EasingSerializer<SimpleEasing> OUT_QUART = registerSimple(Identifier.parse("cutscenes:out_quart"), SimpleEasing.OUT_QUART);
    EasingSerializer<SimpleEasing> IN_OUT_QUART = registerSimple(Identifier.parse("cutscenes:in_out_quart"), SimpleEasing.IN_OUT_QUART);
    EasingSerializer<SimpleEasing> IN_QUINT = registerSimple(Identifier.parse("cutscenes:in_quint"), SimpleEasing.IN_QUINT);
    EasingSerializer<SimpleEasing> OUT_QUINT = registerSimple(Identifier.parse("cutscenes:out_quint"), SimpleEasing.OUT_QUINT);
    EasingSerializer<SimpleEasing> IN_OUT_QUINT = registerSimple(Identifier.parse("cutscenes:in_out_quint"), SimpleEasing.IN_OUT_QUINT);
    EasingSerializer<SimpleEasing> IN_EXPO = registerSimple(Identifier.parse("cutscenes:in_expo"), SimpleEasing.IN_EXPO);
    EasingSerializer<SimpleEasing> OUT_EXPO = registerSimple(Identifier.parse("cutscenes:out_expo"), SimpleEasing.OUT_EXPO);
    EasingSerializer<SimpleEasing> IN_OUT_EXPO = registerSimple(Identifier.parse("cutscenes:in_out_expo"), SimpleEasing.IN_OUT_EXPO);
    EasingSerializer<SimpleEasing> IN_CIRC = registerSimple(Identifier.parse("cutscenes:in_circ"), SimpleEasing.IN_CIRC);
    EasingSerializer<SimpleEasing> OUT_CIRC = registerSimple(Identifier.parse("cutscenes:out_circ"), SimpleEasing.OUT_CIRC);
    EasingSerializer<SimpleEasing> IN_OUT_CIRC = registerSimple(Identifier.parse("cutscenes:in_out_circ"), SimpleEasing.IN_OUT_CIRC);
    EasingSerializer<SimpleEasing> IN_BACK = registerSimple(Identifier.parse("cutscenes:in_back"), SimpleEasing.IN_BACK);
    EasingSerializer<SimpleEasing> OUT_BACK = registerSimple(Identifier.parse("cutscenes:out_back"), SimpleEasing.OUT_BACK);
    EasingSerializer<SimpleEasing> IN_OUT_BACK = registerSimple(Identifier.parse("cutscenes:in_out_back"), SimpleEasing.IN_OUT_BACK);
    EasingSerializer<SimpleEasing> IN_ELASTIC = registerSimple(Identifier.parse("cutscenes:in_elastic"), SimpleEasing.IN_ELASTIC);
    EasingSerializer<SimpleEasing> OUT_ELASTIC = registerSimple(Identifier.parse("cutscenes:out_elastic"), SimpleEasing.OUT_ELASTIC);
    EasingSerializer<SimpleEasing> IN_OUT_ELASTIC = registerSimple(Identifier.parse("cutscenes:in_out_elastic"), SimpleEasing.IN_OUT_ELASTIC);
    EasingSerializer<SimpleEasing> OUT_BOUNCE = registerSimple(Identifier.parse("cutscenes:out_bounce"), SimpleEasing.OUT_BOUNCE);
    EasingSerializer<SimpleEasing> IN_BOUNCE = registerSimple(Identifier.parse("cutscenes:in_bounce"), SimpleEasing.IN_BOUNCE);
    EasingSerializer<SimpleEasing> IN_OUT_BOUNCE = registerSimple(Identifier.parse("cutscenes:in_out_bounce"), SimpleEasing.IN_OUT_BOUNCE);
    EasingSerializer<ConstantEasing> CONSTANT = register(Identifier.parse("cutscenes:constant"), ConstantEasingSerializer.INSTANCE);
    EasingSerializer<CompoundEasing> COMPOUND = register(Identifier.parse("cutscenes:compound"), CompoundEasingSerializer.INSTANCE);
    EasingSerializer<ChainEasing> CHAIN = register(Identifier.parse("cutscenes:chain"), ChainEasingSerializer.INSTANCE);
    EasingSerializer<SingleArgumentEasing> ABS = registerSingleArg(Identifier.parse("cutscenes:abs"), Math::abs);
    EasingSerializer<SingleArgumentEasing> SQUARE = registerSingleArg(Identifier.parse("cutscenes:square"), t -> t * t);
    EasingSerializer<SingleArgumentEasing> CUBE = registerSingleArg(Identifier.parse("cutscenes:cube"), t -> t * t * t);
    EasingSerializer<SingleArgumentEasing> SQRT = registerSingleArg(Identifier.parse("cutscenes:sqrt"), Math::sqrt);
    EasingSerializer<SingleArgumentEasing> SIN = registerSingleArg(Identifier.parse("cutscenes:sin"), Math::sin);
    EasingSerializer<SingleArgumentEasing> COS = registerSingleArg(Identifier.parse("cutscenes:cos"), Math::cos);
    EasingSerializer<SingleArgumentEasing> TAN = registerSingleArg(Identifier.parse("cutscenes:tan"), Math::tan);
    EasingSerializer<SingleArgumentEasing> ASIN = registerSingleArg(Identifier.parse("cutscenes:asin"), Math::asin);
    EasingSerializer<SingleArgumentEasing> ACOS = registerSingleArg(Identifier.parse("cutscenes:acos"), Math::acos);
    EasingSerializer<SingleArgumentEasing> ATAN = registerSingleArg(Identifier.parse("cutscenes:atan"), Math::atan);
    EasingSerializer<SingleArgumentEasing> TO_DEGREES = registerSingleArg(Identifier.parse("cutscenes:to_degrees"), Math::toDegrees);
    EasingSerializer<SingleArgumentEasing> TO_RADIANS = registerSingleArg(Identifier.parse("cutscenes:to_radians"), Math::toRadians);
    EasingSerializer<DoubleArgumentEasing> ADD = registerDoubleArg(Identifier.parse("cutscenes:add"), Double::sum);
    EasingSerializer<DoubleArgumentEasing> SUBTRACT = registerDoubleArg(Identifier.parse("cutscenes:subtract"), (a, b) -> a - b);
    EasingSerializer<DoubleArgumentEasing> MUL = registerDoubleArg(Identifier.parse("cutscenes:mul"), (a, b) -> a * b);
    EasingSerializer<DoubleArgumentEasing> DIV = registerDoubleArg(Identifier.parse("cutscenes:div"), (a, b) -> a / b);
    EasingSerializer<DoubleArgumentEasing> MOD = registerDoubleArg(Identifier.parse("cutscenes:mod"), (a, b) -> a % b);
    EasingSerializer<DoubleArgumentEasing> POW = registerDoubleArg(Identifier.parse("cutscenes:pow"), Math::pow);
    EasingSerializer<DoubleArgumentEasing> MIN = registerDoubleArg(Identifier.parse("cutscenes:min"), Math::min);
    EasingSerializer<DoubleArgumentEasing> MAX = registerDoubleArg(Identifier.parse("cutscenes:max"), Math::max);
    EasingSerializer<DoubleArgumentEasing> ATAN2 = registerDoubleArg(Identifier.parse("cutscenes:atan2"), Math::atan2);
    EasingSerializer<ClampEasing> CLAMP = register(Identifier.parse("cutscenes:clamp"), ClampEasingSerializer.INSTANCE);
    EasingSerializer<SplineEasing> SPLINE = register(Identifier.parse("cutscenes:spline"), SplineEasingSerializer.INSTANCE);
    EasingSerializer<LerpEasing> LERP = register(Identifier.parse("cutscenes:lerp"), LerpEasingSerializer.INSTANCE);
    EasingSerializer<ColorEasing> COLOR = register(Identifier.parse("cutscenes:color"), ColorEasingSerializer.INSTANCE);
    EasingSerializer<IndependentCoordinateEasing> COORDINATE = register(Identifier.parse("cutscenes:coordinate"), IndependentCoordinateSerializer.INSTANCE);
    EasingSerializer<RangeChoiceEasing> RANGE_CHOICE = register(Identifier.parse("cutscenes:range_choice"), RangeChoiceSerializer.INSTANCE);
    EasingSerializer<RandomEasing> RANDOM = register(Identifier.parse("cutscenes:random"), RandomEasingSerializer.INSTANCE);


    T fromNetwork(FriendlyByteBuf buf);
    T fromJSON(JsonObject json, LoadingContext context);
    default MapCodec<T> codec() {
        return null;
    }

    static <T extends Easing> EasingSerializer<T> register(Identifier id, EasingSerializer<T> serializer) {
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static SimpleEasingSerializer registerSimple(Identifier id, SimpleEasing easing) {
        SimpleEasingSerializer serializer = new SimpleEasingSerializer(easing);
        LEGACY_COMPAT.put(id.getPath(), serializer.easing());
        SIMPLE_EASINGS.put(id.getPath(), serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static SingleArgumentEasingSerializer registerSingleArg(Identifier id, DoubleUnaryOperator operator) {
        SingleArgumentEasingSerializer serializer = new SingleArgumentEasingSerializer(operator);
        SINGLE_ARGUMENT_EASINGS.put(operator, serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static DoubleArgumentEasingSerializer registerDoubleArg(Identifier id, DoubleBinaryOperator operator) {
        DoubleArgumentEasingSerializer serializer = new DoubleArgumentEasingSerializer(operator);
        DOUBLE_ARGUMENT_EASINGS.put(operator, serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    /** We need this for the class to load */
    static void init() {}
}