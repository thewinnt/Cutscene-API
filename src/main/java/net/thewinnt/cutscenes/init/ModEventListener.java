package net.thewinnt.cutscenes.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.EasingSerializer;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class ModEventListener {
    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(CutsceneAPI.EASING_SERIALIZERS);
    }

    @SubscribeEvent
    public static void registerElements(RegisterEvent event) {
        EasingSerializer.init();
    }
}
