package net.thewinnt.cutscenes;

import java.util.ArrayList;
import java.util.List;

import com.mojang.logging.LogUtils;
import net.thewinnt.cutscenes.effect.ServerEffectWrapper;
import net.thewinnt.cutscenes.rotation.handler.CutsceneRotation;
import net.thewinnt.cutscenes.rotation.handler.PlayerRotation;
import net.thewinnt.cutscenes.time.CutsceneLength;
import net.thewinnt.cutscenes.time.GameTickManager;
import net.thewinnt.cutscenes.rotation.RotationHandler;
import net.thewinnt.cutscenes.time.TimeManager;
import net.thewinnt.cutscenes.util.LoadingContext;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition;
import net.thewinnt.cutscenes.util.ActionToggles;
import net.thewinnt.cutscenes.util.ActionToggles.Builder;
import net.thewinnt.cutscenes.util.JsonHelper;
import org.slf4j.Logger;

/**
 * A cutscene type consists of a camera path, rotation, transitions and some parameters. A cutscene has a fixed length,
 * and it can choose to let the player move their body and/or camera and hide their hand and block outlines.
 * When a cutscene is run, it's given a starting position and a starting camera rotation. The cutscene's path can be
 * rotated by some amount.
 */
public class CutsceneType {
    public static final Logger LOGGER = LogUtils.getLogger();
    public final CutsceneLength length;
    public final @Nullable Path path;
    public final @Nullable Path rotationProvider;
    public final Transition startTransition;
    public final Transition endTransition;
    public final boolean blockMovement;
    public final RotationHandler rotationHandler;
    public final ActionToggles actionToggles;
    public final boolean hideHand;
    public final boolean hideBlockOutline;
    public final boolean disableF5;
    public final List<CutsceneEffect<?>> effects;

    /** Constructs a cutscene type with all parameters specified. */
    public CutsceneType(PathLike path, Path rotationProvider, CutsceneLength length, Transition start, Transition end, boolean blockMovement, RotationHandler rotationHandler, ActionToggles toggles, boolean hideHand, boolean hideBlockOutline, List<CutsceneEffect<?>> effects) {
        if (path instanceof Path pth) {
            this.path = pth;
        } else if (path != null) {
            this.path = new Path(path);
        } else {
            this.path = null;
        }
        this.rotationProvider = rotationProvider;
        this.length = length;
        this.startTransition = start;
        this.endTransition = end;
        this.blockMovement = path != null || blockMovement; // if there's a path, you can't block movement
        this.rotationHandler = rotationHandler;
        this.actionToggles = toggles;
        this.hideHand = hideHand;
        this.hideBlockOutline = hideBlockOutline;
        this.effects = effects;
        this.disableF5 = path != null || rotationProvider != null || actionToggles.disablePerspectiveChanging();
    }

    /** Constructs a simple cutscene type with default parameters for most settings. */
    public CutsceneType(PathLike path, Path rotationProvider, int length) {
        if (path instanceof Path pth) {
            this.path = pth;
        } else if (path != null) {
            this.path = new Path(path);
        } else {
            this.path = null;
        }
        this.rotationProvider = rotationProvider;
        this.length = new CutsceneLength(length, new GameTickManager());
        this.startTransition = new SmoothEaseTransition(40, true, true);
        this.endTransition = new SmoothEaseTransition(40, false, false);
        this.blockMovement = true;
        this.rotationHandler = CutsceneRotation.INSTANCE;
        this.actionToggles = new Builder(true).build();
        this.hideHand = false;
        this.hideBlockOutline = false;
        this.effects = List.of();
        this.disableF5 = path != null || rotationProvider != null;
    }

    /**
     * Returns a point for this cutscene's camera path at the specified progress value, if there is a path.
     * @param point the progress of this cutscene, in range [0, 1].
     * @param level the level where the cutscene is run
     * @param cutsceneStart the cutscene's starting position
     * @return a point for the given progress, or {@code null} if there's no path
     */
    @Nullable
    public Vec3 getPathPoint(double point, Level level, Vec3 cutsceneStart) {
        if (path == null) return null;
        return path.getPoint(point, level, cutsceneStart);
    }

    /**
     * Returns a point for this cutscene's camera rotation at the specified progress value, if there is any rotation.
     * @param point the progress of this cutscene, in range [0, 1].
     * @param level the level where the cutscene is run
     * @param cutsceneStart the cutscene's starting position
     * @return a point for the given progress, or {@code null} if there's no path. The returned point's coordinates
     * are [yaw, pitch, roll], matching Minecraft's [y, x, z] coordinates respectively.
     */
    @Nullable
    public Vec3 getRotationAt(double point, Level level, Vec3 cutsceneStart) {
        if (rotationProvider == null) return null;
        return rotationProvider.getPoint(point, level, cutsceneStart);
    }

    /**
     * Returns a point for this cutscene's camera rotation at given progress value, transformed by the
     * rotation handler.
     * @param point the progress value, in range [0, 1]
     * @param level the world where the cutscene is run
     * @param cutsceneStart the cutscene's starting position
     * @param initCamRot the player's camera rotation before the cutscene began
     * @param startRot the starting rotation of the cutscene
     * @param playerRot the player's current rotation
     * @param dt the time since last call, in seconds
     * @return a point for the given progress, in format [yaw, pitch, roll] (aka minecraft [y, x, z]).
     */
    public Vec3 getTransformedRotation(double point, Level level, Vec3 cutsceneStart, Vec3 initCamRot, Vec3 startRot, Vec3 playerRot, double dt) {
        Vec3 output;
        if (rotationProvider == null) {
            output = Vec3.ZERO;
        } else {
            output = rotationProvider.getPoint(point, level, cutsceneStart);
        }
        return rotationHandler.apply(initCamRot, startRot, playerRot, output, dt);
    }

    /** Serializes this cutscene type to network, to fully reconstruct it later on the client side. */
    public void toNetwork(FriendlyByteBuf buf) {
        length.toNetwork(buf);
        buf.writeNullable(path, (buf1, path) -> path.toNetwork(buf1));
        buf.writeNullable(rotationProvider, (buf1, path) -> path.toNetwork(buf1));
        buf.writeResourceLocation(CutsceneManager.getTransitionTypeId(startTransition.getSerializer()));
        startTransition.toNetwork(buf);
        buf.writeResourceLocation(CutsceneManager.getTransitionTypeId(endTransition.getSerializer()));
        endTransition.toNetwork(buf);
        buf.writeBoolean(blockMovement);
        RotationHandler.toNetwork(buf, rotationHandler);
        actionToggles.toNetwork(buf);
        buf.writeBoolean(hideHand);
        buf.writeBoolean(hideBlockOutline);
        buf.writeCollection(effects, (buf1, cutsceneEffect) -> cutsceneEffect.toNetwork(buf1));
    }

    /** Reads a cutscene type from network. */
    public static CutsceneType fromNetwork(FriendlyByteBuf buf) {
        CutsceneLength length = CutsceneLength.fromNetwork(buf);
        Path path = buf.readNullable(buf1 -> Path.fromNetwork(buf1, null));
        Path rotationProvider = buf.readNullable(buf1 -> Path.fromNetwork(buf1, path));
        Transition start = Transition.fromNetwork(buf);
        Transition end = Transition.fromNetwork(buf);
        boolean blockMovement = buf.readBoolean();
        RotationHandler rotationHandler = RotationHandler.fromNetwork(buf);
        ActionToggles actionToggles = ActionToggles.fromNetwork(buf);
        boolean hideHand = buf.readBoolean();
        boolean hideBlockOutline = buf.readBoolean();
        List<CutsceneEffect<?>> effects = buf.readCollection(ArrayList::new, CutsceneEffect::fromNetwork);
        return new CutsceneType(path, rotationProvider, length, start, end, blockMovement, rotationHandler, actionToggles, hideHand, hideBlockOutline, effects);
    }

    /** Reads a cutscene type from JSON. */
    public static CutsceneType fromJSON(JsonObject json, LoadingContext context) {
        int dataVersion;
        JsonElement dataVersionJson = json.get("version");
        if (dataVersionJson != null) {
            dataVersion = dataVersionJson.getAsInt();
        } else {
            LOGGER.info("Loading a cutscene with no version, assuming it is 0. Current version is {}.", CutsceneAPI.DATA_VERSION);
            dataVersion = 0;
        }
        if (dataVersion > CutsceneAPI.DATA_VERSION) {
            LOGGER.warn("Loading a cutscene with version {}, which is newer than the current one ({}). Things may break!", dataVersion, CutsceneAPI.DATA_VERSION);
        } else if (dataVersion < CutsceneAPI.DATA_VERSION) {
            LOGGER.warn("Loading a cutscene with version {}, which is earlier than the current one ({}). The cutscene should be updated to the new format to make sure it works correctly!", dataVersion, CutsceneAPI.DATA_VERSION);
        }
        context.setDataVersion(dataVersion);
        CutsceneLength length = CutsceneLength.fromJson(json.get("length"));
        Path path = context.wrapLoading("path", () -> Path.fromJSON(JsonHelper.getNullableObject(json, "path"), null, context));
        Path rotation = context.wrapLoading("rotation", () -> Path.fromJSON(JsonHelper.getNullableObject(json, "rotation"), path, context));
        Transition start = context.wrapLoading("start_transition", () -> Transition.fromJSON(JsonHelper.getNullableObject(json, "start_transition"), context, TimeManager.DEFAULT_TRANSITIONS.get(length.manager().type()).get(true)));
        Transition end = context.wrapLoading("end_transition", () -> Transition.fromJSON(JsonHelper.getNullableObject(json, "end_transition"), context, TimeManager.DEFAULT_TRANSITIONS.get(length.manager().type()).get(false)));
        boolean blockMovement = GsonHelper.getAsBoolean(json, "block_movement", false) || path != null;
        RotationHandler rotationHandler;
        if (json.has("block_rotation") || !json.has("rotation_handler")) {
            boolean blockRotation = GsonHelper.getAsBoolean(json, "block_rotation", false) || rotation != null;
            rotationHandler = blockRotation ? CutsceneRotation.INSTANCE : PlayerRotation.INSTANCE;
        } else {
            rotationHandler = context.wrapLoading("rotation_handler", () -> RotationHandler.fromJson(json.get("rotation_handler"), context));
        }
        ActionToggles toggles;
        if (json.has("disable_actions")) {
            toggles = ActionToggles.fromJson(json.get("disable_actions"));
        } else {
            toggles = new ActionToggles.Builder(true).build();
        }
        boolean defaultHideBlockOutline = toggles.disableBreakingBlocks() && toggles.disableBlockInteractions() && toggles.disablePickingBlocks();
        boolean defaultHideHand = defaultHideBlockOutline && toggles.disableAttacking() && toggles.disableUsingItems() && toggles.disableEntityInteractions();
        boolean hideHand = GsonHelper.getAsBoolean(json, "hide_hand", defaultHideHand);
        boolean hideBlockOutline = GsonHelper.getAsBoolean(json, "hide_block_outline", defaultHideBlockOutline);
        JsonArray effectsJson = GsonHelper.getAsJsonArray(json, "effects", new JsonArray());
        ArrayList<CutsceneEffect<?>> effects = new ArrayList<>();
        int index = 0;
        for (JsonElement i : effectsJson) {
            context.pushElement("effects[" + (index++) + ']');
            ServerEffectWrapper<?> effect = CutsceneEffect.fromJSON(GsonHelper.convertToJsonObject(i, "effect"), context);
            if (effect != null) {
                effects.add(effect);
            }
            context.popElement();
        }
        return new CutsceneType(path, rotation, length, start, end, blockMovement, rotationHandler, toggles, hideHand, hideBlockOutline, effects);
    }
}
