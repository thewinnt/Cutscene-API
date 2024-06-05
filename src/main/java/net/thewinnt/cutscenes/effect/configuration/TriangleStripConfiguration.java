package net.thewinnt.cutscenes.effect.configuration;

import net.thewinnt.cutscenes.util.DynamicVertex;

public record TriangleStripConfiguration(DynamicVertex[] vertices) {
    public TriangleStripConfiguration(DynamicVertex[] vertices) {
        this.vertices = vertices;
        if (vertices.length < 3) {
            throw new IllegalArgumentException("A triangle strip must contain at least 1 triangle (3 vertices)");
        }
    }
}
