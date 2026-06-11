package net.thewinnt.cutscenes.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.profiling.Profiler;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.SimpleTextConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;
import org.joml.Matrix3x2fStack;

public class SimpleTextOverlay implements Overlay {
    private final SimpleTextConfiguration config;

    public SimpleTextOverlay(SimpleTextConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor graphics, int width, int height, Object cfg) {
        Profiler.get().push("cutscenes:text");
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        TimeProvider time = ((TimeProvider) cfg);
        double progress = time.getProgress();

        float scale = (float) config.scale().get(progress);
        float rotation = (float) config.rotation().get(progress);
        int x = (int)config.rx().get(progress, width / scale);
        int y = (int)config.ry().get(progress, height / scale);
        if (config.centered()) {
            y -= (int) (minecraft.font.lineHeight * minecraft.getWindow().getGuiScale() / (2.0 * scale));
        }

        pose.scale(scale, scale);
        pose.translate(x, y);
        pose.rotate(rotation);
        int color;
        if (config.colorOverride().isPresent()) {
            color = config.colorOverride().get().toARGB(progress);
        } else {
            color = -1;
        }
        graphics.centeredText(minecraft.font, config.text(), 0, 0, color);
        pose.popMatrix();
        Profiler.get().pop();
    }
}
