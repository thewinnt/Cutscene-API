package net.thewinnt.cutscenes.forge;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientPlatformListener {
    // i can't put this to ForgeClientPlatform, because its superclass also has event listeners
    @SubscribeEvent
    public static void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraAngleSetterImpl impl = new CameraAngleSetterImpl(event);
        CutsceneAPIForgeClient.CLIENT_PLATFORM.angleSetters.forEach(consumer -> consumer.accept(impl));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            CutsceneAPIForgeClient.CLIENT_PLATFORM.clientTick.forEach(Runnable::run);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CutsceneAPIForgeClient.CLIENT_PLATFORM.onLogout.forEach(Runnable::run);
    }
}
