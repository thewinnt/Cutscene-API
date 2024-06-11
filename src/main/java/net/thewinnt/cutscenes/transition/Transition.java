package net.thewinnt.cutscenes.transition;

import java.util.function.Function;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.path.Path;

/**
 * A Transition provides a smooth change between the player doing their business and watching a cutscene.
 */
public interface Transition {
    /** @return the total length of this transition. */
    int getLength();

    /**
     * Returns the time that will be added to the total cutscene time as a result of this transition running.
     * Should not change during a cutscene.
     * @return the time spent outside the cutscene's length
     */
    int getOffCutsceneTime();

    /**
     * Returns the time of this transition that will be taken from the cutscene's time. This means that the
     * cutscene itself will be running, so a path will be shown.
     * @return the time spent inside the cutscene's length
     */
    int getOnCutsceneTime();

    /**
     * Returns the camera position at the specified moment
     * @param progress the progress of the transition [0-1]
     * @param level the level the transition is executed in
     * @param startPos the cutscene start position (specified in the command, defaults to [0, 100, 0])
     * @param pathRot the rotation of the path
     * @param initCamPos the camera position right before the cutscene started
     * @param cutscene the cutscene the transition is associated with
     */
    Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene);

    /**
     * Returns the camera rotation at the specified moment
     * @param progress the progress of the transition [0-1]
     * @param level the level the transition is executed in
     * @param startPos the cutscene start position (world coordinates)
     * @param startRot the start rotation (specified in the command defaults to [0, 0, 0])
     * @param initCamRot the camera rotation right before the cutscene started
     * @param cutscene the cutscene the transition is associated with
     */
    Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene);

    /**
     * Saves this transition for network transfer, so that it can be fully recreated on the receiving end.
     * @param buf the buffer to write to
     */
    void toNetwork(FriendlyByteBuf buf);

    /**
     * @return a serializer that creates transitions of this type
     */
    TransitionSerializer<?> getSerializer();

    /**
     * Executed when the transition begins. It is guaranteed to be executed exactly once during the cutscene
     * if it isn't stopped before this transition begins.
     * @param cutscene the cutscene this transition is associated with
     */
    default void onStart(CutsceneType cutscene) {}

    /**
     * Executed when the transition ends. It is guaranteed to be executed exactly once during the cutscene
     * if it isn't stopped before this transition begins.
     * @param cutscene the cutscene this transition is associated with
     */
    default void onEnd(CutsceneType cutscene) {}

    /**
     * Executed every frame that this transition is active. This may not ever happen if the transition is too short.
     * @param progress the progress of this transition
     * @param cutscene the cutscene this transition is associated with
     */
    default void onFrame(double progress, CutsceneType cutscene) {}

    public static Transition fromJSON(JsonObject json) {
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(json, "type"));
        TransitionSerializer<?> serializer = CutsceneManager.getTransitionType(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown transition type: " + type);
        }
        return serializer.fromJSON(json);
    }

    /**
     * Returns a Transition from json, or a fallback value if json is null
     */
    public static Transition fromJSON(JsonObject json, Transition defaultIfNull) {
        if (json == null) return defaultIfNull;
        return fromJSON(json);
    }

    public static Transition fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        TransitionSerializer<?> serializer = CutsceneManager.getTransitionType(type);
        if (serializer == null) {
            throw new IllegalStateException("Received an invalid transition type: " + type);
        }
        return serializer.fromNetwork(buf);
    }

    /**
     * A transition serializer, representing a transition type.
     */
    public static interface TransitionSerializer<T extends Transition> {
        /**
         * Recreates a transition from network, matching its server-side version as close as possible.
         * @param buf the buffer to read from. The data in this buffer is enough to fully recreate the
         *            original segment type.
         * @return a transition reconstructed from network
         */
        T fromNetwork(FriendlyByteBuf buf);

        /**
         * Loads a transition from a JSON object. The object created from here is stored on the server, and then
         * serialized to network to be reconstructed on the client.
         * @param json the JSON object with settings for this transition type. It may not be enough to create a
         *             meaningful object.
         * @return a transition created from given JSON.
         * @throws IllegalArgumentException if there's not enough data to create a transition, or it is invalid
         */
        T fromJSON(JsonObject json);

        /**
         * A helper method to create a segment serializer from 2 functions.
         * @param network a {@link #fromNetwork(FriendlyByteBuf)} implementation
         * @param json a {@link #fromJSON(JsonObject)} implementation
         * @return a segment serializer for the given type
         * @param <T> the class for the segment type
         * @see net.thewinnt.cutscenes.CutsceneManager#BEZIER
         */
        public static <T extends Transition> TransitionSerializer<T> of(Function<FriendlyByteBuf, T> network, Function<JsonObject, T> json) {
            return new TransitionSerializer<T>() {
                @Override
                public T fromNetwork(FriendlyByteBuf buf) {
                    return network.apply(buf);
                }

                @Override
                public T fromJSON(JsonObject j) {
                    return json.apply(j);
                }
            };
        }
    }
}
