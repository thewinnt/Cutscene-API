package net.thewinnt.cutscenes.forge;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.platform.AbstractPacket;
import net.thewinnt.cutscenes.rotation.RotationSerializer;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventListener {
    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        // register stuff
        CutsceneAPIForge.PLATFORM.registries.forEach((key, setter) -> event.create(
            new RegistryBuilder<>().setName(key.location()).hasTags(),
            objects -> apply(setter, key))
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void apply(Consumer<T> setter, ResourceKey key) {
        setter.accept((T)BuiltInRegistries.REGISTRY.get(key));
    }

    @SubscribeEvent
    public static void registerStuff(RegisterEvent event) {
        if (event.getRegistryKey() == CutsceneAPI.SEGMENT_TYPE_KEY) {
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "line"), CutsceneManager.LINE);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "bezier"), CutsceneManager.BEZIER);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "catmull_rom"), CutsceneManager.CATMULL_ROM);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "path"), CutsceneManager.PATH);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "constant"), CutsceneManager.CONSTANT);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "look_at_point"), CutsceneManager.LOOK_AT_POINT);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "transition"), CutsceneManager.PATH_TRANSITION);
            CutsceneManager.registerSegmentType(new ResourceLocation("cutscenes", "calculated"), CutsceneManager.CALCULATED_POINT);
        } else if (event.getRegistryKey() == CutsceneAPI.POINT_TYPE_KEY) {
            CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "static"), CutsceneManager.STATIC);
            CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "waypoint"), CutsceneManager.WAYPOINT);
            CutsceneManager.registerPointType(new ResourceLocation("cutscenes", "world"), CutsceneManager.WORLD);
        } else if (event.getRegistryKey() == CutsceneAPI.TRANSITION_TYPE_KEY) {
            CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "no_op"), CutsceneManager.NO_OP);
            CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "smooth_ease"), CutsceneManager.SMOOTH_EASE);
            CutsceneManager.registerTransitionType(new ResourceLocation("cutscenes", "fade"), CutsceneManager.FADE);
        } else {
            EasingSerializer.init();
            CutsceneEffectSerializer.init();
            DelayProviderSerializer.init();
            RotationSerializer.init();
            // TODO fix kick from server
        }
    }
}
