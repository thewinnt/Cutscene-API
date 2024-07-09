package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

public class SmoothEaseTransition implements Transition {
    private final int length;
    private final boolean countTowardsCutsceneTime;
    private final boolean isStart;
    private final Easing easingX;
    private final Easing easingY;
    private final Easing easingZ;
    private final Easing easingRotX;
    private final Easing easingRotY;
    private final Easing easingRotZ;

    public SmoothEaseTransition(int length, boolean countTowardsCutsceneTime, boolean isStart) {
        this(length, countTowardsCutsceneTime, isStart, SimpleEasing.OUT_QUINT, SimpleEasing.OUT_QUINT, SimpleEasing.OUT_QUINT, SimpleEasing.OUT_QUINT, SimpleEasing.OUT_QUINT, SimpleEasing.OUT_QUINT);
    }

    public SmoothEaseTransition(int length, boolean countTowardsCutsceneTime, boolean isStart, Easing easingX,
            Easing easingY, Easing easingZ, Easing easingRotX, Easing easingRotY,
            Easing easingRotZ) {
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
        Vec3 point = cutscene.getPathPoint(cutsceneProgress, level, startPos);
        if (point == null) {
            point = startPos;
        } else {
            point = point.yRot((float) pathRot.y).zRot((float) pathRot.z).xRot((float) pathRot.x).add(startPos);
        }
        if (isStart) {
            return new Vec3(
                Mth.lerp(easingX.get(progress), initCamPos.x, point.x),
                Mth.lerp(easingY.get(progress), initCamPos.y, point.y),
                Mth.lerp(easingZ.get(progress), initCamPos.z, point.z)
            );
        } else {
            return new Vec3(
                Mth.lerp(easingX.get(progress), point.x, initCamPos.x),
                Mth.lerp(easingY.get(progress), point.y, initCamPos.y),
                Mth.lerp(easingZ.get(progress), point.z, initCamPos.z)
            );
        }
    }

    @Override
    public Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene) {
        double cutsceneProgress = progress * this.length / cutscene.length;
        Vec3 rotation = cutscene.getRotationAt(cutsceneProgress, level, startPos);
        if (rotation == null) {
            rotation = initCamRot;
        }
        double targetYaw = rotation.x + startRot.x;
        double targetPitch = rotation.y + startRot.y;
        double targetRoll = rotation.z + startRot.z;
        double x = initCamRot.x;
        double y = initCamRot.y;
        double z = initCamRot.z;
        if (countTowardsCutsceneTime && isStart) {
            Vec3 rot0 = cutscene.getRotationAt(0, level, startPos);
            if (rot0 == null) {
                rot0 = startRot;
            }
            if (initCamRot.x != rot0.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.get(progress)), (float)initCamRot.x, (float)targetYaw);
            }
            if (initCamRot.y != rot0.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.get(progress)), initCamRot.y, (float)targetPitch);
            }
            if (initCamRot.z != rot0.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.get(progress)), initCamRot.z, (float)targetRoll);
            }
            return new Vec3(x, y, z);
        } else if (countTowardsCutsceneTime && !isStart) {
            Vec3 rot1 = cutscene.getRotationAt(1, level, startPos);
            if (rot1 == null) {
                rot1 = startRot;
            }
            if (initCamRot.x != rot1.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.get(progress)), (float)targetYaw, (float)initCamRot.x);
            }
            if (initCamRot.y != rot1.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.get(progress)), (float)targetPitch, initCamRot.y);
            }
            if (initCamRot.z != rot1.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.get(progress)), (float)targetRoll, initCamRot.z);
            }
            return new Vec3(x, y, z);
        } else if (!countTowardsCutsceneTime && isStart) {
            Vec3 rot0 = cutscene.getRotationAt(0, level, startPos);
            if (rot0 == null) {
                rot0 = startRot;
            }
            if (initCamRot.x != rot0.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.get(progress)), (float)initCamRot.x, (float)rot0.x);
            }
            if (initCamRot.y != rot0.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.get(progress)), initCamRot.y, (float)rot0.y);
            }
            if (initCamRot.z != rot0.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.get(progress)), initCamRot.z, (float)rot0.z);
            }
            return new Vec3(x, y, z);
        } else {
            Vec3 rot1 = cutscene.getRotationAt(1, level, startPos);
            if (rot1 == null) {
                rot1 = startRot;
            }
            if (initCamRot.x != rot1.x + startRot.x) {
                x = Mth.rotLerp((float)(easingRotX.get(progress)), (float)rot1.x, (float)initCamRot.x);
            }
            if (initCamRot.y != rot1.y + startRot.y) {
                y = Mth.lerp((float)(easingRotY.get(progress)), (float)rot1.y, initCamRot.y);
            }
            if (initCamRot.z != rot1.z + startRot.z) {
                z = Mth.lerp((float)(easingRotZ.get(progress)), (float)rot1.z, initCamRot.z);
            }
            return new Vec3(x, y, z);
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(length);
        buf.writeBoolean(countTowardsCutsceneTime);
        buf.writeBoolean(isStart);
        Easing.toNetwork(easingX, buf);
        Easing.toNetwork(easingY, buf);
        Easing.toNetwork(easingZ, buf);
        Easing.toNetwork(easingRotX, buf);
        Easing.toNetwork(easingRotY, buf);
        Easing.toNetwork(easingRotZ, buf);
    }

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.SMOOTH_EASE;
    }

    public static SmoothEaseTransition fromNetwork(FriendlyByteBuf buf) {
        int length = buf.readInt();
        boolean countTowardsCutsceneTime = buf.readBoolean();
        boolean easeIn = buf.readBoolean();
        Easing easingX = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingY = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingZ = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingRotX = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingRotY = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingRotZ = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        return new SmoothEaseTransition(length, countTowardsCutsceneTime, easeIn, easingX, easingY, easingZ, easingRotX, easingRotY, easingRotZ);
    }

    public static SmoothEaseTransition fromJSON(JsonObject json) {
        int length = GsonHelper.getAsInt(json, "length", 40);
        boolean isStart = GsonHelper.getAsBoolean(json, "is_start");
        boolean countTowardsCutsceneTime = GsonHelper.getAsBoolean(json, "count_towards_cutscene_time", isStart);
        Easing easingX = Easing.fromJSON(json.get("easing_x"), SimpleEasing.OUT_QUINT);
        Easing easingY = Easing.fromJSON(json.get("easing_y"), SimpleEasing.OUT_QUINT);
        Easing easingZ = Easing.fromJSON(json.get("easing_z"), SimpleEasing.OUT_QUINT);
        Easing easingRotX = Easing.fromJSON(json.get("easing_rot_x"), SimpleEasing.OUT_QUINT);
        Easing easingRotY = Easing.fromJSON(json.get("easing_rot_y"), SimpleEasing.OUT_QUINT);
        Easing easingRotZ = Easing.fromJSON(json.get("easing_rot_z"), SimpleEasing.OUT_QUINT);
        return new SmoothEaseTransition(length, countTowardsCutsceneTime, isStart, easingX, easingY, easingZ, easingRotX, easingRotY, easingRotZ);
    }
}
