package net.thewinnt.cutscenes.math;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.ClientCutsceneManager;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.util.JsonHelper;

public class LookAtPoint implements PathLike {
    private final Vec3 point;
    private PathLike pathSupplier;
    private final int weight;

    public LookAtPoint(Vec3 point, PathLike pathSupplier) {
        this.point = point;
        this.pathSupplier = pathSupplier;
        this.weight = 1;
    }
    
    public LookAtPoint(Vec3 point, PathLike pathSupplier, int weight) {
        this.point = point;
        this.pathSupplier = pathSupplier;
        this.weight = weight;
    }

    @Override
    public Vec3 getPoint(double t) {
        if (pathSupplier == null) {
            throw new IllegalStateException("Tried to use LookAtPoint for a camera path");
        }
        Vec3 start = pathSupplier.getPoint(t);
        start = start.yRot((float)Math.toRadians(ClientCutsceneManager.startPathYaw));
        start = start.zRot((float)Math.toRadians(ClientCutsceneManager.startPathPitch));
        start = start.xRot((float)Math.toRadians(ClientCutsceneManager.startPathRoll));
        double d0 = point.x - start.x;
        double d1 = point.y - start.y;
        double d2 = point.z - start.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        double xRot = Mth.wrapDegrees((-(Mth.atan2(d1, d3) * (180F / Math.PI))));
        double yRot = Mth.wrapDegrees((Mth.atan2(d2, d0) * (180F / Math.PI)) - 90.0F);
        return new Vec3(yRot, xRot, 0);
    }

    @Override
    public Vec3 getStart() {
        return this.getPoint(0);
    }

    @Override
    public Vec3 getEnd() {
        return this.getPoint(1);
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writeVec3(buf, point);
        buf.writeInt(weight);
    }

    @Override
    public SegmentSerializer<?> getSerializer() {
        return CutsceneManager.LOOK_AT_POINT;
    }

    public static LookAtPoint fromNetwork(FriendlyByteBuf buf, Path path) {
        Vec3 point = CutsceneNetworkHandler.readVec3(buf);
        int weight = buf.readInt();
        return new LookAtPoint(point, path, weight);
    }

    public static LookAtPoint fromJSON(JsonObject json, Path path) {
        Vec3 point = JsonHelper.vec3FromJson(json, "point");
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new LookAtPoint(point, path, weight);
    }
}
