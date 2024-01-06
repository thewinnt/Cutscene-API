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

public interface Transition {
    int getLength();
    boolean countTowardsCutsceneTime();
    /**
     * Returns the camera position at the specified moment
     * @param progress the progress of the transition [0-1]
     * @param level
     * @param startPos the cutscene start position (specified in the command, defaults to [0, 100, 0])
     * @param pathRot the rotation of the path
     * @param initCamPos the camera position right before the cutscene started
     * @param cutscene
     */
    Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene);
    /**
     * Returns the camera rotation at the specified moment
     * @param progress the progress of the transition [0-1]
     * @param level
     * @param startPos the cutscene start position (world coordinates)
     * @param startRot the start rotation (specified in the command defaults to [0, 0, 0])
     * @param initCamRot the camera rotation right before the cutscene started
     * @param cutscene
     */
    Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene);
    void toNetwork(FriendlyByteBuf buf);
    TransitionSerializer<?> getSerializer();
    default void onStart(CutsceneType cutscene) {}
    default void onEnd(CutsceneType cutscene) {}
    default void onFrame(double progress, CutsceneType cutscene) {}

    public static Transition fromJSON(JsonObject json) {
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(json, "type"));
        TransitionSerializer<?> serializer = CutsceneManager.getTransitionType(type);
        return serializer.fromJSON(json);
    }

    public static Transition fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        TransitionSerializer<?> serializer = CutsceneManager.getTransitionType(type);
        return serializer.fromNetwork(buf);
    }

    public static interface TransitionSerializer<T extends Transition> {
        T fromNetwork(FriendlyByteBuf buf);
        T fromJSON(JsonObject json);
        
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
