package net.thewinnt.cutscenes;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.util.LoadResolver;
import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thewinnt.cutscenes.entity.WaypointEntity;

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

    // registries
    public static final Registry<EasingSerializer<?>> EASING_SERIALIZERS = new RegistryBuilder<>(EASING_SERIALIZER_KEY)
        .sync(true)
        .defaultKey(new ResourceLocation("cutscenes:linear"))
        .create();

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "cutscenes");
    public static final DeferredHolder<EntityType<?>, EntityType<WaypointEntity>> WAYPOINT = ENTITIES.register("waypoint", () -> EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC).sized(0.1f, 0.1f).clientTrackingRange(9999).setTrackingRange(9999).canSpawnFarFromPlayer().build("waypoint"));

    public CutsceneAPI(IEventBus modBus, Dist dist) {
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
        NeoForge.EVENT_BUS.register(CutsceneAPI.class);
        ENTITIES.register(modBus);
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
                        LOGGER.error("Caused by:", e);
                    }
                });
                LOGGER.info("Loaded {} cutscenes", loaded.get());
            }
        });
    }
}
