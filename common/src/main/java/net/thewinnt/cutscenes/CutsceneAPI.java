package net.thewinnt.cutscenes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.nio.AbstractNioByteChannel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.server.ServerFunctionManager;
import net.thewinnt.cutscenes.command.ExecuteCommands;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
import net.thewinnt.cutscenes.networking.packets.CutsceneOverPacket;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StopCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.path.PathLike.SegmentType;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;
import net.thewinnt.cutscenes.platform.ClientPlatformAbstractions;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;
import net.thewinnt.cutscenes.rotation.RotationSerializer;
import net.thewinnt.cutscenes.transition.Transition.TransitionSerializer;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

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
    public static final ResourceKey<Registry<SegmentType<?>>> SEGMENT_TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:segment_types"));
    public static final ResourceKey<Registry<PointSerializer<?>>> POINT_TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:point_providers"));
    public static final ResourceKey<Registry<TransitionSerializer<?>>> TRANSITION_TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:transition_types"));
    public static final ResourceKey<Registry<DelayProviderSerializer<?>>> DELAY_PROVIDER_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:delay_providers"));
    public static final ResourceKey<Registry<RotationSerializer<?>>> ROTATION_HANDLER_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("cutscenes:rotation_handlers"));

    // registries
    public static final MappedRegistry<EasingSerializer<?>> EASING_SERIALIZERS = new MappedRegistry<>(EASING_SERIALIZER_KEY, Lifecycle.stable());
    public static final MappedRegistry<CutsceneEffectSerializer<?>> CUTSCENE_EFFECT_SERIALIZERS = new MappedRegistry<>(CUTSCENE_EFFECT_SERIALIZER_KEY, Lifecycle.stable());
    public static final MappedRegistry<SegmentType<?>> SEGMENT_TYPES = new MappedRegistry<>(SEGMENT_TYPE_KEY, Lifecycle.stable());
    public static final MappedRegistry<PointSerializer<?>> POINT_TYPES = new MappedRegistry<>(POINT_TYPE_KEY, Lifecycle.stable());
    public static final MappedRegistry<TransitionSerializer<?>> TRANSITION_TYPES = new MappedRegistry<>(TRANSITION_TYPE_KEY, Lifecycle.stable());
    public static final MappedRegistry<DelayProviderSerializer<?>> DELAY_PROVIDERS = new MappedRegistry<>(DELAY_PROVIDER_KEY, Lifecycle.stable());
    public static final MappedRegistry<RotationSerializer<?>> ROTATION_HANDLERS = new MappedRegistry<>(ROTATION_HANDLER_KEY, Lifecycle.stable());

    public static void onInitialize(@NotNull PlatformAbstractions abstractions) {
        CutsceneAPI.PLATFORM = abstractions;

        // networking
        abstractions.registerClientboundPacket(PreviewCutscenePacket.TYPE, PreviewCutscenePacket::read);
        abstractions.registerClientboundPacket(StartCutscenePacket.TYPE, StartCutscenePacket::read);
        abstractions.registerClientboundPacket(StopCutscenePacket.TYPE, StopCutscenePacket::read);
        abstractions.registerClientboundPacket(UpdateCutscenesPacket.TYPE, UpdateCutscenesPacket::read);
        abstractions.registerServerboundPacket(CutsceneOverPacket.TYPE, buf -> new CutsceneOverPacket());

        // other stuff
        addReloadListeners(abstractions);
        abstractions.submitOnRegisterCommand(CutsceneCommand::register);
        abstractions.submitOnRegisterCommand(ExecuteCommands::register);

        // event listeners
        CutsceneEvents.CUTSCENE_OVER_SERVER.addListener((type, id, player, reason) -> {
            ServerFunctionManager manager = player.server.getFunctions();
            ResourceOrTag resourceOrTag = type.onOver;
            ServerPlayerExt ext = (ServerPlayerExt) player;
            if (resourceOrTag == null) return;

            CommandSourceStack stack;
            if (type.logCommands) {
                stack = player.createCommandSourceStack();
            } else {
                stack = new CommandSourceStack(
                    CommandSource.NULL,
                    player.position(),
                    player.getRotationVector(),
                    player.serverLevel(),
                    player.server.getProfilePermissions(player.getGameProfile()),
                    player.getName().getString(),
                    player.getDisplayName(),
                    player.server,
                    player
                );
            }
            Collection<CommandFunction<CommandSourceStack>> functions;
            if (resourceOrTag.isTag()) {
                functions = manager.getTag(resourceOrTag.id());
            } else {
                functions = manager.get(resourceOrTag.id()).map(List::of).orElseGet(List::of);
            }
            functions.forEach(i -> manager.execute(i, stack));
        });
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
        abstractions.registerReloadListener(new JsonLoader(GSON, "easing_macros") {
            private static final Marker MARKER = MarkerFactory.getMarker("EasingMacroLoader");

            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                Easing.EASING_MACROS.clear();
                LoadResolver<Easing> macroLoader = new LoadResolver<>(files, true);
                LoadingContext context = new LoadingContext(macroLoader);
                Map<ResourceLocation, Easing> easings = macroLoader.load((json, id) -> context.wrapStrict(id.toString(), () -> Easing.fromJSON(json, context)));
                List<String> errors = context.getErrors();
                if (!errors.isEmpty()) {
                    LOGGER.error(MARKER, "Error loading easing macros");
                    for (String i : errors) {
                        LOGGER.error(MARKER, i);
                    }
                }
                for (var i : easings.entrySet()) {
                    if (i.getValue() != null) {
                        Easing.EASING_MACROS.put(i.getKey(), i.getValue());
                    }
                }
                LOGGER.info(MARKER, "Loaded {}/{} easing macros", Easing.EASING_MACROS.size(), files.size());
            }
        }, ResourceLocation.parse("cutscenes:easing_macros"));
        abstractions.registerReloadListener(new JsonLoader(GSON, "cutscenes") {
            private static final Marker MARKER = MarkerFactory.getMarker("CutsceneLoader");

            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                CutsceneManager.REGISTRY.clear();
                PointProvider.POINT_CACHE.clear();
                AtomicInteger loaded = new AtomicInteger();
                LoadingContext context = new LoadingContext(null);
                files.forEach((id, element) -> {
                    try {
                        context.clear();
                        JsonObject json = GsonHelper.convertToJsonObject(element, "cutscene");
                        CutsceneType type = CutsceneType.fromJSON(json, context);
                        List<String> errors = context.getErrors();
                        if (errors.isEmpty()) {
                            CutsceneManager.registerCutscene(id, type);
                            loaded.getAndIncrement();
                        } else {
                            LOGGER.error(MARKER, "Failed to load cutscene {}:", id);
                            for (String i : errors) {
                                LOGGER.error(MARKER, i);
                            }
                        }
                    } catch (RuntimeException e) {
                        LOGGER.error(MARKER, "Exception loading cutscene {}", id);
                        LOGGER.error(MARKER, "Caused by: ", e);
                    }
                });
                LOGGER.info(MARKER, "Loaded {}/{} cutscenes", loaded.get(), files.size());
            }
        }, ResourceLocation.parse("cutscenes:cutscenes"));
    }
}
