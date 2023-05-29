package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.util.JsonHelper;

public class ConstantPoint implements PathLike {
    private final PointProvider point;
    private final int weight;

    @Deprecated(since = "1.1", forRemoval = true)
    public ConstantPoint(Vec3 point) {
        this.point = new StaticPointProvider(point);
        this.weight = 1;
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public ConstantPoint(Vec3 point, int weight) {
        this.point = new StaticPointProvider(point);
        this.weight = weight;
    }

    public ConstantPoint(PointProvider point, int weight) {
        this.point = point;
        this.weight = weight;
    }

    @Override
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return point;
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return point;
    }

    @Override
    public Vec3 getPoint(double t, Level l, Vec3 s) {
        return point.getPoint(l, s);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writePointProvider(buf, point);
        buf.writeInt(weight);
    }

    @Override
    public SegmentSerializer<?> getSerializer() {
        return CutsceneManager.CONSTANT;
    }

    public static ConstantPoint fromNetwork(FriendlyByteBuf buf, Path path) {
        PointProvider point = CutsceneNetworkHandler.readPointProvider(buf);
        int weight = buf.readInt();
        return new ConstantPoint(point, weight);
    }

    public static ConstantPoint fromJSON(JsonObject json, Path path) {
        PointProvider point = JsonHelper.pointFromJson(json, "point");
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new ConstantPoint(point, weight);
    }
}
