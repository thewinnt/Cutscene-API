package net.thewinnt.cutscenes.client.overlay;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.util.TimeProvider;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;

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
        minecraft.getProfiler().push("cutscenes:appearing_text");
        minecraft.getProfiler().push("prepare");
        TimeProvider time = (TimeProvider) config;
        Component text = this.config.text();
        // list 1: each component split into formatted strings
        List<Pair<String, Style>> styles = new ArrayList<>();
        double currentTime = time.getTime();
        final var state = new DrawingState();
        final DelayProvider delays = this.config.delays();
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
            if (state.gettingDelay) {
                state.t += delays.delay(codepoint);
                state.gettingDelay = false;
                return true;
            }
            if (codepoint == delays.activationCodepoint() && !state.didJustEscape) {
                state.gettingDelay = true;
            } else if (codepoint == '\\' && !state.didJustEscape) {
                state.didJustEscape = true;
            } else {
                state.currentString.appendCodePoint(codepoint);
                state.t += delays.defaultDelay(codepoint);
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
        SoundEvent soundbite = SoundEvent.createVariableRangeEvent(this.config.soundbite());
        if (lastT != state.t) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundbite, this.config.pitch().sample(minecraft.player.getRandom()), 1));
        }
        lastT = state.t;
        float x = this.config.rx().get(time.getProgress(), width);
        float y = this.config.ry().get(time.getProgress(), height);
        int lineWidth = (int)this.config.width().get(time.getProgress(), width);
        // i could've used drawWordWrap() here, but it doesn't do a shadow
        // the code below is copied from GuiGraphics#drawWordWrap
        minecraft.getProfiler().popPush("draw");
        for (FormattedCharSequence j : minecraft.font.split(FormattedText.composite(result), lineWidth)) {
            graphics.drawString(minecraft.font, j, (int)x, (int)y, 0xffffff, this.config.dropShadow());
            y += minecraft.font.lineHeight;
        }
        minecraft.getProfiler().pop();
        minecraft.getProfiler().pop();
    }

    private static class DrawingState {
        double t;
        boolean didJustEscape;
        boolean didJustAdd;
        boolean gettingDelay;
        Style style;
        StringBuilder currentString;
    }
}
