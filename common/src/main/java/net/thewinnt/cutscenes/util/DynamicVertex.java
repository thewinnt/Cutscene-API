package net.thewinnt.cutscenes.util;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

public record DynamicVertex(CoordinateProvider x, CoordinateProvider y, DynamicColor color) {
    public void toNetwork(FriendlyByteBuf buf) {
        x.toNetwork(buf);
        y.toNetwork(buf);
        color.toNetwork(buf);
    }

    public static DynamicVertex fromNetwork(FriendlyByteBuf buf) {
        CoordinateProvider x = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider y = CoordinateProvider.fromNetwork(buf);
        DynamicColor color = DynamicColor.fromNetwork(buf);
        return new DynamicVertex(x, y, color);
    }

    public static DynamicVertex fromJSON(JsonObject json) {
        CoordinateProvider x = CoordinateProvider.fromJSON(json.get("x"));
        CoordinateProvider y = CoordinateProvider.fromJSON(json.get("y"));
        DynamicColor color = DynamicColor.fromJSON(json.get("color"));
        return new DynamicVertex(x, y, color);
    }
}
