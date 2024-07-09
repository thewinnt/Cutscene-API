package net.thewinnt.cutscenes.client.overlay;

import net.thewinnt.cutscenes.util.DynamicColor;

public class FadeToColorOverlayConfiguration {
    public final DynamicColor bottomLeft;
    public final DynamicColor topLeft;
    public final DynamicColor topRight;
    public final DynamicColor bottomRight;
    private float alpha;
    private double progress = 0;

    public FadeToColorOverlayConfiguration(DynamicColor colorBottomLeft, DynamicColor colorTopLeft, DynamicColor colorTopRight, DynamicColor colorBottomRight, float alpha) {
        this.bottomLeft = colorBottomLeft;
        this.topLeft = colorTopLeft;
        this.topRight = colorTopRight;
        this.bottomRight = colorBottomRight;
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
}
