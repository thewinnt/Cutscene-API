package net.thewinnt.cutscenes.forge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thewinnt.cutscenes.entity.WaypointEntity;

public class CutsceneAPIEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "cutscenes");
    public static final RegistryObject<EntityType<WaypointEntity>> WAYPOINT = REGISTRY.register("waypoint", () -> EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC)
        .sized(0.1f, 0.1f)
        .clientTrackingRange(9999)
        .canSpawnFarFromPlayer()
        .build("waypoint")
    );
}
