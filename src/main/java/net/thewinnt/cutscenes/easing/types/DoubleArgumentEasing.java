package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class DoubleArgumentEasing implements Easing {
    protected final Easing arg1;
    protected final Easing arg2;
    protected final DoubleBinaryOperator operation;

    public DoubleArgumentEasing(Easing arg1, Easing arg2, DoubleBinaryOperator operation) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.operation = operation;
    }

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
