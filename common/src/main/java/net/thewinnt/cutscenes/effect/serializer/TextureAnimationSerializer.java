package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.TextureAnimationEffect;
import net.thewinnt.cutscenes.effect.configuration.TextureAnimationConfiguration;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;

public class TextureAnimationSerializer implements CutsceneEffectSerializer<TextureAnimationConfiguration> {
    public static final TextureAnimationSerializer INSTANCE = new TextureAnimationSerializer();
    
    private TextureAnimationSerializer() {}

    @Override
    public TextureAnimationConfiguration fromNetwork(FriendlyByteBuf buf) {
        String texture = buf.readUtf();
        int frameCount = buf.readVarInt();
        int frameOffset = buf.readVarInt();
        Easing timeWarp = Easing.fromNetwork(buf);
        CoordinateProvider x1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider y1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider x2 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider y2 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider u1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider v1 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider u2 = CoordinateProvider.fromNetwork(buf);
        CoordinateProvider v2 = CoordinateProvider.fromNetwork(buf);
        DynamicColor tint = DynamicColor.fromNetwork(buf);
        float zIndex = buf.readFloat();
        return new TextureAnimationConfiguration(texture, frameCount, frameOffset, timeWarp, x1, y1, x2, y2, u1, v1, u2, v2, tint, zIndex);
    }

    @Override
    public TextureAnimationConfiguration fromJSON(JsonObject json) {
        String texture = GsonHelper.getAsString(json, "texture");
        int frameCount = GsonHelper.getAsInt(json, "frames");
        int frameOffset = GsonHelper.getAsInt(json, "frame_offset", 0);
        Easing timeWarp = Easing.fromJSON(json.get("time_warp"), SimpleEasing.LINEAR);
        CoordinateProvider x1 = CoordinateProvider.fromJSON(json.get("x1"), ConstantEasing.ZERO);
        CoordinateProvider y1 = CoordinateProvider.fromJSON(json.get("y1"), ConstantEasing.ZERO);
        CoordinateProvider x2 = CoordinateProvider.fromJSON(json.get("x2"), ConstantEasing.ONE);
        CoordinateProvider y2 = CoordinateProvider.fromJSON(json.get("y2"), ConstantEasing.ONE);
        CoordinateProvider u1 = CoordinateProvider.fromJSON(json.get("u1"), ConstantEasing.ZERO);
        CoordinateProvider v1 = CoordinateProvider.fromJSON(json.get("v1"), ConstantEasing.ZERO);
        CoordinateProvider u2 = CoordinateProvider.fromJSON(json.get("u2"), ConstantEasing.ONE);
        CoordinateProvider v2 = CoordinateProvider.fromJSON(json.get("v2"), ConstantEasing.ONE);
        DynamicColor tint = DynamicColor.fromJSON(json.get("tint"), DynamicColor.WHITE);
        float zIndex = GsonHelper.getAsFloat(json, "z", 0);
        return new TextureAnimationConfiguration(texture, frameCount, frameOffset, timeWarp, x1, y1, x2, y2, u1, v1, u2, v2, tint, zIndex);
    }

    @Override
    public void toNetwork(TextureAnimationConfiguration object, FriendlyByteBuf buf) {
        buf.writeUtf(object.textureMask());
        buf.writeVarInt(object.frameCount());
        buf.writeVarInt(object.frameOffset());
        Easing.toNetwork(object.timeWarp(), buf);
        object.x1().toNetwork(buf);
        object.y1().toNetwork(buf);
        object.x2().toNetwork(buf);
        object.y2().toNetwork(buf);
        object.u1().toNetwork(buf);
        object.v1().toNetwork(buf);
        object.u2().toNetwork(buf);
        object.v2().toNetwork(buf);
        object.tint().toNetwork(buf);
        buf.writeFloat(object.zIndex());
    }

    @Override
    public CutsceneEffectFactory<TextureAnimationConfiguration> factory() {
        return TextureAnimationEffect::new;
    }
}