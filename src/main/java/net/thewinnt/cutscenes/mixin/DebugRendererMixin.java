package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.thewinnt.cutscenes.client.preview.PathPreviewRenderer;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void beforeDebugRender(PoseStack stack, MultiBufferSource.BufferSource source, double x, double y, double z, CallbackInfo callback) {
        VertexConsumer consumer = source.getBuffer(RenderType.lines());
        PathPreviewRenderer.beforeDebugRender(stack, consumer);
    }
}
