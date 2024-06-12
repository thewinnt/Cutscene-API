package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.effect.type.TriangleStripEffect;
import net.thewinnt.cutscenes.util.DynamicVertex;

public class TriangleStripSerializer implements CutsceneEffectSerializer<TriangleStripConfiguration> {
    public static final TriangleStripSerializer INSTANCE = new TriangleStripSerializer();

    private TriangleStripSerializer() {}

    @Override
    public TriangleStripConfiguration fromNetwork(FriendlyByteBuf buf) {
        DynamicVertex[] vertices = buf.readArray(DynamicVertex[]::new, DynamicVertex::fromNetwork);
        return new TriangleStripConfiguration(vertices);
    }

    @Override
    public TriangleStripConfiguration fromJSON(JsonObject json) {
        JsonArray array = GsonHelper.getAsJsonArray(json, "vertices");
        DynamicVertex[] vertices = new DynamicVertex[array.size()];
        for (int i = 0; i < array.size(); i++) {
            vertices[i] = DynamicVertex.fromJSON(GsonHelper.convertToJsonObject(array.get(i), "vertex"));
        }
        return new TriangleStripConfiguration(vertices);
    }

    @Override
    public void toNetwork(TriangleStripConfiguration object, FriendlyByteBuf buf) {
        buf.writeArray(object.vertices(), (buf1, vertex) -> vertex.toNetwork(buf1));
    }

    @Override
    public CutsceneEffectFactory<TriangleStripConfiguration> factory() {
        return TriangleStripEffect::new;
    }
}
