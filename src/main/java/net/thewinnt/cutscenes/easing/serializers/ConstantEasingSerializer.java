package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;

public class ConstantEasingSerializer implements EasingSerializer<ConstantEasing> {
    public static final ConstantEasingSerializer INSTANCE = new ConstantEasingSerializer();

    private ConstantEasingSerializer() {}

    @Override
    public ConstantEasing fromNetwork(FriendlyByteBuf buf) {
        return new ConstantEasing(buf.readDouble());
    }

    @Override
    public ConstantEasing fromJSON(JsonObject json) {
        return new ConstantEasing(json.get("value").getAsDouble());
    }
}
