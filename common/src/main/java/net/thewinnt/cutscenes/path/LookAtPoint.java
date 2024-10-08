package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.util.JsonHelper;

import java.util.Objects;

public class LookAtPoint implements PathLike {
    private final PointProvider point;
    private final PathLike pathSupplier;
    private final int weight;

    public LookAtPoint(PointProvider point, PathLike pathSupplier) {
        this.point = point;
        this.pathSupplier = Objects.requireNonNull(pathSupplier);
        this.weight = 1;
    }
    
    public LookAtPoint(PointProvider point, PathLike pathSupplier, int weight) {
        this.point = point;
        this.pathSupplier = Objects.requireNonNull(pathSupplier);
        this.weight = weight;
    }

    public LookAtPoint(Vec3 point, PathLike pathSupplier) {
        this.point = new StaticPointProvider(point);
        this.pathSupplier = Objects.requireNonNull(pathSupplier);
        this.weight = 1;
    }
    
    public LookAtPoint(Vec3 point, PathLike pathSupplier, int weight) {
        this.point = new StaticPointProvider(point);
        this.pathSupplier = Objects.requireNonNull(pathSupplier);
        this.weight = weight;
    }

    @Override
    public Vec3 getPoint(double t, Level l, Vec3 s) {
        Vec3 start = pathSupplier.getPoint(t, l, s);
        start = start.yRot((float)Math.toRadians(ClientCutsceneManager.startPathYaw));
        start = start.zRot((float)Math.toRadians(ClientCutsceneManager.startPathPitch));
        start = start.xRot((float)Math.toRadians(ClientCutsceneManager.startPathRoll));
        double d0 = point.getPoint(l, s).x - start.x;
        double d1 = point.getPoint(l, s).y - start.y;
        double d2 = point.getPoint(l, s).z - start.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        double xRot = Mth.wrapDegrees((-(Mth.atan2(d1, d3) * (180F / Math.PI))));
        double yRot = Mth.wrapDegrees((Mth.atan2(d2, d0) * (180F / Math.PI)) - 90.0F);
        return new Vec3(yRot, xRot, 0);
    }

    @Override
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return new StaticPointProvider(this.getPoint(0, level, cutsceneStart));
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return new StaticPointProvider(this.getPoint(1, level, cutsceneStart));
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
        return CutsceneManager.LOOK_AT_POINT;
    }

    public static LookAtPoint fromNetwork(FriendlyByteBuf buf, Path path) {
        PointProvider point = CutsceneNetworkHandler.readPointProvider(buf);
        int weight = buf.readInt();
        return new LookAtPoint(point, path, weight);
    }

    public static LookAtPoint fromJSON(JsonObject json, Path path) {
        PointProvider point = JsonHelper.pointFromJson(json, "point");
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new LookAtPoint(point, path, weight);
    }
}
