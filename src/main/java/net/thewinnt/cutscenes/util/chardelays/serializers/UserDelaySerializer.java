package net.thewinnt.cutscenes.util.chardelays.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.util.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.util.chardelays.types.UserDefinedDelays;

import java.util.Map;
import java.util.Map.Entry;

public class UserDelaySerializer implements DelayProviderSerializer<UserDefinedDelays> {
    public static final UserDelaySerializer INSTANCE = new UserDelaySerializer();

    private UserDelaySerializer() {}

    @Override
    public UserDefinedDelays fromNetwork(FriendlyByteBuf buf) {
        char activation = buf.readChar();
        Map<Integer, Double> delays = buf.readMap(FriendlyByteBuf::readInt, FriendlyByteBuf::readDouble);
        double defaultDelay = buf.readDouble();
        return new UserDefinedDelays(activation, delays, defaultDelay);
    }

    @Override
    public UserDefinedDelays fromJSON(JsonObject json) {
        char activation = GsonHelper.getAsCharacter(json, "activation_character");
        JsonObject map = GsonHelper.getAsJsonObject(json, "delays");
        Map<Integer, Double> delays = new Int2DoubleOpenHashMap();
        for (Entry<String, JsonElement> i : map.entrySet()) {
            if (delays.put(i.getKey().codePointAt(0), GsonHelper.convertToDouble(i.getValue(), "delay")) != null) {
                throw new IllegalArgumentException("Duplicate key for " + i.getKey().charAt(0));
            }
        }
        double defaultDelay = GsonHelper.getAsDouble(json, "default", 1);
        return new UserDefinedDelays(activation, delays, defaultDelay);
    }
}
