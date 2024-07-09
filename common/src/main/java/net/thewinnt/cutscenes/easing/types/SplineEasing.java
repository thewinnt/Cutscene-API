package net.thewinnt.cutscenes.easing.types;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.serializers.SplineEasingSerializer;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;

public class SplineEasing implements Easing {
    private final Easing[] points;

    public SplineEasing(Easing... points) {
        this.points = points;
    }

    @Override
    public double get(double t) {
        if (t <= 0) return points[0].get(t);
        if (t >= 1) return points[points.length - 1].get(t);
        int startSegment = (int)((points.length - 1) * t);
        double a, b, c, d;
        if (this.points.length == 2) {
            b = points[0].get(t);
            c = points[1].get(t);
            a = Mth.lerp(2, c, b); // end to start
            d = Mth.lerp(2, b, c); // start to end
        } else if (startSegment == 0) {
            b = points[0].get(t);
            c = points[1].get(t);
            d = points[2].get(t);
            a = Mth.lerp(2, c, b);
        } else if (startSegment == points.length - 2) {
            a = points[startSegment - 1].get(t);
            b = points[startSegment].get(t);
            c = points[startSegment + 1].get(t);
            d = Mth.lerp(2, b, c);
        } else {
            a = points[startSegment - 1].get(t);
            b = points[startSegment].get(t);
            c = points[startSegment + 1].get(t);
            d = points[startSegment + 2].get(t);
        }
        double step = 1.0 / (points.length - 1);
        t -= startSegment * step;
        t /= step;
        return (2*b + t * (-a + c) + t*t * (2*a - 5*b + 4*c - d) + t*t*t * (-a + 3*b - 3*c + d)) * 0.5;
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return SplineEasingSerializer.INSTANCE;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writeArray(buf, points, (buf1, easing) -> Easing.toNetwork(easing, buf1));
    }
}
