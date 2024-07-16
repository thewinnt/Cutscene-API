package net.thewinnt.cutscenes.neoforge;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.thewinnt.cutscenes.CutsceneAPI;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventListener {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CutsceneAPI.platform().getWaypointEntityType(), NoopRenderer::new);
    }

    // because neoforge doesn't see my @Mod annotation on the client class
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        CutsceneAPINeoForgeClient.init();
    }
}
