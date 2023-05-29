package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.path.point.PointProvider;

public class Transition implements PathLike {
    private final Path path;
    private final int index;
    private final EasingFunction easingX;
    private final EasingFunction easingY;
    private final EasingFunction easingZ;
    private final boolean isRotation;
    private final int weight;

    public Transition(Path path, int index, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation) {
        this.path = path;
        this.index = index;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = 1;
        this.isRotation = isRotation;
    }
    
    public Transition(Path path, int index, EasingFunction easingX, EasingFunction easingY, EasingFunction easingZ, boolean isRotation, int weight) {
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
            a = previous.getEnd(l, s).getPoint(l, s);
        }
        if (next instanceof LookAtPoint) {
            b = next.getPoint(path.getSegmentRange(next).getA() + 0.00000001, l, s);
        } else {
            b = next.getStart(l, s).getPoint(l, s);
        }
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
        buf.writeEnum(easingX);
        buf.writeEnum(easingY);
        buf.writeEnum(easingZ);
        buf.writeBoolean(isRotation);
        buf.writeInt(weight);
    }

    @Override
    public SegmentSerializer<?> getSerializer() {
        return CutsceneManager.TRANSITION;
    }

    public static Transition fromNetwork(FriendlyByteBuf buf, Path path) {
        EasingFunction easingX = buf.readEnum(EasingFunction.class);
        EasingFunction easingY = buf.readEnum(EasingFunction.class);
        EasingFunction easingZ = buf.readEnum(EasingFunction.class);
        boolean isRotation = buf.readBoolean();
        int weight = buf.readInt();
        return new Transition(path, path.size(), easingX, easingY, easingZ, isRotation, weight);
    }

    public static Transition fromJSON(JsonObject json, Path path) {
        EasingFunction easingX = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_x", "linear").toUpperCase());
        EasingFunction easingY = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_y", "linear").toUpperCase());
        EasingFunction easingZ = EasingFunction.valueOf(GsonHelper.getAsString(json, "easing_z", "linear").toUpperCase());
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        boolean isRotation = GsonHelper.getAsBoolean(json, "is_rotation", false);
        return new Transition(path, path.size(), easingX, easingY, easingZ, isRotation, weight);
    }
}
