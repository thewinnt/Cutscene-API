package net.thewinnt.cutscenes.fabric;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.thewinnt.cutscenes.platform.ClientPlatformAbstractions;

public class FabricClientPlatform extends FabricPlatform implements ClientPlatformAbstractions {
    public static final RenderType TRIANGLE_STRIP = RenderType.create(
        "cutscenes:triangle_strip",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLE_STRIP,
        1536,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_GUI_SHADER)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false)
    );

    @Override
    public RenderType getTriangleStrip() {
        return TRIANGLE_STRIP;
    }

    @Override
    public void submitOnLogout(Runnable runnable) {
        ClientPlayConnectionEvents.DISCONNECT.register((clientHandshakePacketListener, minecraft) -> runnable.run());
    }
}
