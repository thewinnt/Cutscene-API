package net.thewinnt.cutscenes.easing.serializers;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.CompoundEasing;
import net.thewinnt.cutscenes.easing.types.CompoundEasing.RangeAppliedEasing;
import net.thewinnt.cutscenes.easing.types.CompoundEasing.TimedEasingEntry;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompoundEasingSerializer implements EasingSerializer<CompoundEasing> {
    public static final CompoundEasingSerializer INSTANCE = new CompoundEasingSerializer();
    public static final Codec<Map<String, RangeAppliedEasing>> MAP_CODEC = Codec.unboundedMap(Codec.DOUBLE.xmap(d -> Double.toString(d), Double::parseDouble), RangeAppliedEasing.CODEC);
    public static final MapCodec<CompoundEasing> CODEC = MAP_CODEC.xmap(CompoundEasing::new, CompoundEasing::asMap).fieldOf("entries");

    private CompoundEasingSerializer() {}

    @Override
    public CompoundEasing fromNetwork(FriendlyByteBuf buf) {
        int size = buf.readInt();
        ArrayList<TimedEasingEntry> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double time = buf.readDouble();
            double minValue = buf.readDouble();
            double maxValue = buf.readDouble();
            Easing easing = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
            data.add(new TimedEasingEntry(time, new RangeAppliedEasing(minValue, maxValue, easing)));
        }
        return new CompoundEasing(data);
    }

    @Override
    public CompoundEasing fromJSON(JsonObject json, LoadingContext context) {
        JsonObject obj = GsonHelper.getAsJsonObject(json, "entries");
        List<TimedEasingEntry> entries = new ArrayList<>();
        for (var i : obj.asMap().entrySet()) {
            double time = context.wrapLoading("time", () -> Double.parseDouble(i.getKey()), 0d);
            RangeAppliedEasing easing = context.wrapLoading("entry@" + time, () -> RangeAppliedEasing.fromJSON(i.getValue(), context));
            entries.add(new TimedEasingEntry(time, easing));
        }
        return new CompoundEasing(entries);
    }

    @Override
    public MapCodec<CompoundEasing> codec() {
        return CODEC;
    }
}
