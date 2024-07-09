package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.BlitConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;
import org.joml.Matrix4f;

public class BlitOverlay implements Overlay {
    private final BlitConfiguration config;

    public BlitOverlay(BlitConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object cfg) {
        TimeProvider time = (TimeProvider) cfg;
        double t = time.getProgress();
        float x1 = config.x1().get(t, width);
        float y1 = config.y1().get(t, height);
        float x2 = config.x2().get(t, width);
        float y2 = config.y2().get(t, height);
        float u1 = config.u1().get(t, 1);
        float v1 = config.v1().get(t, 1);
        float u2 = config.u2().get(t, 1);
        float v2 = config.v2().get(t, 1);
        int color = config.tint().toARGB(t);
        RenderSystem.setShaderTexture(0, config.texture());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix4f, x1, y1, 0).color(color).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, x1, y2, 0).color(color).uv(u1, v2).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y2, 0).color(color).uv(u2, v2).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y1, 0).color(color).uv(u2, v1).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }
}
