package net.thewinnt.cutscenes.client.overlay;

import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.profiling.Profiler;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.SimpleTextConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;

public class SimpleTextOverlay implements Overlay {
    private final SimpleTextConfiguration config;

    public SimpleTextOverlay(SimpleTextConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object cfg) {
        Profiler.get().push("cutscenes:text");
        graphics.pose().pushPose();
        TimeProvider time = ((TimeProvider) cfg);
        double progress = time.getProgress();

        float scale = (float) config.scale().get(progress);
        float rotation = (float) config.rotation().get(progress);
        int x = (int)config.rx().get(progress, width / scale);
        int y = (int)config.ry().get(progress, height / scale);

        graphics.pose().scale(scale, scale, scale);
        graphics.pose().translate(x, y, 0);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
        if (config.centered()) {
            y -= (int) (minecraft.font.lineHeight * minecraft.getWindow().getGuiScale() / (2.0 * scale));
        }
        int color;
        if (config.colorOverride().isPresent()) {
            color = config.colorOverride().get().toARGB(progress);
        } else {
            color = -1;
        }
        graphics.drawCenteredString(minecraft.font, config.text(), 0, 0, color);
        graphics.pose().popPose();
        Profiler.get().pop();
    }
}
