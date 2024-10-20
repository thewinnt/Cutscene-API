package net.thewinnt.cutscenes.path.point;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.util.JsonHelper;

public record WorldPointProvider(Vec3 point) implements PointProvider {
    @Override
    public Vec3 getPoint(Level level, Vec3 cutsceneStart) {
        return point.subtract(cutsceneStart);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVec3(point);
    }

    @Override
    public PointSerializer<WorldPointProvider> getSerializer() {
        return CutsceneManager.WORLD;
    }

    public static WorldPointProvider fromNetwork(FriendlyByteBuf buf) {
        return new WorldPointProvider(buf.readVec3());
    }

    public static WorldPointProvider fromJSON(JsonObject obj) {
        return new WorldPointProvider(JsonHelper.vec3FromJson(obj, "point"));
    }
}
