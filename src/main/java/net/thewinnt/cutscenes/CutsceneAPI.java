package net.thewinnt.cutscenes;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.init.CutsceneAPIEntities;
import net.thewinnt.cutscenes.path.PathLike.SegmentSerializer;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;
import net.thewinnt.cutscenes.transition.Transition.TransitionSerializer;
import net.thewinnt.cutscenes.util.LoadResolver;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/** The main class of Cutscene API. Sort of. */
@Mod("cutscene_api")
public class CutsceneAPI {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random RANDOM = new Random();
    /** 
     * A salt value, updated each time a cutscene is started. Used for randomizing waypoint locations.
     * @see net.thewinnt.cutscenes.path.point.WaypointProvider#getPoint(Level, Vec3)
     */
    private static long WAYPOINT_SALT = RANDOM.nextLong();

    // registry keys
    public static final ResourceKey<Registry<EasingSerializer<?>>> EASING_SERIALIZER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:easing_types"));
    public static final ResourceKey<Registry<CutsceneEffectSerializer<?>>> CUTSCENE_EFFECT_SERIALIZER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:effect_serializers"));
    public static final ResourceKey<Registry<SegmentSerializer<?>>> SEGMENT_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:segment_types"));
    public static final ResourceKey<Registry<PointSerializer<?>>> POINT_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:point_providers"));
    public static final ResourceKey<Registry<TransitionSerializer<?>>> TRANSITION_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:transition_types"));
    public static final ResourceKey<Registry<DelayProviderSerializer<?>>> DELAY_PROVIDER_KEY = ResourceKey.createRegistryKey(new ResourceLocation("cutscenes:delay_providers"));

    // registries
    public static final Registry<EasingSerializer<?>> EASING_SERIALIZERS = new RegistryBuilder<>(EASING_SERIALIZER_KEY)
        .sync(true)
        .defaultKey(new ResourceLocation("cutscenes:linear"))
        .create();

    public static final Registry<CutsceneEffectSerializer<?>> CUTSCENE_EFFECT_SERIALIZERS = new RegistryBuilder<>(CUTSCENE_EFFECT_SERIALIZER_KEY)
        .sync(true)
        .create();

    public static final Registry<SegmentSerializer<?>> SEGMENT_TYPES = new RegistryBuilder<>(SEGMENT_TYPE_KEY)
        .sync(true)
        .create();

    public static final Registry<PointSerializer<?>> POINT_TYPES = new RegistryBuilder<>(POINT_TYPE_KEY)
        .sync(true)
        .create();

    public static final Registry<TransitionSerializer<?>> TRANSITION_TYPES = new RegistryBuilder<>(TRANSITION_TYPE_KEY)
        .sync(true)
        .defaultKey(new ResourceLocation("cutscenes:no_op"))
        .create();

    public static final Registry<DelayProviderSerializer<?>> DELAY_PROVIDERS = new RegistryBuilder<>(DELAY_PROVIDER_KEY)
        .sync(true)
        .defaultKey(new ResourceLocation("cutscenes:undertale"))
        .create();

    public CutsceneAPI(IEventBus modBus, Dist dist) {
        NeoForge.EVENT_BUS.register(CutsceneAPI.class);
        CutsceneAPIEntities.ENTITIES.register(modBus);
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

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimpleJsonResourceReloadListener(new GsonBuilder().create(), "easing_macros") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                Easing.EASING_MACROS.clear();
                LoadResolver<Easing> macroLoader = new LoadResolver<>(Easing::fromJSON, files, true);
                Easing.EASING_MACROS.putAll(macroLoader.load());
                LOGGER.info("Loaded {} easing macros", Easing.EASING_MACROS.size());
            }
        });
        event.addListener(new SimpleJsonResourceReloadListener(new GsonBuilder().create(), "cutscenes") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                CutsceneManager.REGISTRY.clear();
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
        });
    }
}
