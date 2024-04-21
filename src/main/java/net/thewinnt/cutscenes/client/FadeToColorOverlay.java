package net.thewinnt.cutscenes.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class FadeToColorOverlay {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static float[] colorBottomLeft = new float[]{1, 0, 0, 1};
    private static float[] colorTopLeft = new float[]{0, 1, 0, 1};
    private static float[] colorTopRight = new float[]{0, 0, 1, 1};
    private static float[] colorBottomRight = new float[]{1, 1, 1, 1};
    private static float alpha = 0;

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        GuiGraphics graphics = event.getGuiGraphics();

        // alpha = (float)(Math.sin(System.currentTimeMillis() / 2000.0) + 1) / 2f;

        if (ClientCutsceneManager.isCutsceneRunning()) {
            Matrix4f matrix4f = graphics.pose().last().pose();
            VertexConsumer builder = graphics.bufferSource().getBuffer(RenderType.gui());
            builder.vertex(matrix4f, 0, height, -90).color(colorBottomLeft[0], colorBottomLeft[1], colorBottomLeft[2], colorBottomLeft[3] * alpha).endVertex();
            builder.vertex(matrix4f, width, height, -90).color(colorBottomRight[0], colorBottomRight[1], colorBottomRight[2], colorBottomRight[3] * alpha).endVertex();
            builder.vertex(matrix4f, width, 0, -90).color(colorTopRight[0], colorTopRight[1], colorTopRight[2], colorTopRight[3] * alpha).endVertex();
            builder.vertex(matrix4f, 0, 0, -90).color(colorTopLeft[0], colorTopLeft[1], colorTopLeft[2], colorTopLeft[3] * alpha).endVertex();

            graphics.drawString(MINECRAFT.font, Component.literal("alpha " + alpha), 0, 0, 16777215);
            graphics.drawString(MINECRAFT.font, Component.literal("alpha_bl " + alpha * colorBottomLeft[3]), 0, 9, 16777215);
            graphics.drawString(MINECRAFT.font, Component.literal("alpha_tl " + alpha * colorTopLeft[3]), 0, 18, 16777215);
            graphics.drawString(MINECRAFT.font, Component.literal("alpha_tr " + alpha * colorTopRight[3]), 0, 27, 16777215);
            graphics.drawString(MINECRAFT.font, Component.literal("alpha_br " + alpha * colorBottomRight[3]), 0, 36, 16777215);
//            graphics.drawString(MINECRAFT.font, Component.literal("time " + System.currentTimeMillis() / 2000.0), 0, 18, 16777215);
        }
    }

    public static void setAlpha(float alpha) {
        FadeToColorOverlay.alpha = alpha;
    }

    public static void setColorBottomLeft(float[] colorBottomLeft) {
        FadeToColorOverlay.colorBottomLeft = colorBottomLeft;
    }

    public static void setColorTopLeft(float[] colorTopLeft) {
        FadeToColorOverlay.colorTopLeft = colorTopLeft;
    }

    public static void setColorTopRight(float[] colorTopRight) {
        FadeToColorOverlay.colorTopRight = colorTopRight;
    }

    public static void setColorBottomRight(float[] colorBottomRight) {
        FadeToColorOverlay.colorBottomRight = colorBottomRight;
    }

    public static void setColors(float[] bottomLeft, float[] topLeft, float[] topRight, float[] bottomRight) {
        FadeToColorOverlay.colorBottomLeft = bottomLeft;
        FadeToColorOverlay.colorTopLeft = topLeft;
        FadeToColorOverlay.colorTopRight = topRight;
        FadeToColorOverlay.colorBottomRight = bottomRight;
    }
}
