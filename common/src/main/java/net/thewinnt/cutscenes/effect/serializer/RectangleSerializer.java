package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.RectangleConfiguration;
import net.thewinnt.cutscenes.effect.type.RectangleEffect;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;
import net.thewinnt.cutscenes.util.LoadingContext;

public class RectangleSerializer implements CutsceneEffectSerializer<RectangleConfiguration> {
    public static final RectangleSerializer INSTANCE = new RectangleSerializer();

    private RectangleSerializer() {}

    @Override
    public RectangleConfiguration fromNetwork(FriendlyByteBuf buf) {
        CoordinateProvider x = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider y = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider width = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider height = CoordinateProvider.fromNetwork(buf);
        DynamicColor colorTop = DynamicColor.fromNetwork(buf);
        DynamicColor colorBottom = DynamicColor.fromNetwork(buf);
        return new RectangleConfiguration(x, y, width, height, colorTop, colorBottom);
    }

    @Override
    public RectangleConfiguration fromJSON(JsonObject json, LoadingContext context) {
        CoordinateProvider x = CoordinateProvider.fromJSON(json.get("x"), context, ConstantEasing.ZERO);
        CoordinateProvider y = CoordinateProvider.fromJSON(json.get("y"), context, ConstantEasing.ZERO);
        CoordinateProvider width = CoordinateProvider.fromJSON(json.get("width"), context, ConstantEasing.ONE);
        CoordinateProvider height = CoordinateProvider.fromJSON(json.get("height"), context, ConstantEasing.ONE);
        if (json.has("color")) {
            DynamicColor color = DynamicColor.fromJSON(json.get("color"), context);
            return new RectangleConfiguration(x, y, width, height, color, color);
        } else {
            DynamicColor color1 = DynamicColor.fromJSON(json.get("color_top"), context, DynamicColor.BLACK);
            DynamicColor color2 = DynamicColor.fromJSON(json.get("color_bottom"), context, DynamicColor.BLACK);
            return new RectangleConfiguration(x, y, width, height, color1, color2);
        }
    }

    @Override
    public void toNetwork(RectangleConfiguration object, FriendlyByteBuf buf) {
        object.x().toNetwork(buf);
        object.y().toNetwork(buf);
        object.width().toNetwork(buf);
        object.height().toNetwork(buf);
        object.colorTop().toNetwork(buf);
        object.colorBottom().toNetwork(buf);
    }

    @Override
    public CutsceneEffectFactory<RectangleConfiguration> factory() {
        return RectangleEffect::new;
    }
}
