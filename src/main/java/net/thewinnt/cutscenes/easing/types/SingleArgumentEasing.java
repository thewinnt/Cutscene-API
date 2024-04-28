package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

import java.util.function.DoubleUnaryOperator;

public class SingleArgumentEasing implements Easing {
    protected final Easing argument;
    protected final DoubleUnaryOperator operation;

    public SingleArgumentEasing(Easing argument, DoubleUnaryOperator operation) {
        this.argument = argument;
        this.operation = operation;
    }

    @Override
    public double get(double t) {
        return operation.applyAsDouble(argument.get(t));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(argument, buf);
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.SINGLE_ARGUMENT_EASINGS.get(operation);
    }
}
