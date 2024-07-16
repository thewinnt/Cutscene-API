package net.thewinnt.cutscenes.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientPlatformListener {
    // i can't put this to NeoForgeClientPlatform, because its superclass also has event listeners
    @SubscribeEvent
    public static void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraAngleSetterImpl impl = new CameraAngleSetterImpl(event);
        CutsceneAPINeoForgeClient.CLIENT_PLATFORM.angleSetters.forEach(consumer -> consumer.accept(impl));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        CutsceneAPINeoForgeClient.CLIENT_PLATFORM.clientTick.forEach(Runnable::run);
    }


    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CutsceneAPINeoForgeClient.CLIENT_PLATFORM.onLogout.forEach(Runnable::run);
    }
}
