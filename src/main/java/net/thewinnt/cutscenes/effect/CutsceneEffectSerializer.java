package net.thewinnt.cutscenes.effect;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.configuration.RectangleConfiguration;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.effect.serializer.AppearingTextSerializer;
import net.thewinnt.cutscenes.effect.serializer.RectangleSerializer;
import net.thewinnt.cutscenes.effect.serializer.TriangleStripSerializer;

public interface CutsceneEffectSerializer<T> {
    CutsceneEffectSerializer<AppearingTextConfiguration> APPEARING_TEXT = register(new ResourceLocation("cutscenes:appearing_text"), AppearingTextSerializer.INSTANCE);
    CutsceneEffectSerializer<TriangleStripConfiguration> TRIANGLE_STRIP = register(new ResourceLocation("cutscenes:triangle_strip"), TriangleStripSerializer.INSTANCE);
    CutsceneEffectSerializer<RectangleConfiguration> RECTANGLE = register(new ResourceLocation("cutscenes:rectangle"), RectangleSerializer.INSTANCE);

    T fromNetwork(FriendlyByteBuf buf);
    T fromJSON(JsonObject json);
    void toNetwork(T object, FriendlyByteBuf buf);
    CutsceneEffectFactory<T> factory();

    static <T> CutsceneEffectSerializer<T> register(ResourceLocation id, CutsceneEffectSerializer<T> serializer) {
        return Registry.register(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS, id, serializer);
    }

    @FunctionalInterface
    public interface CutsceneEffectFactory<T> {
        CutsceneEffect<T> create(double startTime, double endTime, T config);

        @SuppressWarnings("unchecked")
        default CutsceneEffect<T> unchecked(double startTime, double endTime, Object config) {
            return create(startTime, endTime, (T)config);
        }
    }

    /** Loads the class, causing its static initializer to be run. Just like {@link EasingSerializer#init()} */
    static void init() {}
}
