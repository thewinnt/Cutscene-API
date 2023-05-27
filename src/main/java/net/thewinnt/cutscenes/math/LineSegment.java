package net.thewinnt.cutscenes.math;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.util.JsonHelper;

public class LineSegment implements PathLike {
    private final Vec3 a;
    private final Vec3 b;
    private final int weight;
    private final EasingFunction easingX;
    private final EasingFunction easingY;
    private final EasingFunction easingZ;
    private final boolean isRotation;

    public LineSegment(Vec3 a, Vec3 b, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, int weight, boolean isRotation) {
        this.a = a;
        this.b = b;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = weight;
        this.isRotation = isRotation;
    }

    public LineSegment(Vec3 a, Vec3 b, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation) {
        this.a = a;
        this.b = b;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = 1;
        this.isRotation = isRotation;
    }

    public LineSegment(Vec3 start, Vec3 end) {
        this(start, end, EasingFunction.LINEAR, EasingFunction.LINEAR, EasingFunction.LINEAR, false);
    }

    public LineSegment(Vec3 start, Vec3 end, boolean isRotation) {
        this(start, end, EasingFunction.LINEAR, EasingFunction.LINEAR, EasingFunction.LINEAR, isRotation);
    }

    public LineSegment(Vec3 start, Vec3 end, EasingFunction easing, boolean isRotation) {
        this(start, end, easing, easing, easing, false);
    }

    @Override
    public Vec3 getStart() {
        return a;
    }

    @Override
    public Vec3 getEnd() {
        return b;
    }

    @Override
    public double getLength() {
        return a.distanceTo(b); // TODO correct length calculation
    }

    @Override
    public Vec3 getPoint(double t) {
        double x, y, z;
        if (isRotation) {
            x = Mth.rotLerp((float)easingX.apply(t), (float)a.x, (float)b.x);
            y = Mth.rotLerp((float)easingY.apply(t), (float)a.y, (float)b.y);
            z = Mth.rotLerp((float)easingZ.apply(t), (float)a.z, (float)b.z);
        } else {
            x = Mth.lerp(easingX.apply(t), a.x, b.x);
            y = Mth.lerp(easingY.apply(t), a.y, b.y);
            z = Mth.lerp(easingZ.apply(t), a.z, b.z);
        }
        return new Vec3(x, y, z);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public static LineSegment fromNetwork(FriendlyByteBuf buf, Path path) {
        Vec3 start = CutsceneNetworkHandler.readVec3(buf);
        Vec3 end = CutsceneNetworkHandler.readVec3(buf);
        EasingFunction easingX = buf.readEnum(EasingFunction.class);
        EasingFunction easingY = buf.readEnum(EasingFunction.class);
        EasingFunction easingZ = buf.readEnum(EasingFunction.class);
        boolean isRotation = buf.readBoolean();
        int weight = buf.readInt();
        return new LineSegment(start, end, easingX, easingY, easingZ, weight, isRotation);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writeVec3(buf, a);
        CutsceneNetworkHandler.writeVec3(buf, b);
        buf.writeEnum(easingX);
        buf.writeEnum(easingY);
        buf.writeEnum(easingZ);
        buf.writeBoolean(isRotation);
        buf.writeInt(weight);
    }
    
    @Override
    public SegmentSerializer<LineSegment> getSerializer() {
        return CutsceneManager.LINE;
    }

    public static LineSegment fromJSON(JsonObject json, Path path) {
        Vec3 start = JsonHelper.vec3FromJson(json, "start");
        Vec3 end = JsonHelper.vec3FromJson(json, "end");
        EasingFunction easingX = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_x", "linear").toUpperCase());
        EasingFunction easingY = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_y", "linear").toUpperCase());
        EasingFunction easingZ = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_z", "linear").toUpperCase());
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        boolean isRotation = GsonHelper.getAsBoolean(json, "is_rotation", false);
        return new LineSegment(start, end, easingX, easingY, easingZ, weight, isRotation);
    }
}
