package net.thewinnt.cutscenes;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import net.thewinnt.cutscenes.path.point.PointProvider;
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
import net.thewinnt.cutscenes.platform.AbstractPacket;
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
    public static final ResourceKey<Registry<EasingSerializer<?>>> EASING_SERIALIZER_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:easing_types"));
    public static final ResourceKey<Registry<CutsceneEffectSerializer<?>>> CUTSCENE_EFFECT_SERIALIZER_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:effect_serializers"));
    public static final ResourceKey<Registry<SegmentSerializer<?>>> SEGMENT_TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:segment_types"));
    public static final ResourceKey<Registry<PointSerializer<?>>> POINT_TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:point_providers"));
    public static final ResourceKey<Registry<TransitionSerializer<?>>> TRANSITION_TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:transition_types"));
    public static final ResourceKey<Registry<DelayProviderSerializer<?>>> DELAY_PROVIDER_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:delay_providers"));

    // registries
    public static final MappedRegistry<EasingSerializer<?>> EASING_SERIALIZERS = new DefaultedMappedRegistry<>("cutscenes:linear", EASING_SERIALIZER_KEY, Lifecycle.stable(), false);
    public static final MappedRegistry<CutsceneEffectSerializer<?>> CUTSCENE_EFFECT_SERIALIZERS = new MappedRegistry<>(CUTSCENE_EFFECT_SERIALIZER_KEY, Lifecycle.stable());
    public static final MappedRegistry<SegmentSerializer<?>> SEGMENT_TYPES = new MappedRegistry<>(SEGMENT_TYPE_KEY, Lifecycle.stable());
    public static final MappedRegistry<PointSerializer<?>> POINT_TYPES = new MappedRegistry<>(POINT_TYPE_KEY, Lifecycle.stable());
    public static final MappedRegistry<TransitionSerializer<?>> TRANSITION_TYPES = new DefaultedMappedRegistry<>("cutscenes:no_op", TRANSITION_TYPE_KEY, Lifecycle.stable(), false);
    public static final MappedRegistry<DelayProviderSerializer<?>> DELAY_PROVIDERS = new DefaultedMappedRegistry<>("cutscenes:undertale", DELAY_PROVIDER_KEY, Lifecycle.stable(), false);

    public static void onInitialize(@NotNull PlatformAbstractions abstractions) {
        CutsceneAPI.PLATFORM = abstractions;

        // networking
        abstractions.registerClientboundPacket(PreviewCutscenePacket.TYPE, PreviewCutscenePacket::read, AbstractPacket::execute);
        abstractions.registerClientboundPacket(StartCutscenePacket.TYPE, StartCutscenePacket::read, AbstractPacket::execute);
        abstractions.registerClientboundPacket(StopCutscenePacket.TYPE, buf -> new StopCutscenePacket(), AbstractPacket::execute);
        abstractions.registerClientboundPacket(UpdateCutscenesPacket.TYPE, UpdateCutscenesPacket::read, AbstractPacket::execute);

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
        }, ResourceLocation.parse("cutscenes:easing_macros"));
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
        }, ResourceLocation.parse("cutscenes:cutscenes"));
    }
}
