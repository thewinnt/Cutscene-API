package net.thewinnt.cutscenes.client.overlay;

import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.TextureAnimationConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;

public class TextureAnimationOverlay implements Overlay {
    private final TextureAnimationConfiguration config;
    private final ResourceLocation[] frames;

    public TextureAnimationOverlay(TextureAnimationConfiguration config) {
        this.config = config;
        this.frames = new ResourceLocation[config.frameCount()];
        for (int i = 0; i < frames.length; i++) {
            String texture = config.textureMask();
            String[] frameFormat = texture.split("%");
            texture = texture.replaceAll("%[0-9]*%", String.format("%0" + frameFormat[1] + "d", i + config.frameOffset()));
            this.frames[i] = ResourceLocation.parse(texture);
        }
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object cfg) {
        Minecraft.getInstance().getProfiler().push("cutscenes:animation");
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
        int frame = (int) (Mth.clamp(config.timeWarp().get(time.getProgress()), 0, 1) * config.frameCount());

        RenderSystem.setShaderTexture(0, frames[frame]);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        
        bufferbuilder.addVertex(matrix4f, x1, y1, config.zIndex()).setColor(color).setUv(u1, v1);
        bufferbuilder.addVertex(matrix4f, x1, y2, config.zIndex()).setColor(color).setUv(u1, v2);
        bufferbuilder.addVertex(matrix4f, x2, y2, config.zIndex()).setColor(color).setUv(u2, v2);
        bufferbuilder.addVertex(matrix4f, x2, y1, config.zIndex()).setColor(color).setUv(u2, v1);

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
        Minecraft.getInstance().getProfiler().pop();
    }
}
