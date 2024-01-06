package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;

public class SmoothEaseTransition implements Transition {
    private final int length;
    private final boolean countTowardsCutsceneTime;
    private final boolean easeIn;

    public SmoothEaseTransition(int length, boolean countTowardsCutsceneTime, boolean easeIn) {
        this.length = length;
        this.countTowardsCutsceneTime = countTowardsCutsceneTime;
        this.easeIn = easeIn;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean countTowardsCutsceneTime() {
        return countTowardsCutsceneTime;
    }

    @Override
    public Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene) {
        double cutsceneProgress;
        if (countTowardsCutsceneTime) {
            cutsceneProgress = progress * this.length / cutscene.length;
        } else {
            cutsceneProgress = easeIn ? 0 : 1;
        }
        Vec3 point = cutscene.getPathPoint(cutsceneProgress, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
        if (easeIn) {
            return initCamPos.lerp(point, 1 - Math.pow(1 - progress, 5));
        } else {
            return point.lerp(initCamPos, 1 - Math.pow(1 - progress, 5));
        }
    }

    @Override
    public Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene) {
        double cutsceneProgress = progress * this.length / cutscene.length;
        Vec3 rotation = cutscene.getRotationAt(cutsceneProgress, level, startPos);
        double targetYaw = rotation.x + startRot.x;
        double targetPitch = rotation.y + startRot.y;
        double targetRoll = rotation.z + startRot.z;
        double x = initCamRot.x;
        double y = initCamRot.y;
        double z = initCamRot.z;
        if (countTowardsCutsceneTime && easeIn) {
            Vec3 rot0 = cutscene.getRotationAt(0, level, startPos);
            if (initCamRot.x != rot0.x + startRot.x) {
                x = Mth.rotLerp((float)(1 - Math.pow(1 - progress, 5)), (float)initCamRot.x, (float)targetYaw);
            }
            if (initCamRot.y != rot0.y + startRot.y) {
                y = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), initCamRot.y, (float)targetPitch);
            }
            if (initCamRot.z != rot0.z + startRot.z) {
                z = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), initCamRot.z, (float)targetRoll);
            }
            return new Vec3(x, y, z);
        } else if (countTowardsCutsceneTime && !easeIn) {
            Vec3 rot1 = cutscene.getRotationAt(1, level, startPos);
            if (initCamRot.x != rot1.x + startRot.x) {
                x = Mth.rotLerp((float)(1 - Math.pow(1 - progress, 5)), (float)targetYaw, (float)initCamRot.x);
            }
            if (initCamRot.y != rot1.y + startRot.y) {
                y = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), (float)targetPitch, initCamRot.y);
            }
            if (initCamRot.z != rot1.z + startRot.z) {
                z = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), (float)targetRoll, initCamRot.z);
            }
            return new Vec3(x, y, z);
        } else if (!countTowardsCutsceneTime && easeIn) {
            Vec3 rot0 = cutscene.getRotationAt(0, level, startPos);
            if (initCamRot.x != rot0.x + startRot.x) {
                x = Mth.rotLerp((float)(1 - Math.pow(1 - progress, 5)), (float)initCamRot.x, (float)rot0.x);
            }
            if (initCamRot.y != rot0.y + startRot.y) {
                y = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), initCamRot.y, (float)rot0.y);
            }
            if (initCamRot.z != rot0.z + startRot.z) {
                z = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), initCamRot.z, (float)rot0.z);
            }
            return new Vec3(x, y, z);
        } else {
            Vec3 rot1 = cutscene.getRotationAt(1, level, startPos);
            if (initCamRot.x != rot1.x + startRot.x) {
                x = Mth.rotLerp((float)(1 - Math.pow(1 - progress, 5)), (float)rot1.x, (float)initCamRot.x);
            }
            if (initCamRot.y != rot1.y + startRot.y) {
                y = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), (float)rot1.y, initCamRot.y);
            }
            if (initCamRot.z != rot1.z + startRot.z) {
                z = Mth.lerp((float)(1 - Math.pow(1 - progress, 5)), (float)rot1.z, initCamRot.z);
            }
            return new Vec3(x, y, z);
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(length);
        buf.writeBoolean(countTowardsCutsceneTime);
        buf.writeBoolean(easeIn);
    }

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.SMOOTH_EASE;
    }

    public static SmoothEaseTransition fromNetwork(FriendlyByteBuf buf) {
        int length = buf.readInt();
        boolean countTowardsCutsceneTime = buf.readBoolean();
        boolean easeIn = buf.readBoolean();
        return new SmoothEaseTransition(length, countTowardsCutsceneTime, easeIn);
    }

    public static SmoothEaseTransition fromJSON(JsonObject json) {
        int length = GsonHelper.getAsInt(json, "length", 40);
        boolean countTowardsCutsceneTime = GsonHelper.getAsBoolean(json, "count_towards_cutscene_time", true);
        boolean easeIn = GsonHelper.getAsBoolean(json, "ease_in");
        return new SmoothEaseTransition(length, countTowardsCutsceneTime, easeIn);
    }
}
