package net.thewinnt.cutscenes.path;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;

public class CalculatedPoint implements PathLike {
    private final StaticPointProvider start;
    private final StaticPointProvider end;
    public final int weight;
    public final Easing x;
    public final Easing y;
    public final Easing z;

    public CalculatedPoint(Easing x, Easing y, Easing z) {
        this(x, y, z, 1);
    }

    public CalculatedPoint(Easing x, Easing y, Easing z, int weight) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.start = new StaticPointProvider(getPoint(0, null, null));
        this.end = new StaticPointProvider(getPoint(1, null, null));
        this.weight = weight;
    }

    @Override
    public Vec3 getPoint(double t, Level level, Vec3 cutsceneStart) {
        return new Vec3(x.get(t), y.get(t), z.get(t));
    }

    @Override
    public PointProvider getStart(Level level, Vec3 cutsceneStart) {
        return start;
    }

    @Override
    public PointProvider getEnd(Level level, Vec3 cutsceneStart) {
        return end;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public static CalculatedPoint fromNetwork(FriendlyByteBuf buf, Path path) {
        Easing easingX = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingY = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easingZ = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        int weight = buf.readInt();
        return new CalculatedPoint(easingX, easingY, easingZ, weight);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        Easing.toNetwork(x, buf);
        Easing.toNetwork(y, buf);
        Easing.toNetwork(z, buf);
        buf.writeInt(weight);
    }

    @Override
    public SegmentSerializer<CalculatedPoint> getSerializer() {
        return CutsceneManager.CALCULATED_POINT;
    }

    public static CalculatedPoint fromJSON(JsonObject json, Path path) {
        Easing easingX = Easing.fromJSON(json.get("x"), SimpleEasing.LINEAR);
        Easing easingY = Easing.fromJSON(json.get("y"), SimpleEasing.LINEAR);
        Easing easingZ = Easing.fromJSON(json.get("z"), SimpleEasing.LINEAR);
        int weight = GsonHelper.getAsInt(json, "weight", 1);
        return new CalculatedPoint(easingX, easingY, easingZ);
    }
}
