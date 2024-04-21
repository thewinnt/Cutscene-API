package net.thewinnt.cutscenes.easing.serializers;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

public class SimpleEasingSerializer implements EasingSerializer<SimpleEasing> {
    public final SimpleEasing easing;
    public final Codec<SimpleEasing> codec;

    public SimpleEasingSerializer(SimpleEasing easing) {
        this.easing = easing;
        this.codec = Codec.unit(easing);
    }

    @Override
    public Codec<SimpleEasing> codec() {
        return codec;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, Easing easing) {}

    @Override
    public SimpleEasing fromNetwork(FriendlyByteBuf buf) {
        return easing;
    }
}
