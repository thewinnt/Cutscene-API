package net.thewinnt.cutscenes.rotation.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.rotation.RotationSerializer;
import net.thewinnt.cutscenes.rotation.handler.EaseBackRotation;

public class EaseBackSerializer implements RotationSerializer<EaseBackRotation> {
    public static final EaseBackSerializer INSTANCE = new EaseBackSerializer();

    private EaseBackSerializer() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, EaseBackRotation handler) {
        buf.writeDouble(handler.decay);
    }

    @Override
    public EaseBackRotation fromNetwork(FriendlyByteBuf buf) {
        return new EaseBackRotation(buf.readDouble());
    }

    @Override
    public EaseBackRotation fromJson(JsonObject json) {
        return new EaseBackRotation(GsonHelper.getAsDouble(json, "decay"));
    }
}
