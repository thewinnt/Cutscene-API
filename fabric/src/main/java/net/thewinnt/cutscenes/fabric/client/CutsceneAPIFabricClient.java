package net.thewinnt.cutscenes.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.Identifier;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.ClientServices;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.fabric.CutsceneAPIFabric;
import net.thewinnt.cutscenes.fabric.FabricClientPlatform;
import net.thewinnt.cutscenes.fabric.FabricPlatform;

public final class CutsceneAPIFabricClient implements ClientModInitializer {
    public static final FabricClientPlatform CLIENT_PLATFORM = ((FabricClientPlatform) ClientServices.PLATFORM);

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        FabricPlatform platform = CutsceneAPIFabric.PLATFORM;
        platform.clientboundPackets.forEach(type -> {
            ClientPlayNetworking.registerGlobalReceiver(type.type(), (packet, context) -> {
                context.client().executeBlocking(packet::execute);
            });
        });
        CutsceneAPI.onInitializeClient(CLIENT_PLATFORM);
        EntityRenderers.register(CutsceneAPIFabric.WAYPOINT, NoopRenderer::new);
        HudElementRegistry.addFirst(Identifier.parse("cutscenes:overlays"), (GuiGraphicsExtractor, _) -> {
            CutsceneOverlayManager.render(Minecraft.getInstance(), GuiGraphicsExtractor, GuiGraphicsExtractor.guiWidth(), GuiGraphicsExtractor.guiHeight());
        });
    }
}
