package net.thewinnt.cutscenes.rotation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.rotation.handler.CutsceneRotation;

/**
 * Defines how to handle the player's rotation.
 */
public interface RotationHandler {
    ResourceLocation UNKNOWN = ResourceLocation.withDefaultNamespace("null");
    /**
     * Applies the rotation transform. There is at most one rotation handler ticked at any point in time, so you
     * can freely store data in your instances.
     *
     * @param initCamRot  the initial player rotation, right before the cutscene started
     * @param startRot    the start rotation of the cutscene, aka the third argument in
     *                    {@link CutsceneManager#startCutscene(ResourceLocation, Vec3, Vec3, Vec3, ServerPlayer) CutsceneManager#startCutscene}
     * @param playerRot   the current player rotation (as if the cutscene never began)
     * @param cutsceneRot the rotation value output by the cutscene, relative to zero
     * @param dt          the time difference since last call of this method, in <b>seconds</b>.
     *                    Always less than or equal to zero on the first call to this during a cutscene.
     * @return the camera rotation that the player will ultimately see
     */
    Vec3 apply(Vec3 initCamRot, Vec3 startRot, Vec3 playerRot, Vec3 cutsceneRot, double dt);

    /**
     * Returns a serializer suitable for this rotation handler
     * @return a serializer that can be used with this rotation handler
     */
    RotationSerializer<?> serializer();

    @SuppressWarnings("unchecked")
    static <T extends RotationHandler> void toNetwork(FriendlyByteBuf buf, T handler) {
        RotationSerializer<T> serializer = ((RotationSerializer<T>) handler.serializer());
        ResourceLocation id = CutsceneAPI.ROTATION_HANDLERS.getKey(serializer);
        if (id == null) {
            CutsceneAPI.LOGGER.error("Unregistered rotation serializer: {}", serializer);
            buf.writeResourceLocation(UNKNOWN);
            return;
        }
        buf.writeResourceLocation(id);
        serializer.toNetwork(buf, handler);
    }

    static RotationHandler fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        if (id.equals(UNKNOWN)) {
            CutsceneAPI.LOGGER.warn("Unknown rotation handler, returning default (cutscenes:block). Check server logs for more details.");
            return CutsceneRotation.INSTANCE;
        }
        RotationSerializer<?> serializer = CutsceneAPI.ROTATION_HANDLERS.getValue(id);
        if (serializer == null) {
            CutsceneAPI.LOGGER.warn("Unknown rotation handler: {}, returning default (cutscenes:block)", id);
            return CutsceneRotation.INSTANCE;
        }
        return serializer.fromNetwork(buf);
    }

    static RotationHandler fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            String id = json.getAsString();
            if (RotationSerializer.SIMPLE_HANDLERS.containsKey(id)) {
                return RotationSerializer.SIMPLE_HANDLERS.get(id);
            }
            CutsceneAPI.LOGGER.error("Unknown simple rotation handler: {}, returning default (cutscenes:block)", id);
            return CutsceneRotation.INSTANCE;
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(obj, "type"));
            RotationSerializer<?> serializer = CutsceneAPI.ROTATION_HANDLERS.getValue(id);
            if (serializer != null) {
                return serializer.fromJson(obj);
            }
            CutsceneAPI.LOGGER.error("Unknown complex rotation handler: {}, returning default (cutscenes:block)", id);
            return CutsceneRotation.INSTANCE;
        }
        CutsceneAPI.LOGGER.error("Invalid rotation handler: {}", json);
        return CutsceneRotation.INSTANCE;
    }
}
