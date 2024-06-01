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
        Map<Integer, Double> delaysSpecial = buf.readMap(FriendlyByteBuf::readInt, FriendlyByteBuf::readDouble);
        double fallbackSpecial = buf.readDouble();
        Map<Integer, Double> delaysNormal = buf.readMap(FriendlyByteBuf::readInt, FriendlyByteBuf::readDouble);
        double fallbackNormal = buf.readDouble();
        return new UserDefinedDelays(activation, delaysSpecial, fallbackSpecial, delaysNormal, fallbackNormal);
    }

    @Override
    public UserDefinedDelays fromJSON(JsonObject json) {
        int activation = GsonHelper.getAsString(json, "activation_character").codePointAt(0);
        JsonObject mapSpecial = GsonHelper.getAsJsonObject(json, "delays_active");
        Map<Integer, Double> delaysSpecial = new Int2DoubleOpenHashMap();
        for (Entry<String, JsonElement> i : mapSpecial.entrySet()) {
            if (delaysSpecial.put(i.getKey().codePointAt(0), GsonHelper.convertToDouble(i.getValue(), "delay")) != null) {
                throw new IllegalArgumentException("Duplicate key for " + i.getKey().charAt(0));
            }
        }
        double fallbackSpecial = GsonHelper.getAsDouble(mapSpecial, "default", 0);
        JsonObject mapNormal = GsonHelper.getAsJsonObject(json, "delays_global", new JsonObject());
        Map<Integer, Double> delaysNormal = new Int2DoubleOpenHashMap();
        for (Entry<String, JsonElement> i : mapNormal.entrySet()) {
            if (delaysNormal.put(i.getKey().codePointAt(0), GsonHelper.convertToDouble(i.getValue(), "delay")) != null) {
                throw new IllegalArgumentException("Duplicate key for " + i.getKey().charAt(0));
            }
        }
        double fallbackNormal = GsonHelper.getAsDouble(mapNormal, "default", 1);
        return new UserDefinedDelays(activation, delaysSpecial, fallbackSpecial, delaysNormal, fallbackNormal);
    }
}
