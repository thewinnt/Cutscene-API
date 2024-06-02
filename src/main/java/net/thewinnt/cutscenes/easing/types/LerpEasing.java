package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.serializers.LerpEasingSerializer;

public record LerpEasing(Easing delta, Easing from, Easing to) implements Easing {
    @Override
    public double get(double t) {
        return Mth.lerp(delta.get(t), from.get(t), to.get(t));
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return LerpEasingSerializer.INSTANCE;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(delta, buf);
        Easing.toNetwork(from, buf);
        Easing.toNetwork(to, buf);
    }
}
