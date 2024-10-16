package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.path.point.PointProvider;

public class PathTransition implements PathLike {
    private final Path path;
    private final int index;
    private final Easing easingX;
    private final Easing easingY;
    private final Easing easingZ;
    private final boolean isRotation;
    private final int weight;

    public PathTransition(Path path, int index, Easing easingX, Easing easingY, Easing easingZ, boolean isRotation) {
        this.path = path;
        this.index = index;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = 1;
        this.isRotation = isRotation;
    }
    
    public PathTransition(Path path, int index, Easing easingX, Easing easingY, Easing easingZ, boolean isRotation, int weight) {
        this.path = path;
        this.index = index;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = weight;
        this.isRotation = isRotation;
    }

    @Override
    public Vec3 getPoint(double t, Level l, Vec3 s) {
        double x, y, z;
        Vec3 a, b;
        PathLike previous, next;
        try {
            previous = path.getSegment(index - 1);
            next = path.getSegment(index + 1);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Attempted to use a Transition without one of the points");
        }
        if (previous instanceof LookAtPoint) {
            a = previous.getPoint(path.getSegmentRange(previous).getB() - 0.00000001, l, s);
        } else {
            a = PointProvider.getPoint(previous.getEnd(l, s), l, s);
        }
        if (next instanceof LookAtPoint) {
            b = next.getPoint(path.getSegmentRange(next).getA() + 0.00000001, l, s);
        } else {
            b = PointProvider.getPoint(next.getStart(l, s), l, s);
        }
        if (isRotation) {
            x = Mth.rotLerp((float)easingX.get(t), (float)a.x, (float)b.x);
            y = Mth.rotLerp((float)easingY.get(t), (float)a.y, (float)b.y);
            z = Mth.rotLerp((float)easingZ.get(t), (float)a.z, (float)b.z);
        } else {
            x = Mth.lerp(easingX.get(t), a.x, b.x);
            y = Mth.lerp(easingY.get(t), a.y, b.y);
            z = Mth.lerp(easingZ.get(t), a.z, b.z);
        }
        return new Vec3(x, y, z);
    }

    @Override
    public PointProvider getStart(Level l, Vec3 s) {
        return path.getSegment(index - 1).getEnd(l, s);
    }

    @Override
    public PointProvider getEnd(Level l, Vec3 s) {
        return path.getSegment(index + 1).getStart(l, s);
    }
    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(easingX, buf);
        Easing.toNetwork(easingY, buf);
        Easing.toNetwork(easingZ, buf);
        buf.writeBoolean(isRotation);
        buf.writeInt(weight);
    }

    @Override
    public SegmentSerializer<?> getSerializer() {
        return CutsceneManager.PATH_TRANSITION;
    }

    public static PathTransition fromNetwork(FriendlyByteBuf buf, Path path) {
        Easing easingX = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingY = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingZ = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        boolean isRotation = buf.readBoolean();
        int weight = buf.readInt();
        return new PathTransition(path, path.size(), easingX, easingY, easingZ, isRotation, weight);
    }

    public static PathTransition fromJSON(JsonObject json, Path path) {
        Easing easingX = Easing.fromJSON(json.get("easing_x"), SimpleEasing.LINEAR);
        Easing easingY = Easing.fromJSON(json.get("easing_y"), SimpleEasing.LINEAR);
        Easing easingZ = Easing.fromJSON(json.get("easing_z"), SimpleEasing.LINEAR);
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        boolean isRotation = GsonHelper.getAsBoolean(json, "is_rotation", false);
        return new PathTransition(path, path.size(), easingX, easingY, easingZ, isRotation, weight);
    }
}
