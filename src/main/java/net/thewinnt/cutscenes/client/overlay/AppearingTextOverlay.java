package net.thewinnt.cutscenes.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect.TimeProvider;
import org.jetbrains.annotations.NotNull;

public class AppearingTextOverlay implements Overlay {
    private final AppearingTextConfiguration config;
    private String lastString = "";

    public AppearingTextOverlay(AppearingTextConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config) {
        TimeProvider time = (TimeProvider) config;
        String text = this.config.text().getString();
        double currentTime = time.getTime();
        String toRender = createStringForFrame(text, currentTime);
        if (lastString.length() != toRender.length() && !Character.isWhitespace(toRender.charAt(toRender.length() - 1))) {
            CutsceneAPI.LOGGER.debug("would've played a sound here");
            // TODO play sound
        }
        lastString = toRender;
        int x = (int)(this.config.rx().get(time.getProgress()) * width);
        int y = (int)(this.config.ry().get(time.getProgress()) * height);
        String[] lines = toRender.split("\n");
        for (int i = 0; i < lines.length; i++) {
            graphics.drawString(minecraft.font, lines[i], x, y + minecraft.font.lineHeight * i, 0xffffff);
        }
    }

    private @NotNull String createStringForFrame(String text, double currentTime) {
        StringBuilder toRender = new StringBuilder();
        double t = 0;
        for (int i = 0; i < text.length(); i++) {
            if (t >= currentTime) break;
            char c = text.charAt(i);
            if (c == '^') {
                t += 6.66;
            } else if (c == '\\' && text.charAt(i + 1) == '^') {
                t += 1.33;
                toRender.append('^');
            } else {
                if (c != ' ' && c != '\n') {
                    t += 1.33;
                }
                toRender.append(c);
            }
        }
        return toRender.toString();
    }
}
