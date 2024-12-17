package net.thewinnt.cutscenes;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.systems.RenderSystem;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.networking.packets.CutsceneOverPacket;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.rotation.RotationSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.command.CutsceneCommand;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StopCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.path.PathLike.SegmentSerializer;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;
import net.thewinnt.cutscenes.platform.ClientPlatformAbstractions;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;
import net.thewinnt.cutscenes.transition.Transition.TransitionSerializer;
import net.thewinnt.cutscenes.util.LoadResolver;

/** The main class of Cutscene API. Sort of. */
public class CutsceneAPI {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random RANDOM = new Random();
    public static final Gson GSON = new GsonBuilder().create();
    /**
     * A number representing the current format version. Increments whenever a breaking change happens.
     * <p>
     * Current version: {@code 0} (Cutscene API 1.x)
     */
    public static final int DATA_VERSION = 0;
    /** 
     * A salt value, updated each time a cutscene is started. Used for randomizing waypoint locations.
     * @see net.thewinnt.cutscenes.path.point.WaypointProvider#getPoint(Level, Vec3)
     */
    private static long WAYPOINT_SALT = RANDOM.nextLong();
    private static PlatformAbstractions PLATFORM;
    private static ClientPlatformAbstractions CLIENT_PLATFORM;

    // registry keys
    public static final ResourceKey<Registry<EasingSerializer<?>>> EASING_SERIALIZER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:easing_types"));
    public static final ResourceKey<Registry<CutsceneEffectSerializer<?>>> CUTSCENE_EFFECT_SERIALIZER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:effect_serializers"));
    public static final ResourceKey<Registry<SegmentSerializer<?>>> SEGMENT_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:segment_types"));
    public static final ResourceKey<Registry<PointSerializer<?>>> POINT_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:point_providers"));
    public static final ResourceKey<Registry<TransitionSerializer<?>>> TRANSITION_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:transition_types"));
    public static final ResourceKey<Registry<DelayProviderSerializer<?>>> DELAY_PROVIDER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:delay_providers"));
    public static final ResourceKey<Registry<RotationSerializer<?>>> ROTATION_HANDLER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:rotation_handlers"));

    // registries
    public static MappedRegistry<EasingSerializer<?>> EASING_SERIALIZERS;
    public static MappedRegistry<CutsceneEffectSerializer<?>> CUTSCENE_EFFECT_SERIALIZERS;
    public static MappedRegistry<SegmentSerializer<?>> SEGMENT_TYPES;
    public static MappedRegistry<PointSerializer<?>> POINT_TYPES;
    public static MappedRegistry<TransitionSerializer<?>> TRANSITION_TYPES;
    public static MappedRegistry<DelayProviderSerializer<?>> DELAY_PROVIDERS;
    public static MappedRegistry<RotationSerializer<?>> ROTATION_HANDLERS;

    public static void onInitialize(@NotNull PlatformAbstractions abstractions) {
        CutsceneAPI.PLATFORM = abstractions;

        // registries
        abstractions.registerRegistry(CutsceneAPI.EASING_SERIALIZER_KEY, registry -> CutsceneAPI.EASING_SERIALIZERS = registry);
        abstractions.registerRegistry(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZER_KEY, registry -> CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS = registry);
        abstractions.registerRegistry(CutsceneAPI.SEGMENT_TYPE_KEY, registry -> CutsceneAPI.SEGMENT_TYPES = registry);
        abstractions.registerRegistry(CutsceneAPI.POINT_TYPE_KEY, registry -> CutsceneAPI.POINT_TYPES = registry);
        abstractions.registerRegistry(CutsceneAPI.TRANSITION_TYPE_KEY, registry -> CutsceneAPI.TRANSITION_TYPES = registry);
        abstractions.registerRegistry(CutsceneAPI.DELAY_PROVIDER_KEY, registry -> CutsceneAPI.DELAY_PROVIDERS = registry);
        abstractions.registerRegistry(CutsceneAPI.ROTATION_HANDLER_KEY, registry -> CutsceneAPI.ROTATION_HANDLERS = registry);

        // networking
        abstractions.registerClientboundPacket(PreviewCutscenePacket.class, PreviewCutscenePacket::read, PreviewCutscenePacket.ID);
        abstractions.registerClientboundPacket(StartCutscenePacket.class, StartCutscenePacket::read, StartCutscenePacket.ID);
        abstractions.registerClientboundPacket(StopCutscenePacket.class, StopCutscenePacket::read, StopCutscenePacket.ID);
        abstractions.registerClientboundPacket(UpdateCutscenesPacket.class, UpdateCutscenesPacket::read, UpdateCutscenesPacket.ID);
        abstractions.registerServerboundPacket(CutsceneOverPacket.class, buf -> new CutsceneOverPacket(), CutsceneOverPacket.ID);

        // other stuff
        addReloadListeners(abstractions);
        abstractions.submitOnRegisterCommand(CutsceneCommand::register);
    }

    public static void onInitializeClient(@NotNull ClientPlatformAbstractions abstractions) {
        CutsceneAPI.CLIENT_PLATFORM = abstractions;
        abstractions.submitCameraAngleModifier(ClientCutsceneManager::setCameraPosition);
        abstractions.submitOnLogout(ClientCutsceneManager::onLogout);
        abstractions.submitOnClientTick(ClientCutsceneManager::onClientTick);
    }

    /**
     * Updates the salt value used for waypoint sorting.
     * Called whenever a cutscene is started or a preview is set up.
     * <p>
     * It is not recommended to run this when a cutscene is running.
     */
    public static void updateSalt() {
        WAYPOINT_SALT = RANDOM.nextLong();
    }

    public static long getWaypointSalt() {
        return WAYPOINT_SALT;
    }

    public static PlatformAbstractions platform() {
        return PLATFORM;
    }

    public static ClientPlatformAbstractions clientPlatform() {
        return CLIENT_PLATFORM;
    }

    public static void addReloadListeners(PlatformAbstractions abstractions) {
        abstractions.registerReloadListener(new SimpleJsonResourceReloadListener(GSON, "easing_macros") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                Easing.EASING_MACROS.clear();
                LoadResolver<Easing> macroLoader = new LoadResolver<>(Easing::fromJSON, files, true);
                Easing.EASING_MACROS.putAll(macroLoader.load());
                LOGGER.info("Loaded {} easing macros", Easing.EASING_MACROS.size());
            }
        }, new ResourceLocation("cutscenes:easing_macros"));
        abstractions.registerReloadListener(new SimpleJsonResourceReloadListener(GSON, "cutscenes") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                CutsceneManager.REGISTRY.clear();
                PointProvider.POINT_CACHE.clear();
                AtomicInteger loaded = new AtomicInteger();
                files.forEach((id, element) -> {
                    try {
                        JsonObject json = GsonHelper.convertToJsonObject(element, "cutscene");
                        CutsceneManager.registerCutscene(id, CutsceneType.fromJSON(json));
                        loaded.getAndIncrement();
                    } catch (RuntimeException e) {
                        LOGGER.error("Exception loading cutscene {}", id);
                        LOGGER.error("Caused by: ", e);
                    }
                });
                LOGGER.info("Loaded {} cutscenes", loaded.get());
            }
        }, new ResourceLocation("cutscenes:cutscenes"));
    }
}
