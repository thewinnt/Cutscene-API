package net.thewinnt.cutscenes.client.overlay;

public class FadeToColorOverlayConfiguration {
    private float[] colorBottomLeft = new float[]{1, 0, 0, 1};
    private float[] colorTopLeft = new float[]{0, 1, 0, 1};
    private float[] colorTopRight = new float[]{0, 0, 1, 1};
    private float[] colorBottomRight = new float[]{1, 1, 1, 1};
    private float alpha = 0;

    public FadeToColorOverlayConfiguration(float[] colorBottomLeft, float[] colorTopLeft, float[] colorTopRight, float[] colorBottomRight, float alpha) {
        this.colorBottomLeft = colorBottomLeft;
        this.colorTopLeft = colorTopLeft;
        this.colorTopRight = colorTopRight;
        this.colorBottomRight = colorBottomRight;
        this.alpha = alpha;
    }

    public float[] getColorBottomLeft() {
        return colorBottomLeft;
    }

    public void setColorBottomLeft(float[] colorBottomLeft) {
        this.colorBottomLeft = colorBottomLeft;
    }

    public float[] getColorTopLeft() {
        return colorTopLeft;
    }

    public void setColorTopLeft(float[] colorTopLeft) {
        this.colorTopLeft = colorTopLeft;
    }

    public float[] getColorTopRight() {
        return colorTopRight;
    }

    public void setColorTopRight(float[] colorTopRight) {
        this.colorTopRight = colorTopRight;
    }

    public float[] getColorBottomRight() {
        return colorBottomRight;
    }

    public void setColorBottomRight(float[] colorBottomRight) {
        this.colorBottomRight = colorBottomRight;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setColors(float[] bottomLeft, float[] topLeft, float[] topRight, float[] bottomRight) {
        this.colorBottomLeft = bottomLeft;
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
    }
}
