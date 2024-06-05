package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.thewinnt.cutscenes.client.Overlay;
import org.joml.Matrix4f;

public class FadeToColorOverlay implements Overlay {
    public static final FadeToColorOverlay INSTANCE = new FadeToColorOverlay();

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config) {
        minecraft.getProfiler().push("cutscenes:fade");
        FadeToColorOverlayConfiguration cfg = ((FadeToColorOverlayConfiguration) config);
        Matrix4f matrix4f = graphics.pose().last().pose();
        VertexConsumer builder = graphics.bufferSource().getBuffer(RenderType.gui());
        float[] colorBottomLeft = cfg.bottomLeft.sample(cfg.getProgress());
        float[] colorBottomRight = cfg.bottomRight.sample(cfg.getProgress());
        float[] colorTopLeft = cfg.topLeft.sample(cfg.getProgress());
        float[] colorTopRight = cfg.topRight.sample(cfg.getProgress());
        float alpha = cfg.getAlpha();
        builder.vertex(matrix4f, 0, height, 0).color(colorBottomLeft[0], colorBottomLeft[1], colorBottomLeft[2], colorBottomLeft[3] * alpha).endVertex();
        builder.vertex(matrix4f, width, height, 0).color(colorBottomRight[0], colorBottomRight[1], colorBottomRight[2], colorBottomRight[3] * alpha).endVertex();
        builder.vertex(matrix4f, width, 0, 0).color(colorTopRight[0], colorTopRight[1], colorTopRight[2], colorTopRight[3] * alpha).endVertex();
        builder.vertex(matrix4f, 0, 0, 0).color(colorTopLeft[0], colorTopLeft[1], colorTopLeft[2], colorTopLeft[3] * alpha).endVertex();
        minecraft.getProfiler().pop();

//        graphics.drawString(minecraft.font, Component.literal("alpha " + alpha), 0, 0, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_bl " + alpha * colorBottomLeft[3]), 0, 9, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_tl " + alpha * colorTopLeft[3]), 0, 18, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_tr " + alpha * colorTopRight[3]), 0, 27, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_br " + alpha * colorBottomRight[3]), 0, 36, 16777215);
//        graphics.drawString(MINECRAFT.font, Component.literal("time " + System.currentTimeMillis() / 2000.0), 0, 18, 16777215);
    }
}
