package net.thewinnt.cutscenes.rotation.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.rotation.RotationHandler;
import net.thewinnt.cutscenes.rotation.RotationSerializer;

public class SimpleRotationHandlerSerializer<T extends RotationHandler> implements RotationSerializer<T> {
    private final T handler;

    public SimpleRotationHandlerSerializer(T handler) {
        this.handler = handler;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T handler) {}

    @Override
    public T fromNetwork(FriendlyByteBuf buf) {
        return handler;
    }

    @Override
    public T fromJson(JsonObject json) {
        return handler;
    }
}
