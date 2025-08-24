package net.thewinnt.cutscenes.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
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
        Profiler.get().push("cutscenes:animation");
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

        graphics.blit(RenderPipelines.GUI_TEXTURED, frames[frame], (int) x1, (int) y1, u1, v1, (int)(x2 - x1), (int)(y2 - y1), (int)(u2 - u1), (int)(v2 - v1), 1, 1, color);
    }
}
