package net.thewinnt.cutscenes.fabric;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;

public class FabricClientPlatform extends FabricPlatform {
    @Override
    public void submitOnLogout(Runnable runnable) {
        ClientPlayConnectionEvents.DISCONNECT.register((clientHandshakePacketListener, minecraft) -> runnable.run());
    }
}
