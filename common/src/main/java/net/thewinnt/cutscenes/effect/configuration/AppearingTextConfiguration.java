package net.thewinnt.cutscenes.effect.configuration;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.FloatProvider;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.util.CoordinateProvider;

public record AppearingTextConfiguration(
    Component text,
    CoordinateProvider rx,
    CoordinateProvider ry,
    CoordinateProvider width,
    boolean dropShadow,
    Identifier soundbite,
    DelayProvider delays,
    FloatProvider pitch,
    Easing scale,
    Easing rotation
) {
}
