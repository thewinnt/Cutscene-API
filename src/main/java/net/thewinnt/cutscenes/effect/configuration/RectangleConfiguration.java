package net.thewinnt.cutscenes.effect.configuration;

import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.DynamicColor;

public record RectangleConfiguration(
    CoordinateProvider x,
    CoordinateProvider y,
    CoordinateProvider width,
    CoordinateProvider height,
    DynamicColor colorTop,
    DynamicColor colorBottom
) {}
