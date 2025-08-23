package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

public record RangeChoiceEasing(Easing inRange, Easing outRange, double rangeMin, double rangeMax) implements Easing {
    public RangeChoiceEasing {
        if (rangeMax < rangeMin) {
            double buf = rangeMax;
            rangeMax = rangeMin;
            rangeMin = buf;
        }
    }

    @Override
    public double get(double v) {
        if (v >= rangeMin && v <= rangeMax) {
            return inRange.get(v);
        } else {
            return outRange.get(v);
        }
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.RANGE_CHOICE;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(inRange, buf);
        Easing.toNetwork(outRange, buf);
        buf.writeDouble(rangeMin);
        buf.writeDouble(rangeMax);
    }
}
