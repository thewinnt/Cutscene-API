package net.thewinnt.cutscenes.effect;

import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.configuration.BlitConfiguration;
import net.thewinnt.cutscenes.effect.configuration.PlaySoundConfiguration;
import net.thewinnt.cutscenes.effect.configuration.RectangleConfiguration;
import net.thewinnt.cutscenes.effect.configuration.SimpleTextConfiguration;
import net.thewinnt.cutscenes.effect.configuration.TextureAnimationConfiguration;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.effect.serializer.AppearingTextSerializer;
import net.thewinnt.cutscenes.effect.serializer.BlitSerializer;
import net.thewinnt.cutscenes.effect.serializer.PlaySoundSerializer;
import net.thewinnt.cutscenes.effect.serializer.RectangleSerializer;
import net.thewinnt.cutscenes.effect.serializer.SimpleTextSerializer;
import net.thewinnt.cutscenes.effect.serializer.TextureAnimationSerializer;
import net.thewinnt.cutscenes.effect.serializer.TriangleStripSerializer;
import net.thewinnt.cutscenes.effect.serializer.VoidEffectSerializer;
import net.thewinnt.cutscenes.util.LoadingContext;

public interface CutsceneEffectSerializer<T> {
    CutsceneEffectSerializer<AppearingTextConfiguration> APPEARING_TEXT = register(ResourceLocation.parse("cutscenes:appearing_text"), AppearingTextSerializer.INSTANCE);
    CutsceneEffectSerializer<TriangleStripConfiguration> TRIANGLE_STRIP = register(ResourceLocation.parse("cutscenes:triangle_strip"), TriangleStripSerializer.INSTANCE);
    CutsceneEffectSerializer<RectangleConfiguration> RECTANGLE = register(ResourceLocation.parse("cutscenes:rectangle"), RectangleSerializer.INSTANCE);
    CutsceneEffectSerializer<BlitConfiguration> BLIT = register(ResourceLocation.parse("cutscenes:blit"), BlitSerializer.INSTANCE);
    CutsceneEffectSerializer<PlaySoundConfiguration> PLAY_SOUND = register(ResourceLocation.parse("cutscenes:play_sound"), PlaySoundSerializer.INSTANCE);
    CutsceneEffectSerializer<Void> HIDE_CHUNKS = register(ResourceLocation.parse("cutscenes:hide_chunks"), new VoidEffectSerializer(HideChunksEffect::new));
    CutsceneEffectSerializer<Void> HIDE_GUI = register(ResourceLocation.parse("cutscenes:hide_gui"), new VoidEffectSerializer(HideGuiEffect::new));
    CutsceneEffectSerializer<SimpleTextConfiguration> TEXT = register(ResourceLocation.parse("cutscenes:text"), SimpleTextSerializer.INSTANCE);
    CutsceneEffectSerializer<TextureAnimationConfiguration> ANIMATION = register(ResourceLocation.parse("cutscenes:animation"), TextureAnimationSerializer.INSTANCE);

    T fromNetwork(FriendlyByteBuf buf);
    T fromJSON(JsonObject json, LoadingContext context);
    void toNetwork(T object, FriendlyByteBuf buf);
    CutsceneEffectFactory<T> factory();

    static <T> CutsceneEffectSerializer<T> register(ResourceLocation id, CutsceneEffectSerializer<T> serializer) {
        return Registry.register(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS, id, serializer);
    }

    @FunctionalInterface
    interface CutsceneEffectFactory<T> {
        CutsceneEffect<T> create(double startTime, double endTime, T config);

        @SuppressWarnings("unchecked")
        default CutsceneEffect<T> unchecked(double startTime, double endTime, Object config) {
            return create(startTime, endTime, (T)config);
        }
    }

    /** Loads the class, causing its static initializer to be run. Just like {@link EasingSerializer#init()} */
    static void init() {}
}
