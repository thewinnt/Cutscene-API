package net.thewinnt.cutscenes.math;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.util.JsonHelper;

public class BezierCurve implements PathLike {
    private final Vec3 start;
    private final Vec3 control_a;
    private final Vec3 control_b;
    private final Vec3 end;
    private final int weight;
    private double length;
    private final Vec3 cache_t;
    private final Vec3 cache_t2;
    private final Vec3 cache_t3;

    public BezierCurve(Vec3 start, @Nullable Vec3 control_a, @Nullable Vec3 control_b, Vec3 end) {
        this(start, control_a, control_b, end, 1);
    }

    public BezierCurve(Vec3 start, @Nullable Vec3 control_a, @Nullable Vec3 control_b, Vec3 end, int weight) {
        // init values
        this.start = start;
        this.control_a = control_a;
        this.control_b = control_b;
        this.end = end;
        this.weight = weight;
        if (control_a != null && control_b != null) {
            cache_t = start.scale(-3).add(control_a.scale(3));
            cache_t2 = start.scale(3).add(control_a.scale(-6)).add(control_b.scale(3));
            cache_t3 = start.reverse().add(control_a.scale(3)).add(control_b.scale(-3)).add(end);
        } else {
            cache_t = null;
            cache_t2 = null;
            cache_t3 = null;
        }
        double total_length = 0;
        for (int i = 0; i < 201; i++) {
            Vec3 a = this.getPoint(i / 200d);
            Vec3 b = this.getPoint((i + 1) / 200d);
            total_length += a.distanceTo(b);
        }
        this.length = total_length;
    }

    @Override
    public Vec3 getPoint(double t) {
        if (t == 0) return start;
        if (t == 1) return end;
        if (control_a == null && control_b != null) { // only 2nd control point
            return quad(start, control_b, end, t);
        } else if (control_a != null && control_b == null) { // only 1st control point
            return quad(start, control_a, end, t);
        } else if (control_a == null && control_b == null) { // no c
            return start.lerp(end, t);
        }
        return start.add(cache_t.scale(t)).add(cache_t2.scale(t * t)).add(cache_t3.scale(t * t * t));
    }

    private Vec3 quad(Vec3 a, Vec3 b, Vec3 c, double t) {
        Vec3 start = a.lerp(b, t);
        Vec3 end = b.lerp(c, t);
        return start.lerp(end, t);
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public Vec3 getStart() {
        return start;
    }

    @Override
    public Vec3 getEnd() {
        return end;
    }

    @Nullable
    public Vec3 getControlA() {
        return control_a;
    }

    @Nullable
    public Vec3 getControlB() {
        return control_b;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public static BezierCurve fromNetwork(FriendlyByteBuf buf, Path path) {
        Vec3 start = CutsceneNetworkHandler.readVec3(buf);
        Vec3 control_a = CutsceneNetworkHandler.readVec3(buf);
        Vec3 control_b = CutsceneNetworkHandler.readVec3(buf);
        Vec3 end = CutsceneNetworkHandler.readVec3(buf);
        int weight = buf.readInt();
        return new BezierCurve(start, control_a, control_b, end, weight);
    }

    public static BezierCurve fromJSON(JsonObject json, Path path) {
        Vec3 start = JsonHelper.vec3FromJson(json, "start");
        Vec3 control_a = JsonHelper.vec3FromJson(json, "control_a");
        Vec3 control_b = JsonHelper.vec3FromJson(json, "control_b");
        Vec3 end = JsonHelper.vec3FromJson(json, "end");
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new BezierCurve(start, control_a, control_b, end, weight);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writeVec3(buf, start);
        CutsceneNetworkHandler.writeVec3(buf, control_a);
        CutsceneNetworkHandler.writeVec3(buf, control_b);
        CutsceneNetworkHandler.writeVec3(buf, end);
        buf.writeInt(weight);
    }
    
    @Override
    public SegmentSerializer<BezierCurve> getSerializer() {
        return CutsceneManager.BEZIER;
    }
}
