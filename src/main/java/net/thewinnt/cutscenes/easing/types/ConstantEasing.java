package net.thewinnt.cutscenes.easing.types;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

public record ConstantEasing(double value) implements Easing {
    @Override
    public double get(double t) {
        return value;
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.CONSTANT;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(value);
    }
}
