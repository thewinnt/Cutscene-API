package net.thewinnt.cutscenes.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thewinnt.cutscenes.entity.WaypointEntity;

public class CutsceneAPIEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "cutscenes");
    public static final DeferredHolder<EntityType<?>, EntityType<WaypointEntity>> WAYPOINT = REGISTRY.register("waypoint", () -> EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC)
        .sized(0.1f, 0.1f)
        .clientTrackingRange(9999)
        .canSpawnFarFromPlayer()
        .build("waypoint")
    );
}
