package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

public class ChainEasing implements Easing {
    public final Easing argumentProvider;
    public final Easing easing;

    public ChainEasing(Easing argumentProvider, Easing easing) {
        this.argumentProvider = argumentProvider;
        this.easing = easing;
    }

    @Override
    public double get(double t) {
        return easing.get(argumentProvider.get(t));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(argumentProvider, buf);
        Easing.toNetwork(easing, buf);
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.CHAIN;
    }
}
