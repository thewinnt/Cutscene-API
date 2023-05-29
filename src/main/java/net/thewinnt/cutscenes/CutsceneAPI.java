package net.thewinnt.cutscenes;

import java.util.Map;

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;

/** The main class of Cutscene API. Sort of. */
@Mod("cutscene_api")
public class CutsceneAPI {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "cutscenes");
    public static final RegistryObject<EntityType<?>> WAYPOINT = ENTITIES.register("waypoint", () -> EntityType.Builder.of(WaypointEntity::new, MobCategory.MISC).sized(0.1f, 0.1f).clientTrackingRange(9999).setTrackingRange(9999).canSpawnFarFromPlayer().build("waypoint"));

    public CutsceneAPI() {
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "line"), CutsceneManager.LINE);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "bezier"), CutsceneManager.BEZIER);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "catmull_rom"), CutsceneManager.CATMULL_ROM);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "path"), CutsceneManager.PATH);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "constant"), CutsceneManager.CONSTANT);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "look_at_point"), CutsceneManager.LOOK_AT_POINT);
        CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "transition"), CutsceneManager.TRANSITION);
        CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "static"), CutsceneManager.STATIC);
        CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "waypoint"), CutsceneManager.WAYPOINT);
        CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "world"), CutsceneManager.WORLD);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(CutsceneAPI.class);
        ENTITIES.register(modEventBus);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimpleJsonResourceReloadListener(new GsonBuilder().create(), "cutscenes") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller filler) {
                CutsceneManager.REGISTRY.clear();
                files.forEach((id, element) -> {
                    JsonObject json = GsonHelper.convertToJsonObject(element, "cutscene");
                    CutsceneManager.registerCutscene(id, CutsceneType.fromJSON(json));
                });
            }
        });
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        CutsceneNetworkHandler.register();
    }

    @Mod.EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
    public static class ClientEventListener {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CutsceneAPI.WAYPOINT.get(), NoopRenderer::new);
        }
    }
}
