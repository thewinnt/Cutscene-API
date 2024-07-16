package net.thewinnt.cutscenes.neoforge.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor(value = "RENDERTYPE_GUI_SHADER")
    static RenderStateShard.ShaderStateShard sc$getGuiShader() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "TRANSLUCENT_TRANSPARENCY")
    static RenderStateShard.TransparencyStateShard sc$getTranslucentTransparency() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "LEQUAL_DEPTH_TEST")
    static RenderStateShard.DepthTestStateShard sc$getLEqualDepthTest() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "NO_CULL")
    static RenderStateShard.CullStateShard sc$getNoCull() {
        throw new UnsupportedOperationException();
    }
}