package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.util.JsonHelper;

public class LineSegment implements PathLike {
    private final PointProvider a;
    private final PointProvider b;
    private final int weight;
    private final EasingFunction easingX;
    private final EasingFunction easingY;
    private final EasingFunction easingZ;
    private final boolean isRotation;

    public LineSegment(PointProvider a, PointProvider b, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, int weight, boolean isRotation) {
        this.a = a;
        this.b = b;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = weight;
        this.isRotation = isRotation;
    }

    public LineSegment(PointProvider a, PointProvider b, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation) {
        this.a = a;
        this.b = b;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = 1;
        this.isRotation = isRotation;
    }

    public LineSegment(PointProvider start, PointProvider end) {
        this(start, end, EasingFunction.LINEAR, EasingFunction.LINEAR, EasingFunction.LINEAR, false);
    }

    public LineSegment(PointProvider start, PointProvider end, boolean isRotation) {
        this(start, end, EasingFunction.LINEAR, EasingFunction.LINEAR, EasingFunction.LINEAR, isRotation);
    }

    public LineSegment(PointProvider start, PointProvider end, EasingFunction easing, boolean isRotation) {
        this(start, end, easing, easing, easing, false);
    }

    public LineSegment(Vec3 a, Vec3 b, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, int weight, boolean isRotation) {
        this.a = new StaticPointProvider(a);
        this.b = new StaticPointProvider(b);
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = weight;
        this.isRotation = isRotation;
    }
    
    public LineSegment(Vec3 a, Vec3 b, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation) {
        this.a = new StaticPointProvider(a);
        this.b = new StaticPointProvider(b);
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
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return a;
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return b;
    }

    @Override
    public Vec3 getPoint(double t, Level l, Vec3 s) {
        double x, y, z;
        if (isRotation) {
            x = Mth.rotLerp((float)easingX.apply(t), (float)a.getPoint(l, s).x, (float)b.getPoint(l, s).x);
            y = Mth.rotLerp((float)easingY.apply(t), (float)a.getPoint(l, s).y, (float)b.getPoint(l, s).y);
            z = Mth.rotLerp((float)easingZ.apply(t), (float)a.getPoint(l, s).z, (float)b.getPoint(l, s).z);
        } else {
            x = Mth.lerp(easingX.apply(t), a.getPoint(l, s).x, b.getPoint(l, s).x);
            y = Mth.lerp(easingY.apply(t), a.getPoint(l, s).y, b.getPoint(l, s).y);
            z = Mth.lerp(easingZ.apply(t), a.getPoint(l, s).z, b.getPoint(l, s).z);
        }
        return new Vec3(x, y, z);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public static LineSegment fromNetwork(FriendlyByteBuf buf, Path path) {
        PointProvider start = CutsceneNetworkHandler.readPointProvider(buf);
        PointProvider end = CutsceneNetworkHandler.readPointProvider(buf);
        EasingFunction easingX = buf.readEnum(EasingFunction.class);
        EasingFunction easingY = buf.readEnum(EasingFunction.class);
        EasingFunction easingZ = buf.readEnum(EasingFunction.class);
        boolean isRotation = buf.readBoolean();
        int weight = buf.readInt();
        return new LineSegment(start, end, easingX, easingY, easingZ, weight, isRotation);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writePointProvider(buf, a);
        CutsceneNetworkHandler.writePointProvider(buf, b);
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
        PointProvider start = JsonHelper.pointFromJson(json, "start");
        PointProvider end = JsonHelper.pointFromJson(json, "end");
        EasingFunction easingX = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_x", "linear").toUpperCase());
        EasingFunction easingY = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_y", "linear").toUpperCase());
        EasingFunction easingZ = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_z", "linear").toUpperCase());
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        boolean isRotation = GsonHelper.getAsBoolean(json, "is_rotation", false);
        return new LineSegment(start, end, easingX, easingY, easingZ, weight, isRotation);
    }
}
