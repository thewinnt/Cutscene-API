package net.thewinnt.cutscenes.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneInstance;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;
import net.thewinnt.cutscenes.util.ActionToggles;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.Map;

public class ClientCutsceneManager {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final BiMap<ResourceLocation, CutsceneType> CLIENT_REGISTRY = HashBiMap.create();
    public static final ActionToggles DEFAULT_ACTION_TOGGLES = new ActionToggles.Builder(false).build();
    public static boolean renderedOverlaysThisFrame = false; // whether the overlays were rendered this frame
    private static boolean isCutsceneRunning = false;
    public static CutsceneInstance runningCutscene;
    private static Vec3 startPosition;
    private static long startGameTime;
    public static float startCameraYaw; // x
    public static float startCameraPitch; // y
    public static float startCameraRoll; // z
    public static float startPathYaw;
    public static float startPathPitch;
    public static float startPathRoll;
    private static double lastFrameTime;
    private static double dt;
    private static Vec3 cutsceneRot;

    private static CutsceneType previewedCutscene = null;
    public static Vec3 previewOffset;
    public static float previewPathYaw;
    public static float previewPathPitch;
    public static float previewPathRoll;

    public static CutsceneCameraEntity camera;
    public static float initCameraYaw;
    public static float initCameraPitch;
    public static float initCameraRoll;
    
    @Environment(EnvType.CLIENT)
    public static void startCutscene(CutsceneType type, Vec3 startPos, float cameraYaw, float cameraPitch, float cameraRoll, float pathYaw, float pathPitch, float pathRoll, long gameTime) {
        CutsceneAPI.updateSalt();
        stopCutsceneImmediate(EndingReason.INTERRUPT);
        // if the specified rotation value is NaN, use the initial values
        startCameraYaw = Float.isNaN(cameraYaw) ? initCameraYaw : cameraYaw;
        startCameraPitch = Float.isNaN(cameraPitch) ? initCameraPitch : cameraPitch;
        startCameraRoll = Float.isNaN(cameraRoll) ? initCameraRoll : cameraRoll;
        startPathYaw = pathYaw;
        startPathPitch = pathPitch;
        startPathRoll = pathRoll;
        startGameTime = gameTime;
        runningCutscene = new CutsceneInstance(type);
        // initialize minecraft
        Minecraft minecraft = Minecraft.getInstance();
        if (runningCutscene.cutscene.hideHand) {
            minecraft.gameRenderer.setRenderHand(false);
        }
        if (runningCutscene.cutscene.hideBlockOutline) {
            minecraft.gameRenderer.setRenderBlockOutline(false);
        }
        camera = new CutsceneCameraEntity(-69420, runningCutscene, startPos, startCameraYaw, startCameraPitch, pathYaw, pathPitch, pathRoll);
        if (runningCutscene.cutscene.blockMovement) { // special case: keep the player if we want them to move
            camera.spawn();
            minecraft.setCameraEntity(camera);
        }
        PointProvider.POINT_CACHE.clear();

        isCutsceneRunning = true;
        startPosition = startPos;
    }

    public static void updateRegistry(Map<ResourceLocation, CutsceneType> registry) {
        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(registry);
        if (isCutsceneRunning) CutsceneAPI.updateSalt();
    }

    public static void registerCutscene(ResourceLocation id, CutsceneType type) {
        CLIENT_REGISTRY.put(id, type);
    }

    public static void stopCutsceneImmediate(EndingReason reason) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.setRenderHand(true);
        minecraft.setCameraEntity(minecraft.player);
        minecraft.gameRenderer.setRenderBlockOutline(true);
        if (camera != null) {
            camera.despawn();
            if (!runningCutscene.endedStartTransition() && runningCutscene.isTimeForStart()) {
                runningCutscene.cutscene.startTransition.onEnd(runningCutscene.cutscene);
            }
            if (!runningCutscene.endedEndTransition() && runningCutscene.isTimeForEnd()) {
                runningCutscene.cutscene.endTransition.onEnd(runningCutscene.cutscene);
            }
        }
        camera = null;
        if (minecraft.player != null) {
            minecraft.player.input = new KeyboardInput(minecraft.options);
        }
        isCutsceneRunning = false;
        if (runningCutscene != null) {
            CutsceneEvents.CUTSCENE_OVER_CLIENT.invoke(listener -> listener.accept(runningCutscene.cutscene, CLIENT_REGISTRY.inverse().get(runningCutscene.cutscene), minecraft.player, reason));
            if (reason != EndingReason.FINISH) {
                runningCutscene.interrupt();
            }
        }
        runningCutscene = null;
        CutsceneOverlayManager.clearOverlays();
    }

    public static void setPreviewedCutscene(CutsceneType preview, Vec3 offset, float pathYaw, float pathPitch, float pathRoll) {
        CutsceneAPI.updateSalt();
        previewedCutscene = preview;
        previewOffset = offset;
        previewPathYaw = pathYaw;
        previewPathPitch = pathPitch;
        previewPathRoll = pathRoll;
    }

    public static CutsceneType getPreviewedCutscene() {
        return previewedCutscene;
    }

    public static Vec3 getOffset() {
        if (previewOffset != null) {
            return previewOffset;
        } else {
            return new Vec3(0, 100, 0);
        }
    }

    public static void setCameraPosition(CameraAngleSetter event) {
        if (isCutsceneRunning) {
            if (camera == null) {
                LOGGER.warn("Found ourselves running a cutscene despite the camera being null. Is this normal?");
                stopCutsceneImmediate(EndingReason.ERROR);
                return;
            }
            if (runningCutscene == null) {
                LOGGER.error("Attempted to run an invalid cutscene!");
                stopCutsceneImmediate(EndingReason.ERROR);
                return;
            }
            Level level = Minecraft.getInstance().level;
            Minecraft.getInstance().getProfiler().push("cutscene_tick");
            double now = now();
            dt = now - lastFrameTime;
            lastFrameTime = now;
            if (!runningCutscene.isInitialized()) {
                dt = 0;
            }
            if (runningCutscene.tick()) {
                Minecraft.getInstance().getProfiler().popPush("rotation");
                Vector3f finalRot;
                Vec3 initCamRot = new Vec3(initCameraYaw, initCameraPitch, initCameraRoll);
                Vec3 startRot = new Vec3(startCameraYaw, startCameraPitch, startCameraRoll);
                Vec3 playerRot = camera.getPlayerCamRot();
                if (runningCutscene.cutscene.rotationProvider != null) {
                    double progress = (runningCutscene.getTime() - runningCutscene.cutscene.startTransition.getOffCutsceneTime()) / runningCutscene.cutscene.length.length();
                    cutsceneRot = runningCutscene.cutscene.getRotationAt(progress, level, startPosition);
                    cutsceneRot = runningCutscene.cutscene.rotationHandler.apply(initCamRot, startRot, playerRot, cutsceneRot, dt);
                    if (runningCutscene.isTimeForStart()) {
                        progress = runningCutscene.getTime() / runningCutscene.cutscene.startTransition.getLength();
                        finalRot = runningCutscene.cutscene.startTransition.getRot(progress, level, startPosition, startRot, camera.getPlayerCamRot(), runningCutscene.cutscene).toVector3f();
                    } else if (runningCutscene.isTimeForEnd()) {
                        progress = runningCutscene.getEndProress();
                        finalRot = runningCutscene.cutscene.endTransition.getRot(progress, level, startPosition, startRot, camera.getPlayerCamRot(), runningCutscene.cutscene).toVector3f();
                    } else {
                        finalRot = cutsceneRot.toVector3f();
                    }
                } else {
                    finalRot = runningCutscene.cutscene.rotationHandler.apply(initCamRot, startRot, playerRot, Vec3.ZERO, dt).toVector3f();
                }
                camera.setYRot(finalRot.x);
                camera.setXRot(finalRot.y);
                event.setYaw(finalRot.x);
                event.setPitch(finalRot.y);
                event.setRoll(finalRot.z);
                Minecraft.getInstance().getProfiler().pop();
            }
            Minecraft.getInstance().getProfiler().pop();
        } else {
            initCameraYaw = event.getYaw();
            initCameraPitch = event.getPitch();
            initCameraRoll = event.getRoll();
            dt = -1;
        }
    }

    public static void onLogout() {
        stopCutsceneImmediate(EndingReason.INTERRUPT);
        previewedCutscene = null;
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (isCutsceneRunning && runningCutscene.cutscene.blockMovement) {
            if (minecraft.player != null && minecraft.player.input instanceof KeyboardInput) {
                Input input = new Input();
                input.shiftKeyDown = minecraft.player.input.shiftKeyDown;
                minecraft.player.input = input;
            }
        }
    }

    public static ActionToggles actionToggles() {
        if (!isCutsceneRunning) return DEFAULT_ACTION_TOGGLES;
        return runningCutscene.cutscene.actionToggles;
    }

    public static boolean isCutsceneRunning() {
        return isCutsceneRunning;
    }

    public static long getStartGameTime() {
        return startGameTime;
    }

    public static double dt() {
        return dt;
    }

    public static Vec3 getCutsceneRotation() {
        return cutsceneRot;
    }

    private static double now() {
        return Util.getNanos() / 1000000000.0;
    }
}