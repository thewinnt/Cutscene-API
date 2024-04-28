package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

public record ConstantEasing(double value) implements Easing {
    public static final ConstantEasing PI = new ConstantEasing(Math.PI);
    public static final ConstantEasing E = new ConstantEasing(Math.E);
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
