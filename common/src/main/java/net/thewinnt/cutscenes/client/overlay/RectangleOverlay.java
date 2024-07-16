package net.thewinnt.cutscenes.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.RectangleConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;

public class RectangleOverlay implements Overlay {
    private final RectangleConfiguration config;

    public RectangleOverlay(RectangleConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object cfg) {
        minecraft.getProfiler().push("cutscenes:rectangle");
        TimeProvider time = (TimeProvider) cfg;
        double t = time.getProgress();
        int x = (int) config.x().get(t, width);
        int y = (int) config.y().get(t, height);
        int rwidth = (int) config.width().get(t, width);
        int rheight = (int) config.height().get(t, height);
        int color1 = config.colorTop().toARGB(t);
        int color2;
        if (config.colorTop() == config.colorBottom()) {
            color2 = color1;
        } else {
            color2 = config.colorBottom().toARGB(t);
        }
        graphics.fillGradient(x, y, x + rwidth, y + rheight, color1, color2);
        minecraft.getProfiler().pop();
    }
}
