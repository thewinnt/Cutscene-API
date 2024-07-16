package net.thewinnt.cutscenes.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModEventListener {
    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("cutscenes").versioned("1.5.2");
        CutsceneAPINeoForge.PLATFORM.packets.forEach(type -> NeoForgePlatform.registerPacket(registrar, type));
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        // register stuff
        event.register(CutsceneAPI.EASING_SERIALIZERS);
        event.register(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS);
        event.register(CutsceneAPI.SEGMENT_TYPES);
        event.register(CutsceneAPI.POINT_TYPES);
        event.register(CutsceneAPI.TRANSITION_TYPES);
        event.register(CutsceneAPI.DELAY_PROVIDERS);
    }

    @SubscribeEvent
    public static void registerStuff(RegisterEvent event) {
        if (event.getRegistry() == CutsceneAPI.SEGMENT_TYPES) {
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "line"), CutsceneManager.LINE);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "bezier"), CutsceneManager.BEZIER);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "catmull_rom"), CutsceneManager.CATMULL_ROM);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "path"), CutsceneManager.PATH);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "constant"), CutsceneManager.CONSTANT);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "look_at_point"), CutsceneManager.LOOK_AT_POINT);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "transition"), CutsceneManager.PATH_TRANSITION);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "calculated"), CutsceneManager.CALCULATED_POINT);
        } else if (event.getRegistry() == CutsceneAPI.POINT_TYPES) {
            CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "static"), CutsceneManager.STATIC);
            CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "waypoint"), CutsceneManager.WAYPOINT);
            CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "world"), CutsceneManager.WORLD);
        } else if (event.getRegistry() == CutsceneAPI.TRANSITION_TYPES) {
            CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "no_op"), CutsceneManager.NO_OP);
            CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "smooth_ease"), CutsceneManager.SMOOTH_EASE);
            CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "fade"), CutsceneManager.FADE);
        } else {
            EasingSerializer.init();
            CutsceneEffectSerializer.init();
            DelayProviderSerializer.init();
        }
    }
}
