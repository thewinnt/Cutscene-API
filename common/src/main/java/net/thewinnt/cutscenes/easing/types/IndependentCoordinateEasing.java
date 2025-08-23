package net.thewinnt.cutscenes.easing.types;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.easing.impl.CoordinateSupplierImpl;
import net.thewinnt.cutscenes.util.CoordinateProvider;
import net.thewinnt.cutscenes.util.JsonHelper;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.Locale;
import java.util.function.Supplier;

public class IndependentCoordinateEasing implements Easing {
    private final Supplier<CoordinateSupplier> providerSupplier;
    private final Axis inAxis;
    private final Axis outAxis;
    private final CoordinateProvider in;
    private CoordinateSupplier provider;

    public IndependentCoordinateEasing(Axis inAxis, Axis outAxis, CoordinateProvider in) {
        this.providerSupplier = CoordinateSupplierImpl::new;
        this.inAxis = inAxis;
        this.outAxis = outAxis;
        this.in = in;
    }

    @Override
    public double get(double t) {
        if (this.provider == null) {
            this.provider = this.providerSupplier.get();
        }
        int width = this.provider.getWidth();
        int height = this.provider.getHeight();
        double inValue = in.get(t, inAxis == Axis.WIDTH ? width : height);
        return inValue / (outAxis == Axis.WIDTH ? width : height);
    }

    @Override
    public EasingSerializer<?> getSerializer() {
        return EasingSerializer.COORDINATE;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeEnum(inAxis);
        buf.writeEnum(outAxis);
        in.toNetwork(buf);
    }

    public enum Axis {
        WIDTH,
        HEIGHT;

        public static Axis fromJSON(JsonObject json, String name, LoadingContext context) {
            context.pushElement(name);
            try {
                return valueOf(json.get(name).getAsString().toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                context.reportError("Uncaught exception: " + e);
                return null;
            } finally {
                context.popElement();
            }
        }
    }

    public interface CoordinateSupplier {
        int getWidth();
        int getHeight();
    }
}
