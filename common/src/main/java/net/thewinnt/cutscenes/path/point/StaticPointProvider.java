package net.thewinnt.cutscenes.path.point;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.util.JsonHelper;

public record StaticPointProvider(Vec3 point) implements PointProvider {
    @Override
    public Vec3 getPoint(Level level, Vec3 cutsceneStart) {
        return point;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVec3(point);
    }

    @Override
    public PointSerializer<StaticPointProvider> getSerializer() {
        return CutsceneManager.STATIC;
    }

    @Override
    public boolean shouldCache() {
        // it's actually faster to straight up return the value than to look it up in cache
        return false;
    }

    public static StaticPointProvider fromNetwork(FriendlyByteBuf buf) {
        return new StaticPointProvider(buf.readVec3());
    }

    public static StaticPointProvider fromJSON(JsonObject obj) {
        return new StaticPointProvider(JsonHelper.vec3FromJson(obj, "point"));
    }
}
