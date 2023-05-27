package net.thewinnt.cutscenes;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

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

    private static Vec3 initPosition;
    public static float initCameraYaw;
    public static float initCameraPitch;
    public static float initCameraRoll;
    private static boolean hidGuiBefore;
    
    @OnlyIn(Dist.CLIENT)
    public static void startCutscene(CutsceneType type, Vec3 startPos, float cameraYaw, float cameraPitch, float cameraRoll, float pathYaw, float pathPitch, float pathRoll) {
        cutsceneStatus = CutsceneStatus.RUNNING;
        startPosition = startPos;
        // if the specified rotation value is NaN, use the initial values
        startCameraYaw = Float.isNaN(cameraYaw) ? initCameraYaw : cameraYaw;
        startCameraPitch = Float.isNaN(cameraPitch) ? initCameraPitch : cameraPitch;
        startCameraRoll = Float.isNaN(cameraRoll) ? initCameraRoll : cameraRoll;
        startPathYaw = pathYaw;
        startPathPitch = pathPitch;
        startPathRoll = pathRoll;
        runningCutscene = type;
        startTime = Minecraft.getInstance().level.getGameTime();
    }

    public static void updateRegistry(Map<ResourceLocation, CutsceneType> registry) {
        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(registry);
    }

    public static void registerCutscene(ResourceLocation id, CutsceneType type) {
        CLIENT_REGISTRY.put(id, type);
    }

    public static void stopCutscene() {
        cutsceneStatus = CutsceneStatus.STOPPING;
        Minecraft.getInstance().options.hideGui = hidGuiBefore;
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

    public static Vector3f getOffset() {
        if (previewOffset != null) {
            return new Vector3f(previewOffset);
        } else {
            return new Vector3f(0, 100, 0);
        }
    }

    @SubscribeEvent
    public static void setCameraPosition(ComputeCameraAngles event) {
        if (cutsceneStatus == CutsceneStatus.RUNNING) {
            if (runningCutscene == null) {
                CutsceneAPI.LOGGER.error("Attempted to run an invalid cutscene!");
                cutsceneStatus = CutsceneStatus.NONE;
                event.getRenderer().getMinecraft().options.hideGui = hidGuiBefore;
                return;
            }
            long currentTime = event.getRenderer().getMinecraft().level.getGameTime();
            if (currentTime - startTime >= runningCutscene.length) {
                cutsceneStatus = CutsceneStatus.STOPPING;
                event.getRenderer().getMinecraft().options.hideGui = hidGuiBefore;
                endCutscene(event);
                return;
            }
            double progress = (currentTime - startTime + event.getPartialTick()) / runningCutscene.length;
            Vec3 targetPosition = runningCutscene.getPathPoint(progress);
            float yRot = (float)Math.toRadians(startPathYaw);
            float zRot = (float)Math.toRadians(startPathPitch);
            float xRot = (float)Math.toRadians(startPathRoll);
            targetPosition = targetPosition.yRot(yRot).zRot(zRot).xRot(xRot);
            targetPosition = startPosition.add(targetPosition);
            Vec3 rotation = runningCutscene.getRotationAt(progress);
            Camera cam = event.getCamera();
            double targetYaw = rotation.x + startCameraYaw;
            double targetPitch = rotation.y + startCameraPitch;
            double targetRoll = rotation.z + startCameraRoll;
            double lerpProgress = (currentTime - startTime + event.getPartialTick()) / 40;
            cam.setPosition(targetPosition);
            event.setYaw((float)targetYaw);
            event.setPitch((float)targetPitch);
            event.setRoll((float)targetRoll);
            if (currentTime - startTime + event.getPartialTick() < 40) {
                if (!startPosition.add(runningCutscene.getPathPoint(0).yRot(yRot).zRot(zRot).xRot(xRot)).equals(initPosition)) {
                    Vec3 target = initPosition.lerp(targetPosition, 1 - Math.pow(1 - lerpProgress, 5));
                    cam.setPosition(target);
                }
                Vec3 startRot = runningCutscene.getRotationAt(0);
                if (initCameraYaw != startRot.x + startCameraYaw) {
                    event.setYaw(Mth.rotLerp((float)(1 - Math.pow(1 - lerpProgress, 5)), initCameraYaw, (float)targetYaw));
                }
                if (initCameraPitch != startRot.y + startCameraPitch) {
                    event.setPitch(Mth.lerp((float)(1 - Math.pow(1 - lerpProgress, 5)), initCameraPitch, (float)targetPitch));
                }
                if (initCameraRoll != startRot.z + startCameraRoll) {
                    event.setRoll(Mth.lerp((float)(1 - Math.pow(1 - lerpProgress, 5)), initCameraRoll, (float)targetRoll));
                }
            }
            // CutsceneAPI.LOGGER.info("Cutscene rotation - {} / {} / {}", event.getYaw(), event.getPitch(), event.getRoll());
            event.getRenderer().getMinecraft().options.hideGui = true;
        } else if (cutsceneStatus == CutsceneStatus.NONE) {
            hidGuiBefore = event.getRenderer().getMinecraft().options.hideGui;
            initPosition = event.getCamera().getPosition();
            initCameraYaw = event.getYaw();
            initCameraPitch = event.getPitch();
            initCameraRoll = event.getRoll();
        } else if (cutsceneStatus == CutsceneStatus.STOPPING) {
            endCutscene(event);
        }
    }

    private static void endCutscene(ComputeCameraAngles event) {
        initPosition = event.getCamera().getPosition();
        initCameraYaw = event.getYaw();
        initCameraPitch = event.getPitch();
        initCameraRoll = event.getRoll();
        long currentTime = event.getRenderer().getMinecraft().level.getGameTime();
        double progress = (currentTime - startTime - runningCutscene.length + event.getPartialTick()) / 40;
        if (progress < 1) {
            float yRot = (float)Math.toRadians(startPathYaw);
            float zRot = (float)Math.toRadians(startPathPitch);
            float xRot = (float)Math.toRadians(startPathRoll);
            Vec3 rotation = runningCutscene.getRotationAt(1);
            double targetYaw = rotation.x + startCameraYaw;
            double targetPitch = rotation.y + startCameraPitch;
            double targetRoll = rotation.z + startCameraRoll;
            if (!startPosition.add(runningCutscene.getPathPoint(1).yRot(yRot).zRot(zRot).xRot(xRot)).equals(initPosition)) {
                Vec3 target = startPosition.add(runningCutscene.getPathPoint(1).yRot(yRot).zRot(zRot).xRot(xRot)).lerp(initPosition, 1 - Math.pow(1 - progress, 5));
                event.getCamera().setPosition(target);
            }
            Vec3 endRot = runningCutscene.getRotationAt(1);
            if (initCameraYaw != endRot.x + startCameraYaw) {
                event.setYaw(Mth.rotLerp((float)(1 - Math.pow(1 - progress, 5)), (float)targetYaw, initCameraYaw));
            }
            if (initCameraPitch != endRot.y + startCameraPitch) {
                event.setPitch(Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), (float)targetPitch, initCameraPitch));
            }
            if (initCameraRoll != endRot.z + startCameraRoll) {
                event.setRoll(Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), (float)targetRoll, initCameraRoll));
            }
        } else {
            cutsceneStatus = CutsceneStatus.NONE;
        }
        // CutsceneAPI.LOGGER.info("Stopping rotation - {} / {} / {}", event.getYaw(), event.getPitch(), event.getRoll());
    }

    public static enum CutsceneStatus {
        NONE,
        RUNNING,
        STOPPING;
    }
}