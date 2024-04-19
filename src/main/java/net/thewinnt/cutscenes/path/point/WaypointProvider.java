package net.thewinnt.cutscenes.path.point;

import java.util.*;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.util.JsonHelper;
import net.thewinnt.cutscenes.util.MathHelper;

public record WaypointProvider(String name, int searchRadius, SortType sorting, Vec3 offset, Optional<PointProvider> fallback) implements PointProvider {

    @Override
    public Vec3 getPoint(Level level, Vec3 cutsceneStart) {
        Vec3 offset = Objects.requireNonNullElse(this.offset, Vec3.ZERO);
        List<WaypointEntity> entities = level.getEntitiesOfClass(WaypointEntity.class, new AABB(cutsceneStart, cutsceneStart).inflate(searchRadius), e -> e.getWaypointName().equals(name));
        if (!entities.isEmpty()) {
            switch (sorting) {
                case NEAREST:
                    entities.sort((a, b) -> (int)(a.distanceToSqr(cutsceneStart) - b.distanceToSqr(cutsceneStart)));
                    break;
                case FURTHEST:
                    entities.sort((a, b) -> (int)(b.distanceToSqr(cutsceneStart) - a.distanceToSqr(cutsceneStart)));
                    break;
                case RANDOM:
                    Collections.shuffle(entities, new Random(MathHelper.hash(entities.hashCode(), CutsceneAPI.getWaypointSalt(), System.identityHashCode(this))));
                    break;
                case TRUE_RANDOM:
                    Collections.shuffle(entities);
                default:
                    break;
            }
            return entities.get(0).getPosition(1).subtract(cutsceneStart).add(offset);
        } else if (fallback.isPresent()) {
            return fallback.get().getPoint(level, cutsceneStart);
        } else {
            return offset;
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeInt(searchRadius);
        buf.writeEnum(sorting);
        buf.writeVec3(offset);
        buf.writeOptional(fallback, CutsceneNetworkHandler::writePointProvider);
    }

    @Override
    public PointSerializer<WaypointProvider> getSerializer() {
        return CutsceneManager.WAYPOINT;
    }

    public static WaypointProvider fromNetwork(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        int searchRadius = buf.readInt();
        SortType sortType = buf.readEnum(SortType.class);
        Vec3 offset = buf.readVec3();
        Optional<PointProvider> fallback = buf.readOptional(CutsceneNetworkHandler::readPointProvider);
        return new WaypointProvider(name, searchRadius, sortType, offset, fallback);
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
        Vec3 offset = JsonHelper.vec3FromJson(obj, "offset");
        PointProvider fallback = JsonHelper.pointFromJson(obj, "fallback");
        return new WaypointProvider(name, searchRadius, sortType, offset, Optional.ofNullable(fallback));
    }

    public static enum SortType {
        NEAREST,
        FURTHEST,
        FIRST_FOUND,
        RANDOM,
        TRUE_RANDOM
    }
}
