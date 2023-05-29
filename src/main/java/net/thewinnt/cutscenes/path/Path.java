package net.thewinnt.cutscenes.path;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import oshi.util.tuples.Pair;

public class Path implements PathLike {
    private final ArrayList<PathLike> segments;
    private final int weight;
    private int weightSum;

    public Path(PathLike... segments) {
        this(1, segments);
    }

    public Path(int weight, PathLike... segments) {
        if (segments.length == 0) {
            throw new IllegalArgumentException("A Path must have at least one segment");
        }
        this.segments = new ArrayList<>(List.of(segments));
        for (PathLike i : segments) {
            this.weightSum += i.getWeight();
        }
        this.weight = weight;
    }

    /** Constructs a Path without any segments - for private use only! */
    private Path(int weight) {
        this.weight = weight;
        this.segments = new ArrayList<>();
        this.weightSum = 0;
    }

    @Override
    public Vec3 getPoint(double delta, Level l, Vec3 s) {
        if (delta <= 0) return this.getStart(l, s).getPoint(l, s);
        if (delta >= 1) return this.getEnd(l, s).getPoint(l, s);
        double val = delta * weightSum;
        int i; // define the variable outside the loop to use it later
        for (i = 0; i < this.segments.size() && val >= this.segments.get(i).getWeight(); i++) {
            val -= this.segments.get(i).getWeight();
        }
        PathLike segment = this.segments.get(i);
        if (segment instanceof LookAtPoint) { // special handling!
            return segment.getPoint(delta, l, s);
        } else {
            return segment.getPoint(val / segment.getWeight(), l, s);
        }
    }

    @Override
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return this.segments.get(0).getStart(level, cutsceneStart);
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return this.segments.get(this.segments.size() - 1).getEnd(level, cutsceneStart);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public PathLike last() {
        return this.segments.get(this.segments.size() - 1);
    }

    /** Literally makes it shorter
     * @deprecated for the same reason as everything else. Just look at this code, it's awful!
     */
    @Deprecated(since = "1.1", forRemoval = true)
    private static StaticPointProvider s(Vec3 v) {
        if (v == null) return null;
        return new StaticPointProvider(v);
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public Path continueBezier(Vec3 control_b, Vec3 end, int weight) {
        if (this.last() instanceof BezierCurve bezier && bezier.getControlB() != null) {
            // THIS is why i deprecate this stuff. use datapacks instead. you can do all the same stuff, except it's easier
            this.segments.add(new BezierCurve(bezier.getEnd(null, null), new StaticPointProvider(bezier.getControlB().getPoint(null, null).lerp(bezier.getEnd(null, null).getPoint(null, null), 2)), s(control_b), s(end), weight));
        } else {
            this.segments.add(new BezierCurve(this.last().getEnd(null, null), null, s(control_b), s(end), weight));
        }
        this.weightSum += weight;
        return this;
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public Path continueBezier(Vec3 control_b, Vec3 end) {
        return continueBezier(control_b, end, 1);
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public Path addBezier(Vec3 control_a, Vec3 control_b, Vec3 end, int weight) {
        this.segments.add(new BezierCurve(this.last().getEnd(null, null), s(control_a), s(control_b), s(end), weight));
        return this;
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public Path addBezier(Vec3 control_a, Vec3 control_b, Vec3 end) {
        return addBezier(control_a, control_b, end, 1);
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public Path addLinear(Vec3 end, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation, int weight) {
        this.segments.add(new LineSegment(this.last().getEnd(null, null), s(end), easingX, easingY, easingZ, weight, isRotation));
        return this;
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public Path addLinear(Vec3 end, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation) {
        return this.addLinear(end, easingX, easingY, easingZ, isRotation, 1);
    }

    public Path add(PathLike segment) {
        this.segments.add(segment);
        this.weightSum += segment.getWeight();
        return this;
    }

    public Path set(PathLike segment, int index) {
        this.weightSum -= segments.get(index).getWeight();
        this.segments.set(index, segment);
        this.weightSum += segment.getWeight();
        return this;
    }

    public int indexOf(PathLike segment) {
        return this.segments.indexOf(segment);
    }
    
    @Override
    public SegmentSerializer<Path> getSerializer() {
        return CutsceneManager.PATH;
    }
    
    public static Path fromNetwork(FriendlyByteBuf buf, Path path) {
        Path output = new Path(buf.readInt());
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            ResourceLocation id = buf.readResourceLocation();
            if (CutsceneManager.getSegmentType(id) == null) {
                throw new IllegalArgumentException("Unknown segment type: " + id.toString());
            }
            if (id.equals(new ResourceLocation("cutscenes", "look_at_point"))) {
                // special handling - look_at_point needs a rotation path, while others need this path
                output.add(CutsceneManager.LOOK_AT_POINT.fromNetwork(buf, path));
            } else {
                output.add(CutsceneManager.getSegmentType(id).fromNetwork(buf, output));
            }
        }
        return output;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(weight);
        buf.writeInt(this.segments.size());
        for (PathLike segment : this.segments) {
            buf.writeResourceLocation(CutsceneManager.getSegmentTypeId(segment.getSerializer()));
            segment.toNetwork(buf);
        }
    }

    public static Path fromJSON(JsonObject json, Path path) {
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        Path output = new Path(weight);
        JsonArray segments_j = json.getAsJsonArray("segments");
        for (JsonElement i : segments_j) {
            JsonObject j = i.getAsJsonObject();
            ResourceLocation id = new ResourceLocation(j.get("type").getAsString());
            if (CutsceneManager.getSegmentType(id) == null) {
                throw new IllegalArgumentException("Unknown segment type: " + id.toString());
            }
            if (id.equals(new ResourceLocation("cutscenes", "look_at_point"))) {
                // special handling - look_at_point needs a rotation path, while others need this path
                output.add(CutsceneManager.LOOK_AT_POINT.fromJSON(j, path));
            } else {
                output.add(CutsceneManager.getSegmentType(id).fromJSON(j, output));
            }
        }
        return output;
    }

    public int size() {
        return this.segments.size();
    }
    
    public PathLike getSegment(int index) {
        return this.segments.get(index);
    }

    public int getWeightSum() {
        return weightSum;
    }

    public Pair<Double, Double> getSegmentRange(PathLike segment) {
        double previousSum = 0; // don't wanna cast it later
        int targetIndex = this.segments.indexOf(segment);
        if (targetIndex == -1) {
            throw new IllegalArgumentException("Attempted to get a range of values for a missing segment");
        }
        for (int i = 0; i < targetIndex; i++) {
            previousSum += this.segments.get(i).getWeight();
        }
        double rangeStart = previousSum / weightSum;
        return new Pair<Double,Double>(rangeStart, rangeStart + (double)segment.getWeight() / weightSum);
    }
}
