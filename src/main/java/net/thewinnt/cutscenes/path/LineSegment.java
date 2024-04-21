package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.util.JsonHelper;

import java.util.function.Function;

public class LineSegment implements PathLike {
    private final PointProvider a;
    private final PointProvider b;
    private final int weight;
    private final Easing easingX;
    private final Easing easingY;
    private final Easing easingZ;
    private final boolean isRotation;

    public LineSegment(PointProvider a, PointProvider b, Easing easingX, Easing easingY, Easing easingZ, int weight, boolean isRotation) {
        this.a = a;
        this.b = b;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = weight;
        this.isRotation = isRotation;
    }

    public LineSegment(PointProvider a, PointProvider b, Easing easingX, Easing easingY, Easing easingZ, boolean isRotation) {
        this.a = a;
        this.b = b;
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = 1;
        this.isRotation = isRotation;
    }

    public LineSegment(PointProvider start, PointProvider end) {
        this(start, end, SimpleEasing.LINEAR, SimpleEasing.LINEAR, SimpleEasing.LINEAR, false);
    }

    public LineSegment(PointProvider start, PointProvider end, boolean isRotation) {
        this(start, end, SimpleEasing.LINEAR, SimpleEasing.LINEAR, SimpleEasing.LINEAR, isRotation);
    }

    public LineSegment(PointProvider start, PointProvider end, Easing easing, boolean isRotation) {
        this(start, end, easing, easing, easing, false);
    }

    public LineSegment(Vec3 a, Vec3 b, Easing easingX, Easing easingY, Easing easingZ, int weight, boolean isRotation) {
        this.a = new StaticPointProvider(a);
        this.b = new StaticPointProvider(b);
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = weight;
        this.isRotation = isRotation;
    }
    
    public LineSegment(Vec3 a, Vec3 b, Easing easingX, Easing easingY, Easing easingZ, boolean isRotation) {
        this.a = new StaticPointProvider(a);
        this.b = new StaticPointProvider(b);
        this.easingX = easingX;
        this.easingY = easingY;
        this.easingZ = easingZ;
        this.weight = 1;
        this.isRotation = isRotation;
    }
    
    public LineSegment(Vec3 start, Vec3 end) {
        this(start, end, SimpleEasing.LINEAR, SimpleEasing.LINEAR, SimpleEasing.LINEAR, false);
    }
    
    public LineSegment(Vec3 start, Vec3 end, boolean isRotation) {
        this(start, end, SimpleEasing.LINEAR, SimpleEasing.LINEAR, SimpleEasing.LINEAR, isRotation);
    }
    
    public LineSegment(Vec3 start, Vec3 end, Easing easing, boolean isRotation) {
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
            x = Mth.rotLerp((float)easingX.get(t), (float)a.getPoint(l, s).x, (float)b.getPoint(l, s).x);
            y = Mth.rotLerp((float)easingY.get(t), (float)a.getPoint(l, s).y, (float)b.getPoint(l, s).y);
            z = Mth.rotLerp((float)easingZ.get(t), (float)a.getPoint(l, s).z, (float)b.getPoint(l, s).z);
        } else {
            x = Mth.lerp(easingX.get(t), a.getPoint(l, s).x, b.getPoint(l, s).x);
            y = Mth.lerp(easingY.get(t), a.getPoint(l, s).y, b.getPoint(l, s).y);
            z = Mth.lerp(easingZ.get(t), a.getPoint(l, s).z, b.getPoint(l, s).z);
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
        Easing easingX = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingY = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingZ = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        boolean isRotation = buf.readBoolean();
        int weight = buf.readInt();
        return new LineSegment(start, end, easingX, easingY, easingZ, weight, isRotation);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writePointProvider(buf, a);
        CutsceneNetworkHandler.writePointProvider(buf, b);
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easingX.getSerializer()));
        easingX.getSerializer().toNetwork(buf, easingX);
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easingY.getSerializer()));
        easingY.getSerializer().toNetwork(buf, easingY);
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easingZ.getSerializer()));
        easingZ.getSerializer().toNetwork(buf, easingZ);
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
        Easing easingX = Easing.NO_LEGACY_CODEC.orElse(SimpleEasing.LINEAR).parse(JsonOps.INSTANCE, json.get("easing_x")).get().orThrow();
        Easing easingY = Easing.NO_LEGACY_CODEC.orElse(SimpleEasing.LINEAR).parse(JsonOps.INSTANCE, json.get("easing_y")).get().orThrow();
        Easing easingZ = Easing.NO_LEGACY_CODEC.orElse(SimpleEasing.LINEAR).parse(JsonOps.INSTANCE, json.get("easing_z")).get().orThrow();
        // TODO find out why this doesn't work
        // TODO try not using legacy compat
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        boolean isRotation = GsonHelper.getAsBoolean(json, "is_rotation", false);
        return new LineSegment(start, end, easingX, easingY, easingZ, weight, isRotation);
    }
}
