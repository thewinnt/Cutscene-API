package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.FloatProvider;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.types.UndertaleDelayProvider;

public class AppearingTextSerializer implements CutsceneEffectSerializer<AppearingTextConfiguration> {
    public static final AppearingTextSerializer INSTANCE = new AppearingTextSerializer();

    private AppearingTextSerializer() {}

    @Override
    public AppearingTextConfiguration fromNetwork(FriendlyByteBuf buf) {
        Component text = buf.readComponent();
        CoordinateProvider rx = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider ry = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider lineWidth = CoordinateProvider.fromNetwork(buf);
        boolean dropShadow = buf.readBoolean();
        SoundEvent soundbite = SoundEvent.readFromNetwork(buf);
        DelayProvider delayProvider = DelayProvider.fromNetwork(buf);
        FloatProvider pitch = buf.readWithCodecTrusted(NbtOps.INSTANCE, FloatProvider.CODEC);
        return new AppearingTextConfiguration(text, rx, ry, lineWidth, dropShadow, soundbite, delayProvider, pitch);
    }

    @Override
    public AppearingTextConfiguration fromJSON(JsonObject json) {
        Component text = Component.Serializer.fromJson(json.get("text"));
        CoordinateProvider rx = CoordinateProvider.fromJSON(json.get("x"));
        CoordinateProvider ry = CoordinateProvider.fromJSON(json.get("y"));
        CoordinateProvider lineWidth = CoordinateProvider.fromJSON(json.get("line_width"), ConstantEasing.ONE);
        boolean dropShadow = GsonHelper.getAsBoolean(json, "drop_shadow", true);
        SoundEvent soundbite = SoundEvent.CODEC.parse(JsonOps.INSTANCE, json.get("soundbite")).result().orElse(Holder.direct(SoundEvents.EMPTY)).value();
        DelayProvider delayProvider = DelayProvider.fromJSON(json.get("delays"), UndertaleDelayProvider.INSTANCE);
        FloatProvider pitch = FloatProvider.CODEC.parse(JsonOps.INSTANCE, json.get("pitch")).getOrThrow(false, CutsceneAPI.LOGGER::error);
        return new AppearingTextConfiguration(text, rx, ry, lineWidth, dropShadow, soundbite, delayProvider, pitch);
    }

    @Override
    public void toNetwork(AppearingTextConfiguration config, FriendlyByteBuf buf) {
        buf.writeComponent(config.text());
        config.rx().toNetwork(buf);
        config.ry().toNetwork(buf);
        config.width().toNetwork(buf);
        buf.writeBoolean(config.dropShadow());
        config.soundbite().writeToNetwork(buf);
        DelayProvider.toNetwork(config.delays(), buf);
        buf.writeWithCodec(NbtOps.INSTANCE, FloatProvider.CODEC, config.pitch());
    }

    @Override
    public CutsceneEffectFactory<AppearingTextConfiguration> factory() {
        return AppearingTextEffect::new;
    }
}
