package net.thewinnt.cutscenes.path;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.client.preview.PathPreviewRenderer.Line;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.util.JsonHelper;

public class BezierCurve implements PathLike {
    private final PointProvider start;
    private final PointProvider control_a;
    private final PointProvider control_b;
    private final PointProvider end;
    private final int weight;
    private final Vec3 cache_t;
    private final Vec3 cache_t2;
    private final Vec3 cache_t3;

    public BezierCurve(Vec3 start, @Nullable Vec3 control_a, @Nullable Vec3 control_b, Vec3 end) {
        this(start, control_a, control_b, end, 1);
    }

    public BezierCurve(PointProvider start, @Nullable PointProvider control_a, @Nullable PointProvider control_b, PointProvider end, int weight) {
        this.start = start;
        this.control_a = control_a;
        this.control_b = control_b;
        this.end = end;
        this.weight = weight;
        cache_t = null;
        cache_t2 = null;
        cache_t3 = null;
    }

    public BezierCurve(Vec3 start, @Nullable Vec3 control_a, @Nullable Vec3 control_b, Vec3 end, int weight) {
        // init values
        this.start = new StaticPointProvider(start);
        this.control_a = control_a == null ? null : new StaticPointProvider(control_a);
        this.control_b = control_b == null ? null : new StaticPointProvider(control_b);
        this.end = new StaticPointProvider(end);
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
    }

    @Override
    public Vec3 getPoint(double t, Level l, Vec3 s) {
        if (t == 0) return PointProvider.getPoint(start, l, s);
        if (t == 1) return PointProvider.getPoint(end, l, s);
        if (control_a == null && control_b != null) { // only 2nd control point
            return quad(PointProvider.getPoint(start, l, s), PointProvider.getPoint(control_b, l, s), PointProvider.getPoint(end, l, s), t);
        } else if (control_a != null && control_b == null) { // only 1st control point
            return quad(PointProvider.getPoint(start, l, s), PointProvider.getPoint(control_a, l, s), PointProvider.getPoint(end, l, s), t);
        } else if (control_a == null) { // no control points
            return PointProvider.getPoint(start, l, s).lerp(PointProvider.getPoint(end, l, s), t);
        }
        if (cache_t != null && cache_t2 != null && cache_t3 != null) {
            return PointProvider.getPoint(start, l, s).add(cache_t.scale(t)).add(cache_t2.scale(t * t)).add(cache_t3.scale(t * t * t));
        } else {
            Vec3 _t = PointProvider.getPoint(start, l, s).scale(-3).add(PointProvider.getPoint(control_a, l, s).scale(3));
            Vec3 _t2 = PointProvider.getPoint(start, l, s).scale(3).add(PointProvider.getPoint(control_a, l, s).scale(-6)).add(PointProvider.getPoint(control_b, l, s).scale(3));
            Vec3 _t3 = PointProvider.getPoint(start, l, s).reverse().add(PointProvider.getPoint(control_a, l, s).scale(3)).add(PointProvider.getPoint(control_b, l, s).scale(-3)).add(PointProvider.getPoint(end, l, s));
            return PointProvider.getPoint(start, l, s).add(_t.scale(t)).add(_t2.scale(t * t)).add(_t3.scale(t * t * t));
        }
    }

    private Vec3 quad(Vec3 a, Vec3 b, Vec3 c, double t) {
        Vec3 start = a.lerp(b, t);
        Vec3 end = b.lerp(c, t);
        return start.lerp(end, t);
    }

    @Override
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return start;
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return end;
    }

    @Nullable
    public PointProvider getControlA() {
        return control_a;
    }

    @Nullable
    public PointProvider getControlB() {
        return control_b;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public Collection<Line> getUtilityPoints(Level level, Vec3 cutsceneStart, int initLevel) {
        if (control_a == null && control_b == null) {
            return List.of();
        } else if (control_a != null && control_b == null) {
            return List.of(
                new Line(start, control_a, initLevel),
                new Line(control_a, end, initLevel)
            );
        } else if (control_a == null && control_b != null) {
            return List.of(
                new Line(start, control_b, initLevel),
                new Line(control_b, end, initLevel)
            );
        } else {
            return List.of(
                new Line(start, control_a, initLevel),
                new Line(control_a, control_b, initLevel),
                new Line(control_b, end, initLevel)
            );
        }
    }

    @Override
    public void getAllPoints(Stream.Builder<PointProvider> builder) {
        builder.accept(start);
        builder.accept(control_a);
        builder.accept(control_b);
        builder.accept(end);
    }

    public static BezierCurve fromNetwork(FriendlyByteBuf buf, Path path) {
        PointProvider start = CutsceneNetworkHandler.readPointProvider(buf);
        PointProvider control_a = CutsceneNetworkHandler.readPointProvider(buf);
        PointProvider control_b = CutsceneNetworkHandler.readPointProvider(buf);
        PointProvider end = CutsceneNetworkHandler.readPointProvider(buf);
        int weight = buf.readInt();
        return new BezierCurve(start, control_a, control_b, end, weight);
    }

    public static BezierCurve fromJSON(JsonObject json, Path path) {
        PointProvider start = JsonHelper.pointFromJson(json, "start");
        PointProvider control_a = JsonHelper.pointFromJson(json, "control_a");
        PointProvider control_b = JsonHelper.pointFromJson(json, "control_b");
        PointProvider end = JsonHelper.pointFromJson(json, "end");
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new BezierCurve(start, control_a, control_b, end, weight);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writePointProvider(buf, start);
        CutsceneNetworkHandler.writePointProvider(buf, control_a);
        CutsceneNetworkHandler.writePointProvider(buf, control_b);
        CutsceneNetworkHandler.writePointProvider(buf, end);
        buf.writeInt(weight);
    }
    
    @Override
    public SegmentSerializer<BezierCurve> getSerializer() {
        return CutsceneManager.BEZIER;
    }
}
