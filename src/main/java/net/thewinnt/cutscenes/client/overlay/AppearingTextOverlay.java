package net.thewinnt.cutscenes.client.overlay;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect.TimeProvider;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AppearingTextOverlay implements Overlay {
    private final AppearingTextConfiguration config;
    private double lastT = 0;

    public AppearingTextOverlay(AppearingTextConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config) {
        TimeProvider time = (TimeProvider) config;
        Component text = this.config.text();
        // list 1: each component split into formatted strings
        List<Pair<String, Style>> styles = new ArrayList<>();
        double currentTime = time.getTime();
        var state = new Object() {
            double t = 0;
            boolean didJustEscape = false;
            boolean didJustAdd = false;
            Style style;
            StringBuilder currentString;
        };
        text.getVisualOrderText().accept((index, nextStyle, codepoint) -> {
            if (state.t >= currentTime) return false;
            if (!nextStyle.equals(state.style)) {
                if (state.style != null && state.currentString != null) {
                    styles.add(Pair.of(state.currentString.toString(), state.style));
                    state.didJustAdd = true;
                }
                state.style = nextStyle;
                state.currentString = new StringBuilder();
            } else {
                state.didJustAdd = false;
            }
            if (codepoint == '^' && !state.didJustEscape) {
                state.t += 6.66;
            } else if (codepoint == '\\' && !state.didJustEscape) {
                state.didJustEscape = true;
            } else {
                state.currentString.appendCodePoint(codepoint);
                if (codepoint != '\n' && !Character.isWhitespace(codepoint)) {
                    state.t += 1.33;
                }
                state.didJustEscape = false;
            }
            return true;
        });
        if (!state.didJustAdd && state.currentString != null && state.style != null) {
            styles.add(Pair.of(state.currentString.toString(), state.style));
        }
        // list 2: the pairs united into styles
        List<FormattedText> result = new ArrayList<>();
        for (Pair<String, Style> i : styles) {
            result.add(FormattedText.of(i.getFirst(), i.getSecond()));
        }
        if (lastT != state.t) {
            CutsceneAPI.LOGGER.debug("would've played a sound here");
            // TODO play sound
        }
        lastT = state.t;
        int x = (int)(this.config.rx().get(time.getProgress()) * width);
        int y = (int)(this.config.ry().get(time.getProgress()) * height);
        int lineWidth = (int)(this.config.width().get(time.getProgress()) * width);
        // i could've used drawWordWrap() here, but it doesn't do a shadow
        // the code below is copied from GuiGraphics#drawWordWrap
        for (FormattedCharSequence j : minecraft.font.split(FormattedText.composite(result), lineWidth)) {
            graphics.drawString(minecraft.font, j, x, y, 0xffffff, true);
            y += minecraft.font.lineHeight;
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
