package net.thewinnt.cutscenes.neoforge;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.thewinnt.cutscenes.CutsceneAPI;

@Mod.EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventListener {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CutsceneAPI.platform().getWaypointEntityType(), NoopRenderer::new);
    }
}
