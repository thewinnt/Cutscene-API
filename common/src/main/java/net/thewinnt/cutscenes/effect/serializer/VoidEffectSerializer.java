package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.util.LoadingContext;

public record VoidEffectSerializer(CutsceneEffectFactory<Void> factory) implements CutsceneEffectSerializer<Void> {
    @Override
    public Void fromNetwork(FriendlyByteBuf buf) {
        return null;
    }

    @Override
    public Void fromJSON(JsonObject json, LoadingContext context) {
        return null;
    }

    @Override
    public void toNetwork(Void object, FriendlyByteBuf buf) {}
}
