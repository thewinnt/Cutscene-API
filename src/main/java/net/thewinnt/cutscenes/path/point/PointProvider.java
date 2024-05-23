package net.thewinnt.cutscenes.path.point;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.path.Path;

/** The base interface for point types. */
public interface PointProvider {
    /**
     * Returns the point this PointProvider represents.
     * @param level the level the point is being obtained in.
     * @param cutsceneStart the starting position of the cutscene
     * @return a point, relative to the starting position, or a rotation relative to the starting rotation
     * @see net.thewinnt.cutscenes.path.PathLike#getPoint(double, Level, Vec3) 
     */
    Vec3 getPoint(Level level, Vec3 cutsceneStart);

    /**
     * Writes this point provider's properties to network, so that it can be fully reconstructed later.
     * @see net.thewinnt.cutscenes.path.PathLike#toNetwork(FriendlyByteBuf)
     */
    void toNetwork(FriendlyByteBuf buf);

    /**
     * @return the serializer associated with this point provider's type. Ideally, this should always be
     * the same object.
     */
    PointSerializer<?> getSerializer();

    /** An object that constructs point providers from JSON and network. */
    public static interface PointSerializer<T extends PointProvider> {
        /**
         * Reconstructs a point from network, matching its server companion as closely as possible.
         * @param buf the buffer to read from. The data in this buffer is enough to fully recreate the
         *            original segment type.
         * @return a point reconstructed from network.
         * @see net.thewinnt.cutscenes.path.PathLike.SegmentSerializer#fromNetwork(FriendlyByteBuf, Path) 
         */
        T fromNetwork(FriendlyByteBuf buf);

        /**
         * Reads a point from JSON. The object created from here is stored on the server, and then serialized to
         * network to be reconstructed on the client.
         * @param json the JSON object representing this segment. It may not contain all the properties this segment
         *             has.
         * @return a point created from the given JSON object.
         * @throws IllegalArgumentException if there's not enough data to create a point, or it is invalid
         * @see net.thewinnt.cutscenes.path.PathLike.SegmentSerializer#fromNetwork(FriendlyByteBuf, Path)
         */
        T fromJSON(JsonObject json);

        /**
         * A helper method to create a segment serializer from 2 functions.
         * @param network a {@link #fromNetwork(FriendlyByteBuf)} implementation
         * @param json a {@link #fromJSON(JsonObject)} implementation
         * @return a segment serializer for the given type
         * @param <T> the class for the segment type
         * @see net.thewinnt.cutscenes.CutsceneManager#STATIC
         * @see net.thewinnt.cutscenes.path.PathLike.SegmentSerializer#of(BiFunction, BiFunction)
         */
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
