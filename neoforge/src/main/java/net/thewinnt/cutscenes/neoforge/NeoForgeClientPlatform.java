package net.thewinnt.cutscenes.neoforge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.thewinnt.cutscenes.platform.ClientPlatformAbstractions;

public class NeoForgeClientPlatform extends NeoForgePlatform implements ClientPlatformAbstractions {
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
}
