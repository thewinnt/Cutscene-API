package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

public class ConstantEasingSerializer implements EasingSerializer<ConstantEasing> {
    public static final ConstantEasingSerializer INSTANCE = new ConstantEasingSerializer();
    public static final MapCodec<ConstantEasing> CODEC = Codec.DOUBLE.xmap(ConstantEasing::new, ConstantEasing::value).fieldOf("value");

    private ConstantEasingSerializer() {}

    @Override
    public ConstantEasing fromNetwork(FriendlyByteBuf buf) {
        return new ConstantEasing(buf.readDouble());
    }

    @Override
    public ConstantEasing fromJSON(JsonObject json, LoadingContext context) {
        return new ConstantEasing(json.get("value").getAsDouble());
    }

    @Override
    public MapCodec<ConstantEasing> codec() {
        return CODEC;
    }
}
