package net.thewinnt.cutscenes.easing;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.serializers.*;
import net.thewinnt.cutscenes.easing.types.*;
import net.thewinnt.cutscenes.util.LoadResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public interface EasingSerializer<T extends Easing> {
    Map<String, Easing> LEGACY_COMPAT = new HashMap<>();
    Map<String, SimpleEasingSerializer> SIMPLE_EASINGS = new HashMap<>();
    Map<DoubleUnaryOperator, SingleArgumentEasingSerializer> SINGLE_ARGUMENT_EASINGS = new HashMap<>();
    Map<DoubleBinaryOperator, DoubleArgumentEasingSerializer> DOUBLE_ARGUMENT_EASINGS = new HashMap<>();

    EasingSerializer<SimpleEasing> LINEAR = registerSimple(ResourceLocation.parse("cutscenes:linear"), SimpleEasing.LINEAR);
    EasingSerializer<SimpleEasing> IN_SINE = registerSimple(ResourceLocation.parse("cutscenes:in_sine"), SimpleEasing.IN_SINE);
    EasingSerializer<SimpleEasing> OUT_SINE = registerSimple(ResourceLocation.parse("cutscenes:out_sine"), SimpleEasing.OUT_SINE);
    EasingSerializer<SimpleEasing> IN_OUT_SINE = registerSimple(ResourceLocation.parse("cutscenes:in_out_sine"), SimpleEasing.IN_OUT_SINE);
    EasingSerializer<SimpleEasing> IN_QUAD = registerSimple(ResourceLocation.parse("cutscenes:in_quad"), SimpleEasing.IN_QUAD);
    EasingSerializer<SimpleEasing> OUT_QUAD = registerSimple(ResourceLocation.parse("cutscenes:out_quad"), SimpleEasing.OUT_QUAD);
    EasingSerializer<SimpleEasing> IN_OUT_QUAD = registerSimple(ResourceLocation.parse("cutscenes:in_out_quad"), SimpleEasing.IN_OUT_QUAD);
    EasingSerializer<SimpleEasing> IN_CUBIC = registerSimple(ResourceLocation.parse("cutscenes:in_cubic"), SimpleEasing.IN_CUBIC);
    EasingSerializer<SimpleEasing> OUT_CUBIC = registerSimple(ResourceLocation.parse("cutscenes:out_cubic"), SimpleEasing.OUT_CUBIC);
    EasingSerializer<SimpleEasing> IN_OUT_CUBIC = registerSimple(ResourceLocation.parse("cutscenes:in_out_cubic"), SimpleEasing.IN_OUT_CUBIC);
    EasingSerializer<SimpleEasing> IN_QUART = registerSimple(ResourceLocation.parse("cutscenes:in_quart"), SimpleEasing.IN_QUART);
    EasingSerializer<SimpleEasing> OUT_QUART = registerSimple(ResourceLocation.parse("cutscenes:out_quart"), SimpleEasing.OUT_QUART);
    EasingSerializer<SimpleEasing> IN_OUT_QUART = registerSimple(ResourceLocation.parse("cutscenes:in_out_quart"), SimpleEasing.IN_OUT_QUART);
    EasingSerializer<SimpleEasing> IN_QUINT = registerSimple(ResourceLocation.parse("cutscenes:in_quint"), SimpleEasing.IN_QUINT);
    EasingSerializer<SimpleEasing> OUT_QUINT = registerSimple(ResourceLocation.parse("cutscenes:out_quint"), SimpleEasing.OUT_QUINT);
    EasingSerializer<SimpleEasing> IN_OUT_QUINT = registerSimple(ResourceLocation.parse("cutscenes:in_out_quint"), SimpleEasing.IN_OUT_QUINT);
    EasingSerializer<SimpleEasing> IN_EXPO = registerSimple(ResourceLocation.parse("cutscenes:in_expo"), SimpleEasing.IN_EXPO);
    EasingSerializer<SimpleEasing> OUT_EXPO = registerSimple(ResourceLocation.parse("cutscenes:out_expo"), SimpleEasing.OUT_EXPO);
    EasingSerializer<SimpleEasing> IN_OUT_EXPO = registerSimple(ResourceLocation.parse("cutscenes:in_out_expo"), SimpleEasing.IN_OUT_EXPO);
    EasingSerializer<SimpleEasing> IN_CIRC = registerSimple(ResourceLocation.parse("cutscenes:in_circ"), SimpleEasing.IN_CIRC);
    EasingSerializer<SimpleEasing> OUT_CIRC = registerSimple(ResourceLocation.parse("cutscenes:out_circ"), SimpleEasing.OUT_CIRC);
    EasingSerializer<SimpleEasing> IN_OUT_CIRC = registerSimple(ResourceLocation.parse("cutscenes:in_out_circ"), SimpleEasing.IN_OUT_CIRC);
    EasingSerializer<SimpleEasing> IN_BACK = registerSimple(ResourceLocation.parse("cutscenes:in_back"), SimpleEasing.IN_BACK);
    EasingSerializer<SimpleEasing> OUT_BACK = registerSimple(ResourceLocation.parse("cutscenes:out_back"), SimpleEasing.OUT_BACK);
    EasingSerializer<SimpleEasing> IN_OUT_BACK = registerSimple(ResourceLocation.parse("cutscenes:in_out_back"), SimpleEasing.IN_OUT_BACK);
    EasingSerializer<SimpleEasing> IN_ELASTIC = registerSimple(ResourceLocation.parse("cutscenes:in_elastic"), SimpleEasing.IN_ELASTIC);
    EasingSerializer<SimpleEasing> OUT_ELASTIC = registerSimple(ResourceLocation.parse("cutscenes:out_elastic"), SimpleEasing.OUT_ELASTIC);
    EasingSerializer<SimpleEasing> IN_OUT_ELASTIC = registerSimple(ResourceLocation.parse("cutscenes:in_out_elastic"), SimpleEasing.IN_OUT_ELASTIC);
    EasingSerializer<SimpleEasing> OUT_BOUNCE = registerSimple(ResourceLocation.parse("cutscenes:out_bounce"), SimpleEasing.OUT_BOUNCE);
    EasingSerializer<SimpleEasing> IN_BOUNCE = registerSimple(ResourceLocation.parse("cutscenes:in_bounce"), SimpleEasing.IN_BOUNCE);
    EasingSerializer<SimpleEasing> IN_OUT_BOUNCE = registerSimple(ResourceLocation.parse("cutscenes:in_out_bounce"), SimpleEasing.IN_OUT_BOUNCE);
    EasingSerializer<ConstantEasing> CONSTANT = register(ResourceLocation.parse("cutscenes:constant"), ConstantEasingSerializer.INSTANCE);
    EasingSerializer<CompoundEasing> COMPOUND = register(ResourceLocation.parse("cutscenes:compound"), CompoundEasingSerializer.INSTANCE);
    EasingSerializer<ChainEasing> CHAIN = register(ResourceLocation.parse("cutscenes:chain"), ChainEasingSerializer.INSTANCE);
    EasingSerializer<SingleArgumentEasing> ABS = registerSingleArg(ResourceLocation.parse("cutscenes:abs"), Math::abs);
    EasingSerializer<SingleArgumentEasing> SQUARE = registerSingleArg(ResourceLocation.parse("cutscenes:square"), t -> t * t);
    EasingSerializer<SingleArgumentEasing> CUBE = registerSingleArg(ResourceLocation.parse("cutscenes:cube"), t -> t * t * t);
    EasingSerializer<SingleArgumentEasing> SQRT = registerSingleArg(ResourceLocation.parse("cutscenes:sqrt"), Math::sqrt);
    EasingSerializer<SingleArgumentEasing> SIN = registerSingleArg(ResourceLocation.parse("cutscenes:sin"), Math::sin);
    EasingSerializer<SingleArgumentEasing> COS = registerSingleArg(ResourceLocation.parse("cutscenes:cos"), Math::cos);
    EasingSerializer<SingleArgumentEasing> TAN = registerSingleArg(ResourceLocation.parse("cutscenes:tan"), Math::tan);
    EasingSerializer<SingleArgumentEasing> ASIN = registerSingleArg(ResourceLocation.parse("cutscenes:asin"), Math::asin);
    EasingSerializer<SingleArgumentEasing> ACOS = registerSingleArg(ResourceLocation.parse("cutscenes:acos"), Math::acos);
    EasingSerializer<SingleArgumentEasing> ATAN = registerSingleArg(ResourceLocation.parse("cutscenes:atan"), Math::atan);
    EasingSerializer<SingleArgumentEasing> TO_DEGREES = registerSingleArg(ResourceLocation.parse("cutscenes:to_degrees"), Math::toDegrees);
    EasingSerializer<SingleArgumentEasing> TO_RADIANS = registerSingleArg(ResourceLocation.parse("cutscenes:to_radians"), Math::toRadians);
    EasingSerializer<DoubleArgumentEasing> ADD = registerDoubleArg(ResourceLocation.parse("cutscenes:add"), Double::sum);
    EasingSerializer<DoubleArgumentEasing> SUBTRACT = registerDoubleArg(ResourceLocation.parse("cutscenes:subtract"), (a, b) -> a - b);
    EasingSerializer<DoubleArgumentEasing> MUL = registerDoubleArg(ResourceLocation.parse("cutscenes:mul"), (a, b) -> a * b);
    EasingSerializer<DoubleArgumentEasing> DIV = registerDoubleArg(ResourceLocation.parse("cutscenes:div"), (a, b) -> a / b);
    EasingSerializer<DoubleArgumentEasing> MOD = registerDoubleArg(ResourceLocation.parse("cutscenes:mod"), (a, b) -> a % b);
    EasingSerializer<DoubleArgumentEasing> POW = registerDoubleArg(ResourceLocation.parse("cutscenes:pow"), Math::pow);
    EasingSerializer<DoubleArgumentEasing> MIN = registerDoubleArg(ResourceLocation.parse("cutscenes:min"), Math::min);
    EasingSerializer<DoubleArgumentEasing> MAX = registerDoubleArg(ResourceLocation.parse("cutscenes:max"), Math::max);
    EasingSerializer<DoubleArgumentEasing> ATAN2 = registerDoubleArg(ResourceLocation.parse("cutscenes:atan2"), Math::atan2);
    EasingSerializer<ClampEasing> CLAMP = register(ResourceLocation.parse("cutscenes:clamp"), ClampEasingSerializer.INSTANCE);
    EasingSerializer<SplineEasing> SPLINE = register(ResourceLocation.parse("cutscenes:spline"), SplineEasingSerializer.INSTANCE);
    EasingSerializer<LerpEasing> LERP = register(ResourceLocation.parse("cutscenes:lerp"), LerpEasingSerializer.INSTANCE);
    EasingSerializer<ColorEasing> COLOR = register(ResourceLocation.parse("cutscenes:color"), ColorEasingSerializer.INSTANCE);


    T fromNetwork(FriendlyByteBuf buf);
    T fromJSON(JsonObject json);
    T fromJSON(JsonObject json, LoadResolver<Easing> context);

    static <T extends Easing> EasingSerializer<T> register(ResourceLocation id, EasingSerializer<T> serializer) {
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static SimpleEasingSerializer registerSimple(ResourceLocation id, SimpleEasing easing) {
        SimpleEasingSerializer serializer = new SimpleEasingSerializer(easing);
        LEGACY_COMPAT.put(id.getPath(), serializer.easing());
        SIMPLE_EASINGS.put(id.getPath(), serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static SingleArgumentEasingSerializer registerSingleArg(ResourceLocation id, DoubleUnaryOperator operator) {
        SingleArgumentEasingSerializer serializer = new SingleArgumentEasingSerializer(operator);
        SINGLE_ARGUMENT_EASINGS.put(operator, serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static DoubleArgumentEasingSerializer registerDoubleArg(ResourceLocation id, DoubleBinaryOperator operator) {
        DoubleArgumentEasingSerializer serializer = new DoubleArgumentEasingSerializer(operator);
        DOUBLE_ARGUMENT_EASINGS.put(operator, serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    /** We need this for the class to load */
    static void init() {}
}