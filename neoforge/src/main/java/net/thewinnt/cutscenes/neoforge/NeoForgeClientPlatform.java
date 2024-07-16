package net.thewinnt.cutscenes.neoforge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.thewinnt.cutscenes.neoforge.mixin.RenderStateShardAccessor;
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
            .setShaderState(RenderStateShardAccessor.sc$getGuiShader())
            .setTransparencyState(RenderStateShardAccessor.sc$getTranslucentTransparency())
            .setDepthTestState(RenderStateShardAccessor.sc$getLEqualDepthTest())
            .setCullState(RenderStateShardAccessor.sc$getNoCull())
            .createCompositeState(false)
    );

    @Override
    public RenderType getTriangleStrip() {
        return TRIANGLE_STRIP;
    }
}
