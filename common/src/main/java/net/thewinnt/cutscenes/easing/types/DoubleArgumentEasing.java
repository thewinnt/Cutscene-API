package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

import java.util.function.DoubleBinaryOperator;

public record DoubleArgumentEasing(Easing arg1, Easing arg2,
                                   DoubleBinaryOperator operation) implements Easing {

    @Override
    public double get(double t) {
        return operation.applyAsDouble(arg1.get(t), arg2.get(t));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(arg1, buf);
        Easing.toNetwork(arg2, buf);
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.DOUBLE_ARGUMENT_EASINGS.get(operation);
    }
}
