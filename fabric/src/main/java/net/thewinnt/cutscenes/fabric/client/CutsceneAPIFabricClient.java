package net.thewinnt.cutscenes.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.fabric.CutsceneAPIFabric;
import net.thewinnt.cutscenes.fabric.FabricClientPlatform;
import net.thewinnt.cutscenes.fabric.FabricPlatform;

public final class CutsceneAPIFabricClient implements ClientModInitializer {
    public static final FabricClientPlatform CLIENT_PLATFORM = new FabricClientPlatform();

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
        EntityRendererRegistry.register(CutsceneAPIFabric.WAYPOINT, NoopRenderer::new);
        HudElementRegistry.addFirst(ResourceLocation.parse("cutscenes:overlays"), (guiGraphics, deltaTracker) -> {
            CutsceneOverlayManager.render(Minecraft.getInstance(), guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        });
    }
}
