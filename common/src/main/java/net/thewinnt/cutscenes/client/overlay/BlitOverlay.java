package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CoreShaders;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.BlitConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;

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
        float z = config.z();
        graphics.drawSpecial(source -> {
            RenderType rendertype = RenderType.guiTextured(config.texture());
            Matrix4f matrix4f = graphics.pose().last().pose();
            VertexConsumer vertexconsumer = source.getBuffer(rendertype);
            vertexconsumer.addVertex(matrix4f, x1, y1, z).setUv(u1, v1).setColor(color);
            vertexconsumer.addVertex(matrix4f, x1, y2, z).setUv(u1, v2).setColor(color);
            vertexconsumer.addVertex(matrix4f, x2, y2, z).setUv(u2, v2).setColor(color);
            vertexconsumer.addVertex(matrix4f, x2, y1, z).setUv(u2, v1).setColor(color);
        });
    }
}
