package net.thewinnt.cutscenes.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.command.EndingReasonArgument;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.platform.Services;
import net.thewinnt.cutscenes.rotation.RotationSerializer;

public final class CutsceneAPIFabric implements ModInitializer {
    public static final FabricPlatform PLATFORM = ((FabricPlatform) Services.PLATFORM);
    public static final EntityType<WaypointEntity> WAYPOINT = EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC)
        .sized(0.1f, 0.1f)
        .clientTrackingRange(9999)
        .canSpawnFarFromPlayer()
        .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("cutscenes", "waypoint")));

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        CutsceneAPI.onInitialize();
        ServerLifecycleEvents.SERVER_STARTING.register(PLATFORM::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PLATFORM.setServer(null));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            PLATFORM.sendPacketToPlayer(new UpdateCutscenesPacket(CutsceneManager.REGISTRY), player);
            if (joined && CutsceneManager.getPreviewedCutscene() != null) {
                PLATFORM.sendPacketToPlayer(new PreviewCutscenePacket(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()), CutsceneManager.previewOffset, CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll), player);
            }
        });
        // register stuff
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.EASING_SERIALIZER_KEY, CutsceneAPI.EASING_SERIALIZERS, RegistrationInfo.BUILT_IN);
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZER_KEY, CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS, RegistrationInfo.BUILT_IN);
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.SEGMENT_TYPE_KEY, CutsceneAPI.SEGMENT_TYPES, RegistrationInfo.BUILT_IN);
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.POINT_TYPE_KEY, CutsceneAPI.POINT_TYPES, RegistrationInfo.BUILT_IN);
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.TRANSITION_TYPE_KEY, CutsceneAPI.TRANSITION_TYPES, RegistrationInfo.BUILT_IN);
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.DELAY_PROVIDER_KEY, CutsceneAPI.DELAY_PROVIDERS, RegistrationInfo.BUILT_IN);
        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(CutsceneAPI.ROTATION_HANDLER_KEY, CutsceneAPI.ROTATION_HANDLERS, RegistrationInfo.BUILT_IN);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, "cutscenes:waypoint", WAYPOINT);

        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "line"), CutsceneManager.LINE);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "bezier"), CutsceneManager.BEZIER);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "catmull_rom"), CutsceneManager.CATMULL_ROM);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "path"), CutsceneManager.PATH);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "constant"), CutsceneManager.CONSTANT);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "look_at_point"), CutsceneManager.LOOK_AT_POINT);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "transition"), CutsceneManager.PATH_TRANSITION);
        CutsceneManager.registerSegmentType(Identifier.fromNamespaceAndPath("cutscenes", "calculated"), CutsceneManager.CALCULATED_POINT);

        CutsceneManager.registerPointType(Identifier.fromNamespaceAndPath("cutscenes", "static"), CutsceneManager.STATIC);
        CutsceneManager.registerPointType(Identifier.fromNamespaceAndPath("cutscenes", "waypoint"), CutsceneManager.WAYPOINT);
        CutsceneManager.registerPointType(Identifier.fromNamespaceAndPath("cutscenes", "world"), CutsceneManager.WORLD);

        CutsceneManager.registerTransitionType(Identifier.fromNamespaceAndPath("cutscenes", "no_op"), CutsceneManager.NO_OP);
        CutsceneManager.registerTransitionType(Identifier.fromNamespaceAndPath("cutscenes", "smooth_ease"), CutsceneManager.SMOOTH_EASE);
        CutsceneManager.registerTransitionType(Identifier.fromNamespaceAndPath("cutscenes", "fade"), CutsceneManager.FADE);

        EasingSerializer.init();
        CutsceneEffectSerializer.init();
        DelayProviderSerializer.init();
        RotationSerializer.init();

        ArgumentTypeRegistry.registerArgumentType(
            Identifier.fromNamespaceAndPath("cutscenes", "ending_reason"),
            EndingReasonArgument.class,
            SingletonArgumentInfo.contextFree(EndingReasonArgument::endingReason)
        );

        PLATFORM.clientboundPackets.forEach(FabricPlatform::registerClientboundPacket);
        PLATFORM.serverboundPackets.forEach(FabricPlatform::registerServerboundPacket);
        PLATFORM.serverboundPackets.forEach(type -> {
            ServerPlayNetworking.registerGlobalReceiver(type.type(), (packet, context) -> {
                context.server().execute(() -> packet.execute(context.player()));
            });
        });
    }
}
