package net.thewinnt.cutscenes.util;

public class TimeProvider {
    public final double maxTime;
    private double time;

    public TimeProvider(double maxTime) {
        this.maxTime = maxTime;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    public double getProgress() {
        return time / maxTime;
    }
}
