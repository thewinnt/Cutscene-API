package net.thewinnt.cutscenes.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneInstance;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;
import net.thewinnt.cutscenes.util.ActionToggles;

import java.util.Map;

@Mod.EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientCutsceneManager {
    public static final BiMap<ResourceLocation, CutsceneType> CLIENT_REGISTRY = HashBiMap.create();
    public static final ActionToggles DEFAULT_ACTION_TOGGLES = new ActionToggles.Builder(false).build();
    public static boolean renderedOverlaysThisFrame = false; // whether the overlays were rendered this frame
    private static boolean isCutsceneRunning = false;
    public static CutsceneInstance runningCutscene;
    private static Vec3 startPosition;
    public static float startCameraYaw; // x
    public static float startCameraPitch; // y
    public static float startCameraRoll; // z
    public static float startPathYaw;
    public static float startPathPitch;
    public static float startPathRoll;

    private static CutsceneType previewedCutscene = null;
    public static Vec3 previewOffset;
    public static float previewPathYaw;
    public static float previewPathPitch;
    public static float previewPathRoll;

    public static CameraType prevF5state;
    public static CutsceneCameraEntity camera;
    public static float initCameraYaw;
    public static float initCameraPitch;
    public static float initCameraRoll;
    private static boolean hidGuiBefore;
    
    @OnlyIn(Dist.CLIENT)
    public static void startCutscene(CutsceneType type, Vec3 startPos, float cameraYaw, float cameraPitch, float cameraRoll, float pathYaw, float pathPitch, float pathRoll) {
        CutsceneAPI.updateSalt();
        stopCutsceneImmediate();
        // if the specified rotation value is NaN, use the initial values
        startCameraYaw = Float.isNaN(cameraYaw) ? initCameraYaw : cameraYaw;
        startCameraPitch = Float.isNaN(cameraPitch) ? initCameraPitch : cameraPitch;
        startCameraRoll = Float.isNaN(cameraRoll) ? initCameraRoll : cameraRoll;
        startPathYaw = pathYaw;
        startPathPitch = pathPitch;
        startPathRoll = pathRoll;
        runningCutscene = new CutsceneInstance(type);

        // initialize minecraft
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.smartCull = false;
        if (runningCutscene.cutscene.hideHand) {
            minecraft.gameRenderer.setRenderHand(false);
        }
        if (runningCutscene.cutscene.hideBlockOutline) {
            minecraft.gameRenderer.setRenderBlockOutline(false);
        }
        prevF5state = minecraft.options.getCameraType();
        if (minecraft.gameRenderer.getMainCamera().isDetached()) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
        camera = new CutsceneCameraEntity(-69420, runningCutscene, startPos, startCameraYaw, startCameraPitch, pathYaw, pathPitch, pathRoll);
        if (runningCutscene.cutscene.blockMovement) { // special case: keep the player if we want them to move
            camera.spawn();
            minecraft.setCameraEntity(camera);
        }

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

    public static void stopCutsceneImmediate() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.hideGui = hidGuiBefore;
        minecraft.smartCull = true;
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

    @SubscribeEvent
    public static void setCameraPosition(ComputeCameraAngles event) {
        if (isCutsceneRunning) {
            if (camera == null) {
                CutsceneAPI.LOGGER.warn("Found ourselves running a cutscene despite the camera being null. Is this normal?");
                stopCutsceneImmediate();
                return;
            }
            if (runningCutscene == null) {
                CutsceneAPI.LOGGER.error("Attempted to run an invalid cutscene!");
                stopCutsceneImmediate();
                return;
            }
            long currentTime = Minecraft.getInstance().level.getGameTime();
            float partialTick = (float)event.getPartialTick();
            Level level = Minecraft.getInstance().level;
            Minecraft.getInstance().getProfiler().push("cutscene_rotation");
            Vec3 startRot = new Vec3(startCameraYaw, startCameraPitch, startCameraRoll);
            Vec3 initCamRot = new Vec3(initCameraYaw, initCameraPitch, initCameraRoll);

            if (runningCutscene.isTimeForStart()) {
                double progress = runningCutscene.getTime() / (double)runningCutscene.cutscene.startTransition.getLength();
                event.setRoll((float)runningCutscene.cutscene.startTransition.getRot(progress, level, startPosition, startRot, initCamRot, runningCutscene.cutscene).z);
                if (!runningCutscene.cutscene.blockMovement && runningCutscene.cutscene.blockCameraRotation) {
                    // if the player can move but can't rotate, the camera won't update their rotation,
                    // so we do it here
                    event.setPitch(camera.getViewXRot(partialTick));
                    event.setYaw(camera.getViewYRot(partialTick));
                }
            } else if (runningCutscene.isTimeForEnd()) {
                double progress = runningCutscene.getEndProress();
                event.setRoll((float)runningCutscene.cutscene.endTransition.getRot(progress, level, startPosition, startRot, initCamRot, runningCutscene.cutscene).z);
                if (!runningCutscene.cutscene.blockMovement && runningCutscene.cutscene.blockCameraRotation) {
                    event.setPitch(camera.getViewXRot(partialTick));
                    event.setYaw(camera.getViewYRot(partialTick));
                }
            } else if (runningCutscene.cutscene.rotationProvider != null) {
                double progress = runningCutscene.getTime() / runningCutscene.cutscene.length;
                event.setRoll((float)runningCutscene.cutscene.getRotationAt(progress, level, startPosition).z + startCameraRoll);
                if (!runningCutscene.cutscene.blockMovement && runningCutscene.cutscene.blockCameraRotation) {
                    event.setPitch(camera.getViewXRot(partialTick));
                    event.setYaw(camera.getViewYRot(partialTick));
                }
            }
            Minecraft.getInstance().getProfiler().popPush("cutscene_tick");
            runningCutscene.tick(currentTime + partialTick);
            Minecraft.getInstance().getProfiler().pop();
        } else {
            hidGuiBefore = event.getRenderer().getMinecraft().options.hideGui;
            initCameraYaw = event.getYaw();
            initCameraPitch = event.getPitch();
            initCameraRoll = event.getRoll();
        }
    }

    @SubscribeEvent
    public static void onLogout(LoggingOut event) {
        stopCutsceneImmediate();
        previewedCutscene = null;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            Minecraft minecraft = Minecraft.getInstance();
            if (isCutsceneRunning && runningCutscene.cutscene.blockMovement) {
                if (minecraft.player != null && minecraft.player.input instanceof KeyboardInput) {
                    Input input = new Input();
                    input.shiftKeyDown = minecraft.player.input.shiftKeyDown;
                    minecraft.player.input = input;
                }
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
}