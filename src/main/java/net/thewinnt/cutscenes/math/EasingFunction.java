package net.thewinnt.cutscenes.math;

import java.util.function.Function;

public enum EasingFunction {
    // source: https://easings.net
    LINEAR(t -> t),
    IN_SINE(t -> 1 - Math.cos((t * Math.PI) / 2)),
    OUT_SINE(t -> Math.cos((t * Math.PI) / 2)),
    IN_OUT_SINE(t -> -(Math.cos(t * Math.PI) - 1) / 2),
    IN_QUAD(t -> t * t),
    OUT_QUAD(t -> 1 - (1 - t) * (1 - t)),
    IN_OUT_QUAD(t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2),
    IN_CUBIC(t -> t * t * t),
    OUT_CUBIC(t -> 1 - Math.pow(1 - t, 3)),
    IN_OUT_CUBIC(t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2),
    IN_QUART(t -> t * t * t * t),
    OUT_QUART(t -> 1 - Math.pow(1 - t, 4)),
    IN_OUT_QUART(t -> t < 0.5 ? 8 * t * t * t * t : 1 - Math.pow(-2 * t + 2, 4) / 2),
    IN_QUINT(t -> t * t * t * t * t),
    OUT_QUINT(t -> 1 - Math.pow(1 - t, 5)),
    IN_OUT_QUINT(t -> t < 0.5 ? 16 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2),
    IN_EXPO(t -> t == 0 ? 0 : Math.pow(2, 10 * t - 10)),
    OUT_EXPO(t -> t == 1 ? 1 : 1 - Math.pow(2, -10 * t)),
    IN_OUT_EXPO(t -> t == 0 ? 0 : t == 1 ? 1 : t < 0.5 ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2),
    IN_CIRC(t -> 1 - Math.sqrt(1 - Math.pow(t, 2))),
    OUT_CIRC(t -> Math.sqrt(1 - (t - 1) * (t - 1))),
    IN_OUT_CIRC(t -> t < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * t, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2),
    IN_BACK(t -> 2.70158 * t * t * t - 1.70158 * t * t),
    OUT_BACK(t -> 1 + 2.70158 * Math.pow(t - 1, 3) + 1.70158 * Math.pow(t - 1, 2)),
    IN_OUT_BACK(t -> t < 0.5 ? (Math.pow(2 * t, 2) * ((2.59491 + 1) * 2 * t - 2.59491)) / 2 : (Math.pow(2 * t - 2, 2) * ((2.59491 + 1) * (t * 2 - 2) + 2.59491) + 2) / 2),
    IN_ELASTIC(t -> t == 0 ? 0 : t == 1 ? 1 : -Math.pow(2, 10 * t - 10) * Math.sin((t * 10 - 10.75) * (Math.PI * 2 / 3))),
    OUT_ELASTIC(t -> t == 0 ? 0 : t == 1 ? 1 : Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (Math.PI * 2 / 3)) + 1),
    IN_OUT_ELASTIC(t -> t == 0 ? 0 : t == 1 ? 1 : t < 0.5 ? -(Math.pow(2, 20 * t - 10) * Math.sin((20 * t - 11.125) * (Math.PI * 2 / 4.5))) / 2 : (Math.pow(2, -20 * t + 10) * Math.sin((20 * t - 11.125) * (Math.PI * 2 / 4.5))) / 2 + 1),
    OUT_BOUNCE(t -> {
        double a = 7.5625;
        double b = 2.75;
        if (t < 1 / b) {
            return a * t * t;
        } else if (t < 2 / b) {
            return a * (t -= 1.5 / b) * t + 0.75;
        } else if (t < 2.5 / b) {
            return a * (t -= 2.25 / b) * t + 0.9375;
        } else {
            return a * (t -= 2.625 / b) * t + 0.984375;
        }
    }),
    IN_BOUNCE(t -> 1 - OUT_BOUNCE.apply(1 - t)),
    IN_OUT_BOUNCE(t -> t < 0.5 ? (1 - OUT_BOUNCE.apply(1 - 2 * t)) / 2 : (1 + OUT_BOUNCE.apply(2 * t - 1)) / 2);
    private final Function<Double, Double> function;
    private EasingFunction(Function<Double, Double> function) {
        this.function = function;
    }

    public double apply(double value) {
        return this.function.apply(value);
    }
}