package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ChainEasing;
import net.thewinnt.cutscenes.util.LoadResolver;

public class ChainEasingSerializer implements EasingSerializer<ChainEasing> {
    public static final ChainEasingSerializer INSTANCE = new ChainEasingSerializer();

    private ChainEasingSerializer() {}

    @Override
    public ChainEasing fromNetwork(FriendlyByteBuf buf) {
        Easing argumentProvider = Easing.fromNetwork(buf);
        Easing easing = Easing.fromNetwork(buf);
        return new ChainEasing(argumentProvider, easing);
    }

    @Override
    public ChainEasing fromJSON(JsonObject json) {
        Easing argumentProvider = Easing.fromJSON(json.get("argument"));
        Easing easing = Easing.fromJSON(json.get("easing"));
        return new ChainEasing(argumentProvider, easing);
    }

    @Override
    public ChainEasing fromJSON(JsonObject json, LoadResolver<Easing> context) {
        Easing argumentProvider = Easing.fromJSON(json.get("argument"), context);
        Easing easing = Easing.fromJSON(json.get("easing"), context);
        return new ChainEasing(argumentProvider, easing);
    }
}
