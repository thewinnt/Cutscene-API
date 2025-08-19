package net.thewinnt.cutscenes.effect.serializer;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.SimpleTextEffect;
import net.thewinnt.cutscenes.effect.configuration.SimpleTextConfiguration;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;
import net.thewinnt.cutscenes.util.LoadingContext;

public class SimpleTextSerializer implements CutsceneEffectSerializer<SimpleTextConfiguration> {
    public static final SimpleTextSerializer INSTANCE = new SimpleTextSerializer();
    
    private SimpleTextSerializer() {}

    @Override
    public SimpleTextConfiguration fromNetwork(FriendlyByteBuf buf) {
        Component text = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf);
        CoordinateProvider rx = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider ry = CoordinateProvider.fromNetwork(buf);
        boolean centered = buf.readBoolean();
        Easing scale = Easing.fromNetwork(buf);
        Easing rotation = Easing.fromNetwork(buf);
        Optional<DynamicColor> colorOverride = buf.readOptional(DynamicColor::fromNetwork);
        return new SimpleTextConfiguration(text, rx, ry, centered, scale, rotation, colorOverride);
    }

    @Override
    public SimpleTextConfiguration fromJSON(JsonObject json, LoadingContext context) {
        Component text = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json.get("text")).getOrThrow().getFirst();
        CoordinateProvider rx = CoordinateProvider.fromJSON(json.get("x"), context);
        CoordinateProvider ry = CoordinateProvider.fromJSON(json.get("y"), context);
        boolean centered = GsonHelper.getAsBoolean(json, "centered", false);
        Easing scale = Easing.fromJSON(json.get("scale"), context, ConstantEasing.ONE);
        Easing rotation = Easing.fromJSON(json.get("rotation"), context, ConstantEasing.ZERO);
        Optional<DynamicColor> colorOverride = Optional.ofNullable(DynamicColor.fromJSON(json.get("color_override"), context, null));
        return new SimpleTextConfiguration(text, rx, ry, centered, scale, rotation, colorOverride);
    }

    @Override
    public void toNetwork(SimpleTextConfiguration config, FriendlyByteBuf buf) {
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, config.text());
        config.rx().toNetwork(buf);
        config.ry().toNetwork(buf);
        buf.writeBoolean(config.centered());
        Easing.toNetwork(config.scale(), buf);
        Easing.toNetwork(config.rotation(), buf);
        buf.writeOptional(config.colorOverride(), (buf1, t) -> t.toNetwork(buf1));
    }

    @Override
    public CutsceneEffectFactory<SimpleTextConfiguration> factory() {
        return SimpleTextEffect::new;
    }
}
