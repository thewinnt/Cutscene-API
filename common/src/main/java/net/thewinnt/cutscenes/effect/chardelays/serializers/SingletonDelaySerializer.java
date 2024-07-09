package net.thewinnt.cutscenes.effect.chardelays.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;

public record SingletonDelaySerializer<T extends DelayProvider>(T instance) implements DelayProviderSerializer<T> {

    @Override
    public T fromNetwork(FriendlyByteBuf buf) {
        return instance;
    }

    @Override
    public T fromJSON(JsonObject json) {
        return instance;
    }
}
