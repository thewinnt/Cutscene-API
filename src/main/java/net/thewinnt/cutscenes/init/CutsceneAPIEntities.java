package net.thewinnt.cutscenes.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thewinnt.cutscenes.entity.WaypointEntity;

public class CutsceneAPIEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "cutscenes");
    public static final DeferredHolder<EntityType<?>, EntityType<WaypointEntity>> WAYPOINT = ENTITIES.register(
        "waypoint",
        () -> EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC)
            .sized(0.1f, 0.1f)
            .clientTrackingRange(9999)
            .setTrackingRange(9999)
            .canSpawnFarFromPlayer()
            .build("waypoint")
    );
}
