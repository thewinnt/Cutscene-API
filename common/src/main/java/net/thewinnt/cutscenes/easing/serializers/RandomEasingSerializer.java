package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.RandomEasing;
import net.thewinnt.cutscenes.util.LoadResolver;

public class RandomEasingSerializer implements EasingSerializer<RandomEasing> {
    public static final RandomEasingSerializer INSTANCE = new RandomEasingSerializer();

    private RandomEasingSerializer() {}

    @Override
    public RandomEasing fromNetwork(FriendlyByteBuf buf) {
        return new RandomEasing(buf.readVarLong());
    }

    @Override
    public RandomEasing fromJSON(JsonObject json) {
        if (json.has("seed")) {
            return new RandomEasing(GsonHelper.getAsLong(json, "seed"));
        }
        return new RandomEasing();
    }

    @Override
    public RandomEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        if (json.has("seed")) {
            return new RandomEasing(GsonHelper.getAsLong(json, "seed"));
        }
        return new RandomEasing();
    }
    
}
