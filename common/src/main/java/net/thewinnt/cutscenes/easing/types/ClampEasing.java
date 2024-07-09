package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

public class ClampEasing implements Easing {
    public final Easing input;
    public final Easing min;
    public final Easing max;

    public ClampEasing(Easing input, Easing min, Easing max) {
        this.input = input;
        this.min = min;
        this.max = max;
    }

    @Override
    public double get(double t) {
        return Mth.clamp(input.get(t), min.get(t), max.get(t));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(input, buf);
        Easing.toNetwork(min, buf);
        Easing.toNetwork(max, buf);
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.CLAMP;
    }
}
