package net.thewinnt.cutscenes.easing.types;

import java.util.Random;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

public class RandomEasing implements Easing {
    private final Random random;
    public final long seed;

    public RandomEasing(long seed) {
        this.random = new Random(seed);
        this.seed = seed;
    }

    public RandomEasing() {
        this.seed = CutsceneAPI.RANDOM.nextLong();
        this.random = new Random(seed);
    }

    @Override
    public double get(double t) {
        return random.nextDouble();
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.RANDOM;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarLong(seed);
    }

    
}
