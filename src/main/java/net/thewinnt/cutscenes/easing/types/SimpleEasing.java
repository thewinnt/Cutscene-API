package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;

import java.util.function.Function;

public class SimpleEasing implements Easing {
    public static final SimpleEasing LINEAR = new SimpleEasing(t -> t, "linear");
    public static final SimpleEasing IN_SINE = new SimpleEasing(t -> 1 - Math.cos((t * Math.PI) / 2), "in_sine");
    public static final SimpleEasing OUT_SINE = new SimpleEasing(t -> Math.cos((t * Math.PI) / 2), "out_sine");
    public static final SimpleEasing IN_OUT_SINE = new SimpleEasing(t -> -(Math.cos(t * Math.PI) - 1) / 2, "in_out_sine");
    public static final SimpleEasing IN_QUAD = new SimpleEasing(t -> t * t, "in_quad");
    public static final SimpleEasing OUT_QUAD = new SimpleEasing(t -> 1 - (1 - t) * (1 - t), "out_quad");
    public static final SimpleEasing IN_OUT_QUAD = new SimpleEasing(t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2, "in_out_quad");
    public static final SimpleEasing IN_CUBIC = new SimpleEasing(t -> t * t * t, "in_cubic");
    public static final SimpleEasing OUT_CUBIC = new SimpleEasing(t -> 1 - Math.pow(1 - t, 3), "out_cubic");
    public static final SimpleEasing IN_OUT_CUBIC = new SimpleEasing(t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2, "in_out_cubic");
    public static final SimpleEasing IN_QUART = new SimpleEasing(t -> t * t * t * t, "in_quart");
    public static final SimpleEasing OUT_QUART = new SimpleEasing(t -> 1 - Math.pow(1 - t, 4), "out_quart");
    public static final SimpleEasing IN_OUT_QUART = new SimpleEasing(t -> t < 0.5 ? 8 * t * t * t * t : 1 - Math.pow(-2 * t + 2, 4) / 2, "in_out_quart");
    public static final SimpleEasing IN_QUINT = new SimpleEasing(t -> t * t * t * t * t, "in_quint");
    public static final SimpleEasing OUT_QUINT = new SimpleEasing(t -> 1 - Math.pow(1 - t, 5), "out_quint");
    public static final SimpleEasing IN_OUT_QUINT = new SimpleEasing(t -> t < 0.5 ? 16 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2, "in_out_quint");
    public static final SimpleEasing IN_EXPO = new SimpleEasing(t -> t == 0 ? 0 : Math.pow(2, 10 * t - 10), "in_expo");
    public static final SimpleEasing OUT_EXPO = new SimpleEasing(t -> t == 1 ? 1 : 1 - Math.pow(2, -10 * t), "out_expo");
    public static final SimpleEasing IN_OUT_EXPO = new SimpleEasing(t -> t == 0 ? 0 : t == 1 ? 1 : t < 0.5 ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2, "in_out_expo");
    public static final SimpleEasing IN_CIRC = new SimpleEasing(t -> 1 - Math.sqrt(1 - Math.pow(t, 2)), "in_circ");
    public static final SimpleEasing OUT_CIRC = new SimpleEasing(t -> Math.sqrt(1 - (t - 1) * (t - 1)), "out_circ");
    public static final SimpleEasing IN_OUT_CIRC = new SimpleEasing(t -> t < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * t, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2, "in_out_circ");
    public static final SimpleEasing IN_BACK = new SimpleEasing(t -> 2.70158 * t * t * t - 1.70158 * t * t, "in_back");
    public static final SimpleEasing OUT_BACK = new SimpleEasing(t -> 1 + 2.70158 * Math.pow(t - 1, 3) + 1.70158 * Math.pow(t - 1, 2), "out_back");
    public static final SimpleEasing IN_OUT_BACK = new SimpleEasing(t -> t < 0.5 ? (Math.pow(2 * t, 2) * ((2.59491 + 1) * 2 * t - 2.59491)) / 2 : (Math.pow(2 * t - 2, 2) * ((2.59491 + 1) * (t * 2 - 2) + 2.59491) + 2) / 2, "in_out_back");
    public static final SimpleEasing IN_ELASTIC = new SimpleEasing(t -> t == 0 ? 0 : t == 1 ? 1 : -Math.pow(2, 10 * t - 10) * Math.sin((t * 10 - 10.75) * (Math.PI * 2 / 3)), "in_elastic");
    public static final SimpleEasing OUT_ELASTIC = new SimpleEasing(t -> t == 0 ? 0 : t == 1 ? 1 : Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (Math.PI * 2 / 3)) + 1, "out_elastic");
    public static final SimpleEasing IN_OUT_ELASTIC = new SimpleEasing(t -> t == 0 ? 0 : t == 1 ? 1 : t < 0.5 ? -(Math.pow(2, 20 * t - 10) * Math.sin((20 * t - 11.125) * (Math.PI * 2 / 4.5))) / 2 : (Math.pow(2, -20 * t + 10) * Math.sin((20 * t - 11.125) * (Math.PI * 2 / 4.5))) / 2 + 1, "in_out_elastic");
    public static final SimpleEasing OUT_BOUNCE = new SimpleEasing(t -> {
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
    }, "out_bounce");
    public static final SimpleEasing IN_BOUNCE = new SimpleEasing(t -> 1 - OUT_BOUNCE.get(1 - t), "in_bounce");
    public static final SimpleEasing IN_OUT_BOUNCE = new SimpleEasing(t -> t < 0.5 ? (1 - OUT_BOUNCE.get(1 - 2 * t)) / 2 : (1 + OUT_BOUNCE.get(2 * t - 1)) / 2, "in_out_bounce");
    private final Function<Double, Double> function;
    private final String serializer;

    public SimpleEasing(Function<Double, Double> function, String id) {
        this.function = function;
        this.serializer = id;
    }

    @Override
    public double get(double t) {
        return function.apply(t);
    }

    @Override
    public EasingSerializer<SimpleEasing> getSerializer() {
        return EasingSerializer.SIMPLE_EASINGS.get(serializer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}
}
