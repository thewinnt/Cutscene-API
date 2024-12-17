package net.thewinnt.cutscenes.forge;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.thewinnt.cutscenes.CutsceneAPI;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventListener {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CutsceneAPI.platform().getWaypointEntityType(), NoopRenderer::new);
    }

    // because neoforge doesn't see my @Mod annotation on the client class
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        CutsceneAPIForgeClient.init();
    }
}
