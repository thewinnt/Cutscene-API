package net.thewinnt.cutscenes.math;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.util.JsonHelper;

public class CatmullRomSpline implements PathLike {
    private Vec3 start;
    private final ArrayList<Vec3> points = new ArrayList<>();
    private Vec3 end;
    private final int weight;
    private double length;

    public CatmullRomSpline(Vec3... points) {
        this(1, points);
    }

    public CatmullRomSpline(int weight, Vec3... points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("A Catmull-Rom spline must have at least 2 points");
        }
        this.points.addAll(List.of(points)); // also does null check
        this.start = points[1].lerp(points[0], 2);
        this.end = points[points.length - 2].lerp(points[points.length - 1], 2);
        this.weight = weight;
        this.cacheLength();
    }

    private void cacheLength() {
        double total_length = 0;
        double target = 20d * (this.points.size() - 1);
        for (int i = 0; i < target + 1; i++) {
            Vec3 a = this.getPoint(i / target);
            Vec3 b = this.getPoint((i + 1) / target);
            total_length += a.distanceTo(b);
        }
        this.length = total_length;
    }

    @Override
    public Vec3 getPoint(double t) {
        int startSegment = (int)((this.points.size() - 1) * t);
        Vec3 a, b, c, d;
        if (t <= 0) return this.points.get(0);
        if (t >= 1) return this.points.get(this.points.size() - 1);
        if (this.points.size() == 2) {
            a = start;
            b = points.get(0);
            c = points.get(1);
            d = end;
        } else if (startSegment == 0) {
            a = start;
            b = points.get(0);
            c = points.get(1);
            d = points.get(2);
        } else if (startSegment == points.size() - 2) {
            a = points.get(startSegment - 1);
            b = points.get(startSegment);
            c = points.get(startSegment + 1);
            d = end;
        } else {
            a = points.get(startSegment - 1);
            b = points.get(startSegment);
            c = points.get(startSegment + 1);
            d = points.get(startSegment + 2);
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
    public double getLength() {
        return length;
    }

    @Override
    public Vec3 getStart() {
        return this.points.get(0);
    }

    @Override
    public Vec3 getEnd() {
        return this.points.get(this.points.size() - 1);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public static CatmullRomSpline fromNetwork(FriendlyByteBuf buf, Path path) {
        int length = buf.readInt();
        ArrayList<Vec3> points = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            points.add(CutsceneNetworkHandler.readVec3(buf));
        }
        int weight = buf.readInt();
        return new CatmullRomSpline(weight, points.toArray(new Vec3[0]));
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(points.size());
        for (Vec3 i : points) {
            CutsceneNetworkHandler.writeVec3(buf, i);
        }
        buf.writeInt(weight);
    }
    
    public static CatmullRomSpline fromJSON(JsonObject json, Path path) {
        JsonArray points_j = json.getAsJsonArray("points");
        ArrayList<Vec3> points = new ArrayList<>();
        for (JsonElement i : points_j) {
            points.add(JsonHelper.vec3FromJson(i));
        }
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new CatmullRomSpline(weight, points.toArray(new Vec3[0]));
    }
    
    @Override
    public SegmentSerializer<CatmullRomSpline> getSerializer() {
        return CutsceneManager.CATMULL_ROM;
    }

    public void addPoint(Vec3 point) {
        this.end = this.points.get(points.size() - 1).lerp(point, 2);
        this.points.add(point);
        this.cacheLength();
    }

    public void addPoint(int index, Vec3 point) {
        if (index >= this.points.size() - 1) {
            this.end = this.points.get(points.size() - 1).lerp(point, 2);
        }
        this.points.add(index, point);
        this.cacheLength();
    }

    public int getSize() {
        return this.points.size();
    }
}
