package net.thewinnt.cutscenes.util.chardelays.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.util.chardelays.DelayProvider;
import net.thewinnt.cutscenes.util.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.util.chardelays.serializers.UserDelaySerializer;

import java.util.Map;

public record UserDefinedDelays(
    char activation,
    Map<Integer, Double> delaysSpecial,
    double fallbackSpecial,
    Map<Integer, Double> delaysDefault,
    double fallbackNormal
) implements DelayProvider {

    @Override
    public char activationSymbol() {
        return activation;
    }

    @Override
    public double delay(int input) {
        return delaysSpecial.getOrDefault(input, fallbackSpecial);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeChar(activation);
        buf.writeMap(delaysSpecial, FriendlyByteBuf::writeInt, FriendlyByteBuf::writeDouble);
        buf.writeDouble(fallbackSpecial);
        buf.writeMap(delaysDefault, FriendlyByteBuf::writeInt, FriendlyByteBuf::writeDouble);
        buf.writeDouble(fallbackNormal);
    }

    @Override
    public double defaultDelay(int codepoint) {
        return delaysDefault.getOrDefault(codepoint, fallbackNormal);
    }

    @Override
    public DelayProviderSerializer<?> getSerializer() {
        return UserDelaySerializer.INSTANCE;
    }
}
