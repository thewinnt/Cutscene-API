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
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;

import java.util.Map;

@SuppressWarnings("resource")
@Mod.EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientCutsceneManager { 
    public static final BiMap<ResourceLocation, CutsceneType> CLIENT_REGISTRY = HashBiMap.create();
    public static CutsceneStatus cutsceneStatus = CutsceneStatus.NONE;
    public static CutsceneType runningCutscene;
    public static long startTime;
    private static Vec3 startPosition;
    public static float startCameraYaw; // x
    public static float startCameraPitch; // y
    public static float startCameraRoll; // z
    public static float startPathYaw;
    public static float startPathPitch;
    public static float startPathRoll;

    public static CutsceneType previewedCutscene = null;
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
        // if the specified rotation value is NaN, use the initial values
        startCameraYaw = Float.isNaN(cameraYaw) ? initCameraYaw : cameraYaw;
        startCameraPitch = Float.isNaN(cameraPitch) ? initCameraPitch : cameraPitch;
        startCameraRoll = Float.isNaN(cameraRoll) ? initCameraRoll : cameraRoll;
        startPathYaw = pathYaw;
        startPathPitch = pathPitch;
        startPathRoll = pathRoll;
        runningCutscene = type;
        startTime = Minecraft.getInstance().level.getGameTime();

        // initialize minecraft
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.smartCull = false;
        minecraft.gameRenderer.setRenderHand(false);
        minecraft.gameRenderer.setRenderBlockOutline(false);
        prevF5state = minecraft.options.getCameraType();
        if (minecraft.gameRenderer.getMainCamera().isDetached()) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
        camera = new CutsceneCameraEntity(-69420, type, startPos, startCameraYaw, startCameraPitch, pathYaw, pathPitch, pathRoll);
        camera.spawn();
        minecraft.setCameraEntity(camera);

        cutsceneStatus = CutsceneStatus.RUNNING;
        startPosition = startPos;
    }

    public static void updateRegistry(Map<ResourceLocation, CutsceneType> registry) {
        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(registry);
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
            if (camera.isTimeForStart(minecraft.getPartialTick())) {
                runningCutscene.startTransition.onEnd(runningCutscene);
            }
            if (camera.isTimeForEnd(minecraft.getPartialTick())) {
                runningCutscene.endTransition.onEnd(runningCutscene);
            }
        }
        camera = null;
        if (minecraft.player != null) {
            minecraft.player.input = new KeyboardInput(minecraft.options);
        }
        runningCutscene = null;
    }

    public static void setPreviewedCutscene(CutsceneType preview, Vec3 offset, float pathYaw, float pathPitch, float pathRoll) {
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
        if (cutsceneStatus == CutsceneStatus.RUNNING) {
            if (camera == null) {
                cutsceneStatus = CutsceneStatus.NONE;
                return;
            }
            if (runningCutscene == null) {
                CutsceneAPI.LOGGER.error("Attempted to run an invalid runningCutscene!");
                stopCutsceneImmediate();
                return;
            }
            long currentTime = Minecraft.getInstance().level.getGameTime();
            Level level = Minecraft.getInstance().level;
            float partialTick = (float)event.getPartialTick();
            Vec3 startRot = new Vec3(startCameraYaw, startCameraPitch, startCameraRoll);
            Vec3 initCamRot = new Vec3(initCameraYaw, initCameraPitch, initCameraRoll);
            if (camera.isTimeForStart(partialTick)) {
                double progress = (currentTime + partialTick - startTime) / (double)runningCutscene.startTransition.getLength();
                event.setRoll((float)runningCutscene.startTransition.getRot(progress, level, startPosition, startRot, initCamRot, runningCutscene).z);
            } else if (camera.isTimeForEnd(partialTick)) {
                double progress = camera.getEndProress(partialTick);
                event.setRoll((float)runningCutscene.endTransition.getRot(progress, level, startPosition, startRot, initCamRot, runningCutscene).z);
            } else {
                double progress = (currentTime - startTime + partialTick) / (double)runningCutscene.length;
                event.setRoll((float)runningCutscene.getRotationAt(progress, level, startPosition).z + startCameraRoll);
            }
        } else if (cutsceneStatus == CutsceneStatus.NONE) {
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
            if (cutsceneStatus != CutsceneStatus.NONE) {
                if (minecraft.player != null && minecraft.player.input instanceof KeyboardInput) {
                    Input input = new Input();
                    input.shiftKeyDown = minecraft.player.input.shiftKeyDown;
                    minecraft.player.input = input;
                }
            }
        }
    }

    public static enum CutsceneStatus {
        NONE,
        RUNNING,
        STOPPING;
    }
}