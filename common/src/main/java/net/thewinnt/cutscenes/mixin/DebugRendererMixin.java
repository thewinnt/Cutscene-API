package net.thewinnt.cutscenes.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.thewinnt.cutscenes.client.preview.PathPreviewRenderer;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void beforeDebugRender(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        Minecraft.getInstance().getProfiler().push("cutscene_preview");
        PathPreviewRenderer.beforeDebugRender(poseStack, consumer);
        Minecraft.getInstance().getProfiler().pop();
    }
}
