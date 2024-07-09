package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.serializers.ColorEasingSerializer;

public record ColorEasing(Easing delta, Easing from, Easing to) implements Easing {
    @Override
    public double get(double t) {
        return Math.sqrt(Mth.lerp(delta.get(t), Mth.square(from.get(t)), Mth.square(to.get(t))));
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return ColorEasingSerializer.INSTANCE;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(delta, buf);
        Easing.toNetwork(from, buf);
        Easing.toNetwork(to, buf);
    }
}
