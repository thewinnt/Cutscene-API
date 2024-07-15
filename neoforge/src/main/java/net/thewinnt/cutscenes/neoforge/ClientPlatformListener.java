package net.thewinnt.cutscenes.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientPlatformListener {
    // i can't put this to NeoForgeClientPlatform, because its superclass also has event listeners
    @SubscribeEvent
    public static void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraAngleSetterImpl impl = new CameraAngleSetterImpl(event);
        CutsceneAPINeoForgeClient.CLIENT_PLATFORM.angleSetters.forEach(consumer -> consumer.accept(impl));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            CutsceneAPINeoForgeClient.CLIENT_PLATFORM.clientTick.forEach(Runnable::run);
        }
    }
}
