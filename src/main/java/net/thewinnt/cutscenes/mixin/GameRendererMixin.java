package net.thewinnt.cutscenes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.thewinnt.cutscenes.client.FadeToColorOverlay.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V", shift = Shift.BEFORE))
    private void render(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci, @Local GuiGraphics guigraphics) {
        this.minecraft.getProfiler().push("cutscene_overlay");
        if (ClientCutsceneManager.isCutsceneRunning()) {
            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();
            Matrix4f matrix4f = guigraphics.pose().last().pose();
            VertexConsumer builder = guigraphics.bufferSource().getBuffer(RenderType.gui());
            builder.vertex(matrix4f, 0, height, 0).color(colorBottomLeft[0], colorBottomLeft[1], colorBottomLeft[2], colorBottomLeft[3] * alpha).endVertex();
            builder.vertex(matrix4f, width, height, 0).color(colorBottomRight[0], colorBottomRight[1], colorBottomRight[2], colorBottomRight[3] * alpha).endVertex();
            builder.vertex(matrix4f, width, 0, 0).color(colorTopRight[0], colorTopRight[1], colorTopRight[2], colorTopRight[3] * alpha).endVertex();
            builder.vertex(matrix4f, 0, 0, 0).color(colorTopLeft[0], colorTopLeft[1], colorTopLeft[2], colorTopLeft[3] * alpha).endVertex();

            guigraphics.drawString(minecraft.font, Component.literal("alpha " + alpha), 0, 0, 16777215);
            guigraphics.drawString(minecraft.font, Component.literal("alpha_bl " + alpha * colorBottomLeft[3]), 0, 9, 16777215);
            guigraphics.drawString(minecraft.font, Component.literal("alpha_tl " + alpha * colorTopLeft[3]), 0, 18, 16777215);
            guigraphics.drawString(minecraft.font, Component.literal("alpha_tr " + alpha * colorTopRight[3]), 0, 27, 16777215);
            guigraphics.drawString(minecraft.font, Component.literal("alpha_br " + alpha * colorBottomRight[3]), 0, 36, 16777215);
//            guigraphics.drawString(MINECRAFT.font, Component.literal("time " + System.currentTimeMillis() / 2000.0), 0, 18, 16777215);
        }
        this.minecraft.getProfiler().pop();
    }
}
