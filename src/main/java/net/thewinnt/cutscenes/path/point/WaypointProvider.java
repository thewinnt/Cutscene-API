package net.thewinnt.cutscenes.path.point;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.entity.WaypointEntity;

public record WaypointProvider(String name, int searchRadius, SortType sorting) implements PointProvider {

    @Override
    public Vec3 getPoint(Level level, Vec3 cutsceneStart) {
        List<WaypointEntity> entities = level.getEntitiesOfClass(WaypointEntity.class, new AABB(cutsceneStart, cutsceneStart).inflate(searchRadius), e -> e.getWaypointName().equals(name));
        if (entities.size() > 0) {
            switch (sorting) {
                case NEAREST:
                    entities.sort((a, b) -> (int)(a.distanceToSqr(cutsceneStart) - b.distanceToSqr(cutsceneStart)));
                    break;
                case FURTHEST:
                    entities.sort((a, b) -> (int)(b.distanceToSqr(cutsceneStart) - a.distanceToSqr(cutsceneStart)));
                    break;
                case RANDOM:
                    Collections.shuffle(entities, new Random(Double.doubleToLongBits(cutsceneStart.x * cutsceneStart.y * cutsceneStart.z)));
                    break;
                case TRUE_RANDOM:
                    Collections.shuffle(entities);
                default:
                    break;
            }
            return entities.get(0).getPosition(1).subtract(cutsceneStart);
        } else {
            return new Vec3(0, 0, 0);
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeInt(searchRadius);
        buf.writeEnum(sorting);
    }

    @Override
    public PointSerializer<WaypointProvider> getSerializer() {
        return CutsceneManager.WAYPOINT;
    }

    public static WaypointProvider fromNetwork(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        int searchRadius = buf.readInt();
        SortType sortType = buf.readEnum(SortType.class);
        return new WaypointProvider(name, searchRadius, sortType);
    }

    public static WaypointProvider fromJSON(JsonObject obj) {
        String name = GsonHelper.getAsString(obj, "name");
        int searchRadius = GsonHelper.getAsInt(obj, "search_radius", 64);
        SortType sortType;
        try {
            sortType = SortType.valueOf(GsonHelper.getAsString(obj, "sort_type", "closest").toUpperCase());
        } catch (IllegalArgumentException e) {
            sortType = SortType.NEAREST;
        }
        return new WaypointProvider(name, searchRadius, sortType);
    }

    public static enum SortType {
        NEAREST,
        FURTHEST,
        FIRST_FOUND,
        RANDOM,
        TRUE_RANDOM;
    }
}
