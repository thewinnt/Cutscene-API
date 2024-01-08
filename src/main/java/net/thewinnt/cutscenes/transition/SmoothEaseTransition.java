package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.path.EasingFunction;

public class SmoothEaseTransition implements Transition {
    private final int length;
    private final boolean countTowardsCutsceneTime;
    private final boolean isStart;
    private final EasingFunction easingX;
    private final EasingFunction easingY;
    private final EasingFunction easingZ;
    private final EasingFunction easingRotX;
    private final EasingFunction easingRotY;
    private final EasingFunction easingRotZ;

    public SmoothEaseTransition(int length, boolean countTowardsCutsceneTime, boolean isStart) {
        this(length, countTowardsCutsceneTime, isStart, EasingFunction.OUT_QUINT, EasingFunction.OUT_QUINT, EasingFunction.OUT_QUINT, EasingFunction.OUT_QUINT, EasingFunction.OUT_QUINT, EasingFunction.OUT_QUINT);
    }

    public SmoothEaseTransition(int length, boolean countTowardsCutsceneTime, boolean isStart, EasingFunction easingX,
            EasingFunction easingY, EasingFunction easingZ, EasingFunction easingRotX, EasingFunction easingRotY,
            EasingFunction easingRotZ) {
        this.length = length;
        this.countTowardsCutsceneTime = countTowardsCutsceneTime;
        this.isStart = isStart;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.easingRotX = easingRotX;
        this.easingRotY = easingRotY;
        this.easingRotZ = easingRotZ;
    }

    @Override
    public int getLength() {
        return length;
    }
    
    @Override
    public int getOffCutsceneTime() {
        return countTowardsCutsceneTime ? 0 : length;
    }

    @Override
    public int getOnCutsceneTime() {
        return countTowardsCutsceneTime ? length : 0;
    }

    @Override
    public Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene) {
        double cutsceneProgress;
        if (countTowardsCutsceneTime) {
            cutsceneProgress = progress * this.length / cutscene.length;
        } else {
            cutsceneProgress = isStart ? 0 : 1;
        }
        Vec3 point = cutscene.getPathPoint(cutsceneProgress, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
        if (isStart) {
            return new Vec3(
                Mth.lerp(easingX.apply(progress), initCamPos.x, point.x),
                Mth.lerp(easingY.apply(progress), initCamPos.y, point.y),
                Mth.lerp(easingZ.apply(progress), initCamPos.z, point.z)
            );
        } else {
            return new Vec3(
                Mth.lerp(easingX.apply(progress), point.x, initCamPos.x),
                Mth.lerp(easingY.apply(progress), point.y, initCamPos.y),
                Mth.lerp(easingZ.apply(progress), point.z, initCamPos.z)
            );
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
        if (countTowardsCutsceneTime && isStart) {
            Vec3 rot0 = cutscene.getRotationAt(0, level, startPos);
            if (initCamRot.x != rot0.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.apply(progress)), (float)initCamRot.x, (float)targetYaw);
            }
            if (initCamRot.y != rot0.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.apply(progress)), initCamRot.y, (float)targetPitch);
            }
            if (initCamRot.z != rot0.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.apply(progress)), initCamRot.z, (float)targetRoll);
            }
            return new Vec3(x, y, z);
        } else if (countTowardsCutsceneTime && !isStart) {
            Vec3 rot1 = cutscene.getRotationAt(1, level, startPos);
            if (initCamRot.x != rot1.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.apply(progress)), (float)targetYaw, (float)initCamRot.x);
            }
            if (initCamRot.y != rot1.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.apply(progress)), (float)targetPitch, initCamRot.y);
            }
            if (initCamRot.z != rot1.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.apply(progress)), (float)targetRoll, initCamRot.z);
            }
            return new Vec3(x, y, z);
        } else if (!countTowardsCutsceneTime && isStart) {
            Vec3 rot0 = cutscene.getRotationAt(0, level, startPos);
            if (initCamRot.x != rot0.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.apply(progress)), (float)initCamRot.x, (float)rot0.x);
            }
            if (initCamRot.y != rot0.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.apply(progress)), initCamRot.y, (float)rot0.y);
            }
            if (initCamRot.z != rot0.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.apply(progress)), initCamRot.z, (float)rot0.z);
            }
            return new Vec3(x, y, z);
        } else {
            Vec3 rot1 = cutscene.getRotationAt(1, level, startPos);
            if (initCamRot.x != rot1.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.apply(progress)), (float)rot1.x, (float)initCamRot.x);
            }
            if (initCamRot.y != rot1.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.apply(progress)), (float)rot1.y, initCamRot.y);
            }
            if (initCamRot.z != rot1.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.apply(progress)), (float)rot1.z, initCamRot.z);
            }
            return new Vec3(x, y, z);
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(length);
        buf.writeBoolean(countTowardsCutsceneTime);
        buf.writeBoolean(isStart);
        buf.writeEnum(easingX);
        buf.writeEnum(easingY);
        buf.writeEnum(easingZ);
        buf.writeEnum(easingRotX);
        buf.writeEnum(easingRotY);
        buf.writeEnum(easingRotZ);
    }

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.SMOOTH_EASE;
    }

    public static SmoothEaseTransition fromNetwork(FriendlyByteBuf buf) {
        int length = buf.readInt();
        boolean countTowardsCutsceneTime = buf.readBoolean();
        boolean easeIn = buf.readBoolean();
        EasingFunction easingX = buf.readEnum(EasingFunction.class);
        EasingFunction easingY = buf.readEnum(EasingFunction.class);
        EasingFunction easingZ = buf.readEnum(EasingFunction.class);
        EasingFunction easingRotX = buf.readEnum(EasingFunction.class);
        EasingFunction easingRotY = buf.readEnum(EasingFunction.class);
        EasingFunction easingRotZ = buf.readEnum(EasingFunction.class);
        return new SmoothEaseTransition(length, countTowardsCutsceneTime, easeIn, easingX, easingY, easingZ, easingRotX, easingRotY, easingRotZ);
    }

    public static SmoothEaseTransition fromJSON(JsonObject json) {
        int length = GsonHelper.getAsInt(json, "length", 40);
        boolean isStart = GsonHelper.getAsBoolean(json, "is_start");
        boolean countTowardsCutsceneTime = GsonHelper.getAsBoolean(json, "count_towards_cutscene_time", isStart);
        EasingFunction easingX = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_x", "out_quint").toUpperCase());
        EasingFunction easingY = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_y", "out_quint").toUpperCase());
        EasingFunction easingZ = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_z", "out_quint").toUpperCase());
        EasingFunction easingRotX = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_rot_x", "out_quint").toUpperCase());
        EasingFunction easingRotY = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_rot_y", "out_quint").toUpperCase());
        EasingFunction easingRotZ = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_rot_z", "out_quint").toUpperCase());
        return new SmoothEaseTransition(length, countTowardsCutsceneTime, isStart, easingX, easingY, easingZ, easingRotX, easingRotY, easingRotZ);
    }
}
