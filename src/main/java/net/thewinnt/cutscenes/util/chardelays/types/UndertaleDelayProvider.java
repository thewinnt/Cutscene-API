package net.thewinnt.cutscenes.util.chardelays.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.util.chardelays.DelayProvider;
import net.thewinnt.cutscenes.util.chardelays.DelayProviderSerializer;

public class UndertaleDelayProvider implements DelayProvider {
    public static final UndertaleDelayProvider INSTANCE = new UndertaleDelayProvider();

    private UndertaleDelayProvider() {}

    @Override
    public char activationSymbol() {
        return '^';
    }

    @Override
    public double delay(int input) {
        return switch (input) {
            case '0' -> 0;
            case '1' -> 6.666;
            case '2' -> 13.333;
            case '3' -> 20;
            case '4' -> 26.666;
            case '5' -> 33.333;
            case '6' -> 40;
            case '7' -> 46.666;
            case '8' -> 53.333;
            case '9' -> 60;
            default -> 1;
        };
    }

    @Override
    public double defaultDelay(int codepoint) {
        return 1;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}

    @Override
    public DelayProviderSerializer<?> getSerializer() {
        return DelayProviderSerializer.UNDERTALE;
    }
}
