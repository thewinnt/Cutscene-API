package net.thewinnt.cutscenes.path;

import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.path.point.PointProvider;

public interface PathLike {
    Vec3 getPoint(double t, Level level, Vec3 cutsceneStart);
    PointProvider getStart(Level level, Vec3 cutsceneStart);
    PointProvider getEnd(Level level, Vec3 cutsceneStart);
    int getWeight();
    void toNetwork(FriendlyByteBuf buf);
    SegmentSerializer<?> getSerializer();

    public static interface SegmentSerializer<T extends PathLike> {
        T fromNetwork(FriendlyByteBuf buf, Path path);
        T fromJSON(JsonObject json, Path path);

        public static <T extends PathLike> SegmentSerializer<T> of(BiFunction<FriendlyByteBuf, Path, T> network, BiFunction<JsonObject, Path, T> json) {
            return new SegmentSerializer<T>() {
                @Override
                public T fromNetwork(FriendlyByteBuf buf, Path path) {
                    return network.apply(buf, path);
                }

                @Override
                public T fromJSON(JsonObject j, Path path) {
                    return json.apply(j, path);
                }
            };
        }
    }
}
