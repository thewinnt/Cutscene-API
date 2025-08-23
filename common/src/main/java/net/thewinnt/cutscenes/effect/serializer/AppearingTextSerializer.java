package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.types.UndertaleDelayProvider;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.LoadingContext;

public class AppearingTextSerializer implements CutsceneEffectSerializer<AppearingTextConfiguration> {
    public static final AppearingTextSerializer INSTANCE = new AppearingTextSerializer();
    public static final FloatProvider BACKUP_FLOAT = ConstantFloat.of(1);

    private AppearingTextSerializer() {}

    @Override
    public AppearingTextConfiguration fromNetwork(FriendlyByteBuf buf) {
        Component text = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf);
        CoordinateProvider rx = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider ry = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider lineWidth = CoordinateProvider.fromNetwork(buf);
        boolean dropShadow = buf.readBoolean();
        ResourceLocation soundbite = buf.readResourceLocation();
        DelayProvider delayProvider = DelayProvider.fromNetwork(buf);
        FloatProvider pitch = buf.readWithCodecTrusted(NbtOps.INSTANCE, FloatProvider.CODEC);
        Easing scale = Easing.fromNetwork(buf);
        Easing rotation = Easing.fromNetwork(buf);
        return new AppearingTextConfiguration(text, rx, ry, lineWidth, dropShadow, soundbite, delayProvider, pitch, scale, rotation);
    }

    @Override
    public AppearingTextConfiguration fromJSON(JsonObject json, LoadingContext context) {
        Component text = context.wrapLoading("text", () -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json.get("text")).getOrThrow());
        CoordinateProvider rx = CoordinateProvider.loadWrapped(json, "x", context);
        CoordinateProvider ry = CoordinateProvider.loadWrapped(json, "y", context);
        CoordinateProvider lineWidth = CoordinateProvider.loadWrapped(json, "line_width", context, ConstantEasing.ONE);
        boolean dropShadow = GsonHelper.getAsBoolean(json, "drop_shadow", true);
        ResourceLocation soundbite = tryGetSoundEffect(json.get("soundbite"));
        DelayProvider delayProvider = context.wrapLoading("delays", () -> DelayProvider.fromJSON(json.get("delays"), UndertaleDelayProvider.INSTANCE));
        DataResult<FloatProvider> pitchResult = FloatProvider.CODEC.parse(JsonOps.INSTANCE, json.get("pitch"));
        FloatProvider pitch;
        if (json.has("pitch") && !json.get("pitch").isJsonNull()) {
            if (pitchResult.isSuccess()) {
                pitch = pitchResult.resultOrPartial().orElseThrow();
            } else {
                context.reportError("Invalid float provider: " + pitchResult.error().orElseThrow());
                pitch = BACKUP_FLOAT;
            }
        } else {
            pitch = BACKUP_FLOAT;
        }
        Easing scale = Easing.loadWrapped(json, "scale", context, ConstantEasing.ONE);
        Easing rotation = Easing.loadWrapped(json, "rotation", context, ConstantEasing.ZERO);
        return new AppearingTextConfiguration(text, rx, ry, lineWidth, dropShadow, soundbite, delayProvider, pitch, scale, rotation);
    }

    @Override
    public void toNetwork(AppearingTextConfiguration config, FriendlyByteBuf buf) {
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, config.text());
        config.rx().toNetwork(buf);
        config.ry().toNetwork(buf);
        config.width().toNetwork(buf);
        buf.writeBoolean(config.dropShadow());
        buf.writeResourceLocation(config.soundbite());
        DelayProvider.toNetwork(config.delays(), buf);
        buf.writeWithCodec(NbtOps.INSTANCE, FloatProvider.CODEC, config.pitch());
        Easing.toNetwork(config.scale(), buf);
        Easing.toNetwork(config.rotation(), buf);
    }

    @Override
    public CutsceneEffectFactory<AppearingTextConfiguration> factory() {
        return AppearingTextEffect::new;
    }

    private static ResourceLocation tryGetSoundEffect(JsonElement json) {
        if (json == null) return ResourceLocation.parse("minecraft:empty");
        if (json.isJsonPrimitive()) return ResourceLocation.parse(json.getAsString());
        return ResourceLocation.parse(GsonHelper.getAsString(json.getAsJsonObject(), "sound_id", "minecraft:empty"));
    }
}
