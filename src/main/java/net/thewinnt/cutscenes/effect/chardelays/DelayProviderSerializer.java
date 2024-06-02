package net.thewinnt.cutscenes.effect.chardelays;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.serializers.SingletonDelaySerializer;
import net.thewinnt.cutscenes.effect.chardelays.serializers.UserDelaySerializer;
import net.thewinnt.cutscenes.effect.chardelays.types.UndertaleDelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.types.UserDefinedDelays;

import java.util.HashMap;
import java.util.Map;

public interface DelayProviderSerializer<T extends DelayProvider> {
    Map<ResourceLocation, DelayProvider> SINGLETONS = new HashMap<>();

    DelayProviderSerializer<UndertaleDelayProvider> UNDERTALE = registerSingle(new ResourceLocation("cutscenes:undertale"), UndertaleDelayProvider.INSTANCE);
    DelayProviderSerializer<UserDefinedDelays> USER_DEFINED = register(new ResourceLocation("cutscenes:custom"), UserDelaySerializer.INSTANCE);

    T fromNetwork(FriendlyByteBuf buf);
    T fromJSON(JsonObject json);

    static <T extends DelayProvider> DelayProviderSerializer<T> register(ResourceLocation id, DelayProviderSerializer<T> serializer) {
        return Registry.register(CutsceneAPI.DELAY_PROVIDERS, id, serializer);
    }

    static <T extends DelayProvider> DelayProviderSerializer<T> registerSingle(ResourceLocation id, T instance) {
        SINGLETONS.put(id, instance);
        return Registry.register(CutsceneAPI.DELAY_PROVIDERS, id, new SingletonDelaySerializer<>(instance));
    }

    /**
     * @see EasingSerializer#init()
     * @see CutsceneEffectSerializer#init()
     */
    static void init() {}
}
