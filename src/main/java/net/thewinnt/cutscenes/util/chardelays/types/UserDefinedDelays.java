package net.thewinnt.cutscenes.util.chardelays.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.util.chardelays.DelayProvider;
import net.thewinnt.cutscenes.util.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.util.chardelays.serializers.UserDelaySerializer;

import java.util.Map;

public record UserDefinedDelays(char activation, Map<Integer, Double> delays, double defaultDelay) implements DelayProvider {
    @Override
    public char activationSymbol() {
        return activation;
    }

    @Override
    public double delay(int input) {
        return delays.get(input);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeChar(activation);
        buf.writeMap(delays, FriendlyByteBuf::writeInt, FriendlyByteBuf::writeDouble);
        buf.writeDouble(defaultDelay);
    }

    @Override
    public double defaultDelay(int codepoint) {
        return defaultDelay;
    }

    @Override
    public DelayProviderSerializer<?> getSerializer() {
        return UserDelaySerializer.INSTANCE;
    }
}
