package net.thewinnt.cutscenes.effect.chardelays.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;

public class SingletonDelaySerializer<T extends DelayProvider> implements DelayProviderSerializer<T> {
    public final T instance;

    public SingletonDelaySerializer(T instance) {
        this.instance = instance;
    }

    @Override
    public T fromNetwork(FriendlyByteBuf buf) {
        return instance;
    }

    @Override
    public T fromJSON(JsonObject json) {
        return instance;
    }
}
