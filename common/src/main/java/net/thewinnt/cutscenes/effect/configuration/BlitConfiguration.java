package net.thewinnt.cutscenes.effect.configuration;

import net.minecraft.resources.Identifier;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;

public record BlitConfiguration(
    Identifier texture,
    CoordinateProvider x1,
    CoordinateProvider y1,
    CoordinateProvider x2,
    CoordinateProvider y2,
    CoordinateProvider u1,
    CoordinateProvider v1,
    CoordinateProvider u2,
    CoordinateProvider v2,
    DynamicColor tint
) {
}
