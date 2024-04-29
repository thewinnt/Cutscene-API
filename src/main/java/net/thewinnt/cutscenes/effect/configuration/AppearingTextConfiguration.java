package net.thewinnt.cutscenes.effect.configuration;

import net.minecraft.network.chat.Component;
import net.thewinnt.cutscenes.easing.Easing;

public record AppearingTextConfiguration(Component text, Easing rx, Easing ry) {
}
