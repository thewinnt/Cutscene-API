package net.thewinnt.cutscenes.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.BlitConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;

public class BlitOverlay implements Overlay {
    private final BlitConfiguration config;

    public BlitOverlay(BlitConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor graphics, int width, int height, Object cfg) {
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
        graphics.blit(RenderPipelines.GUI_TEXTURED, config.texture(), (int) x1, (int) y1, u1 * 1000000, v1 * 1000000, (int)(x2 - x1), (int)(y2 - y1), (int)((u2 - u1) * 1000000), (int)((v2 - v1) * 1000000), 1000000, 1000000, color);
    }
}
