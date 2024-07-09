package net.thewinnt.cutscenes.effect.chardelays.types;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;

public class InstantDelayProvider implements DelayProvider {
    public static final InstantDelayProvider INSTANCE = new InstantDelayProvider();

    private InstantDelayProvider() {}

    @Override
    public int activationCodepoint() {
        return 0;
    }

    @Override
    public double delay(int codepoint) {
        return 0;
    }

    @Override
    public double defaultDelay(int codepoint) {
        return 0;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}

    @Override
    public DelayProviderSerializer<?> getSerializer() {
        return DelayProviderSerializer.INSTANT;
    }
}
