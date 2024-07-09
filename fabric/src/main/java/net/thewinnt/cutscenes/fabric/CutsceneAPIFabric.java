package net.thewinnt.cutscenes.fabric;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;

public final class CutsceneAPIFabric implements ModInitializer {
    public static final FabricPlatform PLATFORM = new FabricPlatform();
    public static final EntityType<WaypointEntity> WAYPOINT = EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC)
        .sized(0.1f, 0.1f)
        .clientTrackingRange(9999)
        .canSpawnFarFromPlayer()
        .build("waypoint");

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        CutsceneAPI.onInitialize(PLATFORM);
        ServerLifecycleEvents.SERVER_STARTING.register(PLATFORM::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PLATFORM.setServer(null));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            PLATFORM.sendPacketToPlayer(new UpdateCutscenesPacket(CutsceneManager.REGISTRY), player);
            if (joined && CutsceneManager.getPreviewedCutscene() != null) {
                PLATFORM.sendPacketToPlayer(new PreviewCutscenePacket(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()), CutsceneManager.previewOffset, CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll), player);
            }
        });
        // register stuff
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.EASING_SERIALIZER_KEY, CutsceneAPI.EASING_SERIALIZERS, Lifecycle.stable());
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZER_KEY, CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS, Lifecycle.stable());
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.SEGMENT_TYPE_KEY, CutsceneAPI.SEGMENT_TYPES, Lifecycle.stable());
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.POINT_TYPE_KEY, CutsceneAPI.POINT_TYPES, Lifecycle.stable());
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.TRANSITION_TYPE_KEY, CutsceneAPI.TRANSITION_TYPES, Lifecycle.stable());
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.DELAY_PROVIDER_KEY, CutsceneAPI.DELAY_PROVIDERS, Lifecycle.stable());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, "cutscenes:waypoint", WAYPOINT);

        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "line"), CutsceneManager.LINE);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "bezier"), CutsceneManager.BEZIER);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "catmull_rom"), CutsceneManager.CATMULL_ROM);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "path"), CutsceneManager.PATH);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "constant"), CutsceneManager.CONSTANT);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "look_at_point"), CutsceneManager.LOOK_AT_POINT);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "transition"), CutsceneManager.PATH_TRANSITION);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "calculated"), CutsceneManager.CALCULATED_POINT);

        CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "static"), CutsceneManager.STATIC);
        CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "waypoint"), CutsceneManager.WAYPOINT);
        CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "world"), CutsceneManager.WORLD);

        CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "no_op"), CutsceneManager.NO_OP);
        CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "smooth_ease"), CutsceneManager.SMOOTH_EASE);
        CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "fade"), CutsceneManager.FADE);

        EasingSerializer.init();
        CutsceneEffectSerializer.init();
        DelayProviderSerializer.init();
    }
}
