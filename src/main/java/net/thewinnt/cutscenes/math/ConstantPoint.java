package net.thewinnt.cutscenes.math;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.util.JsonHelper;

public class ConstantPoint implements PathLike {
    private final Vec3 point;
    private final int weight;

    public ConstantPoint(Vec3 point) {
        this.point = point;
        this.weight = 1;
    }

    public ConstantPoint(Vec3 point, int weight) {
        this.point = point;
        this.weight = weight;
    }

    @Override
    public Vec3 getStart() {
        return point;
    }

    @Override
    public Vec3 getEnd() {
        return point;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public Vec3 getPoint(double t) {
        return point;
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
        return CutsceneManager.CONSTANT;
    }

    public static ConstantPoint fromNetwork(FriendlyByteBuf buf, Path path) {
        Vec3 point = CutsceneNetworkHandler.readVec3(buf);
        int weight = buf.readInt();
        return new ConstantPoint(point, weight);
    }

    public static ConstantPoint fromJSON(JsonObject json, Path path) {
        Vec3 point = JsonHelper.vec3FromJson(json, "point");
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new ConstantPoint(point, weight);
    }
}
