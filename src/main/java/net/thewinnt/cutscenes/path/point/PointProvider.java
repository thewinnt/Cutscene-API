package net.thewinnt.cutscenes.path.point;

import java.util.function.Function;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface PointProvider {
    Vec3 getPoint(Level level, Vec3 cutsceneStart);
    void toNetwork(FriendlyByteBuf buf);
    PointSerializer<?> getSerializer();

    public static interface PointSerializer<T extends PointProvider> {
        T fromNetwork(FriendlyByteBuf buf);
        T fromJSON(JsonObject json);

        public static <T extends PointProvider> PointSerializer<T> of(Function<FriendlyByteBuf, T> network, Function<JsonObject, T> json) {
            return new PointSerializer<T>() {
                @Override
                public T fromNetwork(FriendlyByteBuf buf) {
                    return network.apply(buf);
                }

                @Override
                public T fromJSON(JsonObject obj) {
                    return json.apply(obj);
                }
            };
        }
    }
}
