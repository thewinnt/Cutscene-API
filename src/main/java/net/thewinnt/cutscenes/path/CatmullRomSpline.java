package net.thewinnt.cutscenes.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

public class CatmullRomSpline implements PathLike {
    private PointProvider start;
    private final ArrayList<PointProvider> points = new ArrayList<>();
    private PointProvider end;
    private final int weight;
    private final boolean staticEndPoints;

    public CatmullRomSpline(Vec3... points) {
        this(1, points);
    }

    public CatmullRomSpline(int weight, Vec3... points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("A Catmull-Rom spline must have at least 2 points");
        }
        for (Vec3 i : points) {
            this.points.add(new StaticPointProvider(i));
        }
        this.start = new StaticPointProvider(points[1].lerp(points[0], 2));
        this.end = new StaticPointProvider(points[points.length - 2].lerp(points[points.length - 1], 2));
        this.staticEndPoints = true;
        this.weight = weight;
    }

    public CatmullRomSpline(int weight, PointProvider... points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("A Catmull-Rom spline must have at least 2 points");
        }
        this.points.addAll(List.of(points));
        this.staticEndPoints = false;
        this.weight = weight;
    }

    @Override
    public Vec3 getPoint(double t, Level l, Vec3 s) {
        int startSegment = (int)((this.points.size() - 1) * t);
        if (!staticEndPoints) {
            this.start = new StaticPointProvider(points.get(1).getPoint(l, s).lerp(points.get(0).getPoint(l, s), 2));
            this.end = new StaticPointProvider(points.get(points.size() - 2).getPoint(l, s).lerp(points.get(points.size() - 1).getPoint(l, s), 2));
        }
        Vec3 a, b, c, d;
        if (t <= 0) return this.points.get(0).getPoint(l, s);
        if (t >= 1) return this.points.get(this.points.size() - 1).getPoint(l, s);
        if (this.points.size() == 2) {
            a = start.getPoint(l, s);
            b = points.get(0).getPoint(l, s);
            c = points.get(1).getPoint(l, s);
            d = end.getPoint(l, s);
        } else if (startSegment == 0) {
            a = start.getPoint(l, s);
            b = points.get(0).getPoint(l, s);
            c = points.get(1).getPoint(l, s);
            d = points.get(2).getPoint(l, s);
        } else if (startSegment == points.size() - 2) {
            a = points.get(startSegment - 1).getPoint(l, s);
            b = points.get(startSegment).getPoint(l, s);
            c = points.get(startSegment + 1).getPoint(l, s);
            d = end.getPoint(l, s);
        } else {
            a = points.get(startSegment - 1).getPoint(l, s);
            b = points.get(startSegment).getPoint(l, s);
            c = points.get(startSegment + 1).getPoint(l, s);
            d = points.get(startSegment + 2).getPoint(l, s);
        }
        double step = 1d / (this.points.size() - 1);
        t -= startSegment * step;
        t /= step;
        return b.scale(2)
                .add(a.reverse().add(c).scale(t))
                .add(a.scale(2).add(b.scale(-5)).add(c.scale(4)).add(d.reverse()).scale(t * t))
                .add(a.reverse().add(b.scale(3)).add(c.scale(-3)).add(d).scale(t * t * t))
                .scale(0.5);
    }

    @Override
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return this.points.get(0);
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return this.points.get(this.points.size() - 1);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public Collection<Line> getUtilityPoints(Level level, Vec3 cutsceneStart, int initLevel) {
        List<Line> output = new ArrayList<>();
        List<PointProvider> allPoints = new ArrayList<>(this.points);
        allPoints.add(end);
        PointProvider previous = start;
        for (PointProvider i : allPoints) {
            output.add(new Line(previous, i, initLevel));
            previous = i;
        }
        return output;
    }

    public static CatmullRomSpline fromNetwork(FriendlyByteBuf buf, Path path) {
        int length = buf.readInt();
        ArrayList<PointProvider> points = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            points.add(CutsceneNetworkHandler.readPointProvider(buf));
        }
        int weight = buf.readInt();
        return new CatmullRomSpline(weight, points.toArray(new PointProvider[0]));
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(points.size());
        for (PointProvider i : points) {
            CutsceneNetworkHandler.writePointProvider(buf, i);
        }
        buf.writeInt(weight);
    }
    
    public static CatmullRomSpline fromJSON(JsonObject json, Path path) {
        JsonArray points_j = json.getAsJsonArray("points");
        ArrayList<PointProvider> points = new ArrayList<>();
        for (JsonElement i : points_j) {
            points.add(JsonHelper.pointFromJson(i));
        }
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new CatmullRomSpline(weight, points.toArray(new PointProvider[0]));
    }
    
    @Override
    public SegmentSerializer<CatmullRomSpline> getSerializer() {
        return CutsceneManager.CATMULL_ROM;
    }

    public int getSize() {
        return this.points.size();
    }
}
