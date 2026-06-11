package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.profiling.Profiler;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.mixin.GuiGraphicsAccessor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class FadeToColorOverlay implements Overlay {
    public static final FadeToColorOverlay INSTANCE = new FadeToColorOverlay();

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor graphics, int width, int height, Object config) {
        Profiler.get().push("cutscenes:fade");
        FadeToColorOverlayConfiguration cfg = ((FadeToColorOverlayConfiguration) config);
        ((GuiGraphicsAccessor) graphics).guiRenderState().addGuiElement(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                Matrix4f matrix4f = new Matrix4f().mul(graphics.pose());
                float[] colorBottomLeft = cfg.bottomLeft.sample(cfg.getProgress());
                float[] colorBottomRight = cfg.bottomRight.sample(cfg.getProgress());
                float[] colorTopLeft = cfg.topLeft.sample(cfg.getProgress());
                float[] colorTopRight = cfg.topRight.sample(cfg.getProgress());
                float alpha = cfg.getAlpha();
                consumer.addVertex(matrix4f, 0, height, 0).setColor(colorBottomLeft[0], colorBottomLeft[1], colorBottomLeft[2], colorBottomLeft[3] * alpha);
                consumer.addVertex(matrix4f, width, height, 0).setColor(colorBottomRight[0], colorBottomRight[1], colorBottomRight[2], colorBottomRight[3] * alpha);
                consumer.addVertex(matrix4f, width, 0, 0).setColor(colorTopRight[0], colorTopRight[1], colorTopRight[2], colorTopRight[3] * alpha);
                consumer.addVertex(matrix4f, 0, 0, 0).setColor(colorTopLeft[0], colorTopLeft[1], colorTopLeft[2], colorTopLeft[3] * alpha);
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public @Nullable ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public @Nullable ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, width, height);
            }
        });

//        graphics.drawString(minecraft.font, Component.literal("alpha " + alpha), 0, 0, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_bl " + alpha * colorBottomLeft[3]), 0, 9, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_tl " + alpha * colorTopLeft[3]), 0, 18, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_tr " + alpha * colorTopRight[3]), 0, 27, 16777215);
//        graphics.drawString(minecraft.font, Component.literal("alpha_br " + alpha * colorBottomRight[3]), 0, 36, 16777215);
//        graphics.drawString(MINECRAFT.font, Component.literal("time " + System.currentTimeMillis() / 2000.0), 0, 18, 16777215);
    }
}
