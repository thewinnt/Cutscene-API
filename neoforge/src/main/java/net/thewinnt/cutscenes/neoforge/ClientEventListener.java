package net.thewinnt.cutscenes.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventListener {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CutsceneAPI.platform().getWaypointEntityType(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(Identifier.parse("cutscenes:overlays"), (graphics, deltaTracker) -> {
            CutsceneOverlayManager.render(Minecraft.getInstance(), graphics, graphics.guiWidth(), graphics.guiHeight());
        });
    }

    // because neoforge doesn't see my @Mod annotation on the client class
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        CutsceneAPINeoForgeClient.init();
    }
}
