package net.thewinnt.cutscenes.effect.configuration;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.FloatProvider;
import net.thewinnt.cutscenes.effect.chardelays.DelayProvider;
import net.thewinnt.cutscenes.util.CoordinateProvider;

public record AppearingTextConfiguration(
    Component text,
    CoordinateProvider rx,
    CoordinateProvider ry,
    CoordinateProvider width,
    boolean dropShadow,
    ResourceLocation soundbite,
    DelayProvider delays,
    FloatProvider pitch
) {
}
