package net.thewinnt.cutscenes.path;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.preview.PathPreviewRenderer.Line;
import net.thewinnt.cutscenes.path.point.PointProvider;

/**
 * A PathLike is the base interface responsible for camera positioning and rotating in a cutscene.
 * They are also commonly referred to as "segment serializers".
 */
public interface PathLike {
    /**
     * Returns the position at the given moment. Depending on the context, it may be treated differently:
     * <ul>
     *     <li>If the segment is representing the camera position, the resulting position
     *         will be treated as an offset from the cutscene starting position, which will then be rotated
     *         around the starting point by three axes, depending on the parameters the cutscene was started with.</li>
     *     <li>If the segment is representing camera rotation, this method's output will be treated as an
     *         offset from the starting camera rotation of the cutscene.</li>
     * </ul>
     * @param t the progress of the current segment, in range [0, 1]. 0 corresponds to the starting position
     *          and 1 is the end position.
     * @param level the level where the cutscene is run 
     * @param cutsceneStart the starting position of the cutscene
     * @return a point corresponding to the given progress value of the segment.
     */
    Vec3 getPoint(double t, Level level, Vec3 cutsceneStart);

    /**
     * Returns a {@link PointProvider} representing the starting position for this segment.
     * Ideally, this should match the output of {@code getPoint(0, level, cutsceneStart)}.
     * Used in {@link Path paths} and {@link PathTransition path transitions}. 
     * @param level the level where the cutscene is run
     * @param cutsceneStart the starting position of the cutscene
     * @return the {@link PointProvider} representing the starting position for this segment
     */
    PointProvider getStart(Level level, Vec3 cutsceneStart);

    /**
     * Returns a {@link PointProvider} representing the end position for this segment.
     * Ideally, this should match the output of {@code getPoint(1, level, cutsceneStart)}.
     * Used in {@link Path paths} and {@link PathTransition path transitions}. 
     * @param level the level where the cutscene is run
     * @param cutsceneStart the starting position of the cutscene
     * @return the {@link PointProvider} representing the end position for this segment
     */
    PointProvider getEnd(Level level, Vec3 cutsceneStart);

    /**
     * Returns the weight of this segment. Used for timing in {@link Path paths}.
     * The higher the value, the more time is dedicated to this segment and the slower
     * it runs, and vice-versa.
     * @return this segment's weight
     */
    int getWeight();

    /**
     * Writes this segment's parameters to the provided {@link FriendlyByteBuf}.
     * This data should be enough to fully reconstruct this segment on the client.
     * @see SegmentSerializer#fromNetwork(FriendlyByteBuf, Path)
     */
    void toNetwork(FriendlyByteBuf buf);

    /**
     * Returns the serializer that is associated with this segment type. Preferably,
     * this should return the same object every time it's called.
     * @return a serializer for this segment's type
     */
    SegmentSerializer<?> getSerializer();

    /**
     * Returns a list of {@link Line lines} representing the utility points used for
     * constructing this segment's path. Used for rendering a cutscene's preview.
     * @param level the level where the preview is rendered
     * @param cutsceneStart the starting position of the cutscene preview
     * @param initLevel the depth level for this segment. For every segment nested inside another
     *                  segment, this value increases by 1. This argument should be passed to created lines
     *                  without changing it.
     * @return a collection of lines representing this segment's utility points.
     * @see BezierCurve#getUtilityPoints(Level, Vec3, int)
     * @see CatmullRomSpline#getUtilityPoints(Level, Vec3, int)
     */
    default Collection<Line> getUtilityPoints(Level level, Vec3 cutsceneStart, int initLevel) {
        return Collections.emptySet();
    };

    /** An object that constructs path segments from JSON and network. */
    public static interface SegmentSerializer<T extends PathLike> {
        /**
         * Reconstructs a segment from network, matching its server companion as closely as possible.
         * @param buf the buffer to read from. The data in this buffer is enough to fully recreate the
         *            original segment type.
         * @param path the path this segment belongs on. <b>DO NOT ADD THE RESULTING SEGMENT TO THIS PATH!</b>
         *             At the moment of construction, the constructed element is the last in the path, so its
         *             future index is equal to the current size of the path.
         * @return a segment reconstructed from network.
         */
        T fromNetwork(FriendlyByteBuf buf, Path path);

        /**
         * Reads a segment from JSON. The object created from here is stored on the server, and then serialized to
         * network to be reconstructed on the client.
         * @param json the JSON object representing this segment. It may not contain all the properties this segment
         *             has.
         * @param path the path this segment belongs on. <b>DO NOT ADD THE RESULTING SEGMENT TO THIS PATH!</b>
         *             At the moment of construction, the constructed element is the last in the path, so its
         *             future index is equal to the current size of the path.
         * @return a segment created from the given JSON object.
         * @throws IllegalArgumentException if there's not enough data to create a segment, or it is invalid
         */
        T fromJSON(JsonObject json, Path path);

        /**
         * A helper method to create a segment serializer from 2 functions.
         * @param network a {@link #fromNetwork(FriendlyByteBuf, Path)} implementation
         * @param json a {@link #fromJSON(JsonObject, Path)} implementation
         * @return a segment serializer for the given type
         * @param <T> the class for the segment type
         * @see net.thewinnt.cutscenes.CutsceneManager#BEZIER
         */
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
