package net.thewinnt.cutscenes.effect.configuration;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;

public record SimpleTextConfiguration(
    Component text,
    CoordinateProvider rx,
    CoordinateProvider ry,
    boolean centered,
    Easing scale,
    Easing rotation,
    Optional<DynamicColor> colorOverride) {
}
