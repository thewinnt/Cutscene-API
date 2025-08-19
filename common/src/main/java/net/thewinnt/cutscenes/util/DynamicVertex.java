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

    public static DynamicVertex fromJSON(JsonObject json, LoadingContext context) {
        CoordinateProvider x = CoordinateProvider.fromJSON(json.get("x"), context);
        CoordinateProvider y = CoordinateProvider.fromJSON(json.get("y"), context);
        DynamicColor color = DynamicColor.fromJSON(json.get("color"), context);
        return new DynamicVertex(x, y, color);
    }
}
