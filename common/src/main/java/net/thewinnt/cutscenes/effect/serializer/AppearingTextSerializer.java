package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import io.netty.handler.codec.EncoderException;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.types.UndertaleDelayProvider;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect;
import net.thewinnt.cutscenes.util.CoordinateProvider;

public class AppearingTextSerializer implements CutsceneEffectSerializer<AppearingTextConfiguration> {
    public static final AppearingTextSerializer INSTANCE = new AppearingTextSerializer();
    public static final FloatProvider BACKUP_FLOAT = ConstantFloat.of(1);

    private AppearingTextSerializer() {}

    @Override
    public AppearingTextConfiguration fromNetwork(FriendlyByteBuf buf) {
        Component text = buf.readComponent();
        CoordinateProvider rx = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider ry = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider lineWidth = CoordinateProvider.fromNetwork(buf);
        boolean dropShadow = buf.readBoolean();
        ResourceLocation soundbite = buf.readResourceLocation();
        DelayProvider delayProvider = DelayProvider.fromNetwork(buf);
        FloatProvider pitch = switch (buf.readByte()) {
            case 0 -> buf.readWithCodec(NbtOps.INSTANCE, FloatProvider.CODEC);
            case 1 -> ConstantFloat.of(buf.readFloat());
            default -> {
                CutsceneAPI.LOGGER.error("Decoding error for float provider, check server logs");
                yield BACKUP_FLOAT;
            }
        };
        return new AppearingTextConfiguration(text, rx, ry, lineWidth, dropShadow, soundbite, delayProvider, pitch);
    }

    @Override
    public AppearingTextConfiguration fromJSON(JsonObject json) {
        Component text = Component.Serializer.fromJson(json.get("text"));
        CoordinateProvider rx = CoordinateProvider.fromJSON(json.get("x"));
        CoordinateProvider ry = CoordinateProvider.fromJSON(json.get("y"));
        CoordinateProvider lineWidth = CoordinateProvider.fromJSON(json.get("line_width"), ConstantEasing.ONE);
        boolean dropShadow = GsonHelper.getAsBoolean(json, "drop_shadow", true);
        ResourceLocation soundbite = tryGetSoundEffect(json.get("soundbite"));
        DelayProvider delayProvider = DelayProvider.fromJSON(json.get("delays"), UndertaleDelayProvider.INSTANCE);
        FloatProvider pitch = FloatProvider.CODEC.parse(JsonOps.INSTANCE, json.get("pitch")).result().orElse(BACKUP_FLOAT);
//        if (pitch == BACKUP_FLOAT) CutsceneAPI.LOGGER.warn("Error loading float provider, using fallback");
        return new AppearingTextConfiguration(text, rx, ry, lineWidth, dropShadow, soundbite, delayProvider, pitch);
    }

    @Override
    public void toNetwork(AppearingTextConfiguration config, FriendlyByteBuf buf) {
        buf.writeComponent(config.text());
        config.rx().toNetwork(buf);
        config.ry().toNetwork(buf);
        config.width().toNetwork(buf);
        buf.writeBoolean(config.dropShadow());
        buf.writeResourceLocation(config.soundbite());
        DelayProvider.toNetwork(config.delays(), buf);
        Tag tag = null;
        try { // ensure valid data client-side
            tag = Util.getOrThrow(FloatProvider.CODEC.encodeStart(NbtOps.INSTANCE, config.pitch()), string -> new EncoderException("Failed to encode: " + string + " " + config.pitch()));
        } catch (Exception e) {
            CutsceneAPI.LOGGER.error("Error encoding FloatProvider", e);
        }
        if (tag instanceof FloatTag number) {
            buf.writeByte(1);
            buf.writeFloat(number.getAsFloat());
        } else if (tag instanceof CompoundTag nbt) {
            buf.writeByte(0);
            buf.writeNbt(nbt);
        } else {
            CutsceneAPI.LOGGER.error("Unexpected tag type when enconding FloatProvider for network: {}", tag);
            buf.writeByte(-1);
        }
    }

    @Override
    public CutsceneEffectFactory<AppearingTextConfiguration> factory() {
        return AppearingTextEffect::new;
    }

    private static ResourceLocation tryGetSoundEffect(JsonElement json) {
        if (json == null) return new ResourceLocation("minecraft:empty");
        if (json.isJsonPrimitive()) return new ResourceLocation(json.getAsString());
        return new ResourceLocation(GsonHelper.getAsString(json.getAsJsonObject(), "sound_id", "minecraft:empty"));
    }
}
