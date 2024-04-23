package net.thewinnt.cutscenes.easing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.serializers.CompoundEasingSerializer;
import net.thewinnt.cutscenes.easing.serializers.ConstantEasingSerializer;
import net.thewinnt.cutscenes.easing.serializers.SimpleEasingSerializer;
import net.thewinnt.cutscenes.easing.types.CompoundEasing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

import java.util.HashMap;
import java.util.Map;

public interface EasingSerializer<T extends Easing> {
    Map<String, Easing> LEGACY_COMPAT = new HashMap<>();
    Map<String, SimpleEasingSerializer> SIMPLE_EASINGS = new HashMap<>();
    EasingSerializer<SimpleEasing> LINEAR = registerSimple(new ResourceLocation("cutscenes:linear"), SimpleEasing.LINEAR);
    EasingSerializer<SimpleEasing> IN_SINE = registerSimple(new ResourceLocation("cutscenes:in_sine"), SimpleEasing.IN_SINE);
    EasingSerializer<SimpleEasing> OUT_SINE = registerSimple(new ResourceLocation("cutscenes:out_sine"), SimpleEasing.OUT_SINE);
    EasingSerializer<SimpleEasing> IN_OUT_SINE = registerSimple(new ResourceLocation("cutscenes:in_out_sine"), SimpleEasing.IN_OUT_SINE);
    EasingSerializer<SimpleEasing> IN_QUAD = registerSimple(new ResourceLocation("cutscenes:in_quad"), SimpleEasing.IN_QUAD);
    EasingSerializer<SimpleEasing> OUT_QUAD = registerSimple(new ResourceLocation("cutscenes:out_quad"), SimpleEasing.OUT_QUAD);
    EasingSerializer<SimpleEasing> IN_OUT_QUAD = registerSimple(new ResourceLocation("cutscenes:in_out_quad"), SimpleEasing.IN_OUT_QUAD);
    EasingSerializer<SimpleEasing> IN_CUBIC = registerSimple(new ResourceLocation("cutscenes:in_cubic"), SimpleEasing.IN_CUBIC);
    EasingSerializer<SimpleEasing> OUT_CUBIC = registerSimple(new ResourceLocation("cutscenes:out_cubic"), SimpleEasing.OUT_CUBIC);
    EasingSerializer<SimpleEasing> IN_OUT_CUBIC = registerSimple(new ResourceLocation("cutscenes:in_out_cubic"), SimpleEasing.IN_OUT_CUBIC);
    EasingSerializer<SimpleEasing> IN_QUART = registerSimple(new ResourceLocation("cutscenes:in_quart"), SimpleEasing.IN_QUART);
    EasingSerializer<SimpleEasing> OUT_QUART = registerSimple(new ResourceLocation("cutscenes:out_quart"), SimpleEasing.OUT_QUART);
    EasingSerializer<SimpleEasing> IN_OUT_QUART = registerSimple(new ResourceLocation("cutscenes:in_out_quart"), SimpleEasing.IN_OUT_QUART);
    EasingSerializer<SimpleEasing> IN_QUINT = registerSimple(new ResourceLocation("cutscenes:in_quint"), SimpleEasing.IN_QUINT);
    EasingSerializer<SimpleEasing> OUT_QUINT = registerSimple(new ResourceLocation("cutscenes:out_quint"), SimpleEasing.OUT_QUINT);
    EasingSerializer<SimpleEasing> IN_OUT_QUINT = registerSimple(new ResourceLocation("cutscenes:in_out_quint"), SimpleEasing.IN_OUT_QUINT);
    EasingSerializer<SimpleEasing> IN_EXPO = registerSimple(new ResourceLocation("cutscenes:in_expo"), SimpleEasing.IN_EXPO);
    EasingSerializer<SimpleEasing> OUT_EXPO = registerSimple(new ResourceLocation("cutscenes:out_expo"), SimpleEasing.OUT_EXPO);
    EasingSerializer<SimpleEasing> IN_OUT_EXPO = registerSimple(new ResourceLocation("cutscenes:in_out_expo"), SimpleEasing.IN_OUT_EXPO);
    EasingSerializer<SimpleEasing> IN_CIRC = registerSimple(new ResourceLocation("cutscenes:in_circ"), SimpleEasing.IN_CIRC);
    EasingSerializer<SimpleEasing> OUT_CIRC = registerSimple(new ResourceLocation("cutscenes:out_circ"), SimpleEasing.OUT_CIRC);
    EasingSerializer<SimpleEasing> IN_OUT_CIRC = registerSimple(new ResourceLocation("cutscenes:in_out_circ"), SimpleEasing.IN_OUT_CIRC);
    EasingSerializer<SimpleEasing> IN_BACK = registerSimple(new ResourceLocation("cutscenes:in_back"), SimpleEasing.IN_BACK);
    EasingSerializer<SimpleEasing> OUT_BACK = registerSimple(new ResourceLocation("cutscenes:out_back"), SimpleEasing.OUT_BACK);
    EasingSerializer<SimpleEasing> IN_OUT_BACK = registerSimple(new ResourceLocation("cutscenes:in_out_back"), SimpleEasing.IN_OUT_BACK);
    EasingSerializer<SimpleEasing> IN_ELASTIC = registerSimple(new ResourceLocation("cutscenes:in_elastic"), SimpleEasing.IN_ELASTIC);
    EasingSerializer<SimpleEasing> OUT_ELASTIC = registerSimple(new ResourceLocation("cutscenes:out_elastic"), SimpleEasing.OUT_ELASTIC);
    EasingSerializer<SimpleEasing> IN_OUT_ELASTIC = registerSimple(new ResourceLocation("cutscenes:in_out_elastic"), SimpleEasing.IN_OUT_ELASTIC);
    EasingSerializer<SimpleEasing> OUT_BOUNCE = registerSimple(new ResourceLocation("cutscenes:out_bounce"), SimpleEasing.OUT_BOUNCE);
    EasingSerializer<SimpleEasing> IN_BOUNCE = registerSimple(new ResourceLocation("cutscenes:in_bounce"), SimpleEasing.IN_BOUNCE);
    EasingSerializer<SimpleEasing> IN_OUT_BOUNCE = registerSimple(new ResourceLocation("cutscenes:in_out_bounce"), SimpleEasing.IN_OUT_BOUNCE);
    EasingSerializer<ConstantEasing> CONSTANT = register(new ResourceLocation("cutscenes:constant"), ConstantEasingSerializer.INSTANCE);
    EasingSerializer<CompoundEasing> COMPOUND = register(new ResourceLocation("cutscenes:compound"), CompoundEasingSerializer.INSTANCE);

    T fromNetwork(FriendlyByteBuf buf);
    T fromJSON(JsonObject json);

    static <T extends Easing> EasingSerializer<T> register(ResourceLocation id, EasingSerializer<T> serializer) {
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    static SimpleEasingSerializer registerSimple(ResourceLocation id, SimpleEasing easing) {
        SimpleEasingSerializer serializer = new SimpleEasingSerializer(easing);
        LEGACY_COMPAT.put(id.getPath(), serializer.easing());
        SIMPLE_EASINGS.put(id.getPath(), serializer);
        return Registry.register(CutsceneAPI.EASING_SERIALIZERS, id, serializer);
    }

    /** We need this for the class to load */
    static void init() {}
}
