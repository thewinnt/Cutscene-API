package net.thewinnt.cutscenes.util;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

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
        CoordinateProvider x = CoordinateProvider.loadWrapped(json, "x", context);
        CoordinateProvider y = CoordinateProvider.loadWrapped(json, "y", context);
        DynamicColor color = DynamicColor.loadWrapped(json, "color", context);
        return new DynamicVertex(x, y, color);
    }
}
