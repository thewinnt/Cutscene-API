package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.BlitConfiguration;
import net.thewinnt.cutscenes.effect.type.BlitEffect;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;

public class BlitSerializer implements CutsceneEffectSerializer<BlitConfiguration> {
    public static final BlitSerializer INSTANCE = new BlitSerializer();

    private BlitSerializer() {}

    @Override
    public BlitConfiguration fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation texture = buf.readResourceLocation();
        CoordinateProvider x1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider y1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider x2 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider y2 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider u1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider v1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider u2 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider v2 = CoordinateProvider.fromNetwork(buf);
        DynamicColor tint = DynamicColor.fromNetwork(buf);
        return new BlitConfiguration(texture, x1, y1, x2, y2, u1, v1, u2, v2, tint);
    }

    @Override
    public BlitConfiguration fromJSON(JsonObject json) {
        ResourceLocation texture = new ResourceLocation(GsonHelper.getAsString(json, "texture"));
        CoordinateProvider x1 = CoordinateProvider.fromJSON(json.get("x1"), ConstantEasing.ZERO);
        CoordinateProvider y1 = CoordinateProvider.fromJSON(json.get("y1"), ConstantEasing.ZERO);
        CoordinateProvider x2 = CoordinateProvider.fromJSON(json.get("x2"), ConstantEasing.ONE);
        CoordinateProvider y2 = CoordinateProvider.fromJSON(json.get("y2"), ConstantEasing.ONE);
        CoordinateProvider u1 = CoordinateProvider.fromJSON(json.get("u1"), ConstantEasing.ZERO);
        CoordinateProvider v1 = CoordinateProvider.fromJSON(json.get("v1"), ConstantEasing.ZERO);
        CoordinateProvider u2 = CoordinateProvider.fromJSON(json.get("u2"), ConstantEasing.ONE);
        CoordinateProvider v2 = CoordinateProvider.fromJSON(json.get("v2"), ConstantEasing.ONE);
        DynamicColor tint = DynamicColor.fromJSON(json.get("tint"), DynamicColor.WHITE);
        return new BlitConfiguration(texture, x1, y1, x2, y2, u1, v1, u2, v2, tint);
    }

    @Override
    public void toNetwork(BlitConfiguration object, FriendlyByteBuf buf) {
        buf.writeResourceLocation(object.texture());
        object.x1().toNetwork(buf);
        object.y1().toNetwork(buf);
        object.x2().toNetwork(buf);
        object.y2().toNetwork(buf);
        object.u1().toNetwork(buf);
        object.v1().toNetwork(buf);
        object.u2().toNetwork(buf);
        object.v2().toNetwork(buf);
        object.tint().toNetwork(buf);
    }

    @Override
    public CutsceneEffectFactory<BlitConfiguration> factory() {
        return BlitEffect::new;
    }
}
