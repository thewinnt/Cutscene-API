package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.client.overlay.FadeToColorOverlay;
import net.thewinnt.cutscenes.client.overlay.FadeToColorOverlayConfiguration;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ColorEasing;
import net.thewinnt.cutscenes.easing.types.CompoundEasing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.util.DynamicColor;
import net.thewinnt.cutscenes.util.JsonHelper;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.List;

public class FadeToColorTransition implements Transition {
    public static final MapCodec<FadeToColorTransition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ColorConfig.CODEC.fieldOf("colors").forGetter(t -> t.colors),
        Codec.DOUBLE.fieldOf("length_a").forGetter(t -> t.lengthA),
        Codec.DOUBLE.fieldOf("length_b").forGetter(t -> t.lengthB),
        Easing.CODEC.optionalFieldOf("ease_in", SimpleEasing.LINEAR).forGetter(t -> t.easeIn),
        Easing.CODEC.optionalFieldOf("ease_out", SimpleEasing.LINEAR).forGetter(t -> t.easeOut),
        Codec.BOOL.fieldOf("is_start").forGetter(t -> t.isStart)
    ).apply(instance, FadeToColorTransition::new));
    private final ColorConfig colors;
    private final double lengthA;
    private final double lengthB;
    private final double progressLengthA;
    private final double progressLengthB;
    private final Easing easeIn;
    private final Easing easeOut;
    private final boolean isStart;
    private FadeToColorOverlayConfiguration config;

    private FadeToColorTransition(ColorConfig colors, double lengthA, double lengthB, Easing easeIn, Easing easeOut, boolean isStart) {
        this.colors = colors;
        this.lengthA = lengthA;
        this.lengthB = lengthB;
        this.progressLengthA = lengthA / (lengthA + lengthB);
        this.progressLengthB = lengthB / (lengthA + lengthB);
        this.easeIn = easeIn;
        this.easeOut = easeOut;
        this.isStart = isStart;
    }

    public FadeToColorTransition(DynamicColor colorBottomLeft, DynamicColor colorTopLeft, DynamicColor colorTopRight, DynamicColor colorBottomRight, double lengthA, double lengthB, Easing easeIn, Easing easeOut, boolean isStart) {
        this.colors = new ColorConfig(colorBottomLeft, colorTopLeft, colorTopRight, colorBottomRight);
        this.lengthA = lengthA;
        this.lengthB = lengthB;
        this.progressLengthA = lengthA / (lengthA + lengthB);
        this.progressLengthB = lengthB / (lengthA + lengthB);
        this.easeIn = easeIn;
        this.easeOut = easeOut;
        this.isStart = isStart;
    }

    public FadeToColorTransition(DynamicColor color, double lengthA, double lengthB, Easing easeIn, Easing easeOut, boolean isStart) {
        this(color, color, color, color, lengthA, lengthB, easeIn, easeOut, isStart);
    }

    public FadeToColorTransition(DynamicColor color, double length, Easing easeIn, Easing easeOut, boolean isStart) {
        this(color, length, length, easeIn, easeOut, isStart);
    }

    @Override
    public double getLength() {
        return lengthA + lengthB;
    }

    @Override
    public double getOffCutsceneTime() {
        return isStart ? lengthA : lengthB;
    }

    @Override
    public double getOnCutsceneTime() {
        return isStart ? lengthB : lengthA;
    }

    @Override
    public Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene) {
        if (isStart) {
            if (progress < progressLengthA) {
                return initCamPos;
            } else if (cutscene.path == null) {
                return Minecraft.getInstance().player.getPosition((float) (progress * getLength() % 1));
            } else {
                double cutsceneProgress = (progress - progressLengthA) / progressLengthA * lengthA / cutscene.length.length();
                return cutscene.getPathPoint(cutsceneProgress, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
            }
        } else {
            if (progress > progressLengthA) {
                return initCamPos;
            } else if (cutscene.path == null) {
                return startPos;
            } else {
                double cutsceneProgress = (cutscene.length.length() - lengthA + lengthA * (progress / progressLengthA)) / cutscene.length.length();
                return cutscene.getPathPoint(cutsceneProgress, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
            }
        }
    }

    @Override
    public Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene) {
        if (isStart) {
            if (progress < progressLengthA) {
                return initCamRot;
            } else if (cutscene.rotationProvider == null) {
                return ClientCutsceneManager.camera.getPlayerCamRot();
            } else {
                double cutsceneProgress = (progress - progressLengthA) / progressLengthA * lengthA / cutscene.length.length();
                return cutscene.getTransformedRotation(cutsceneProgress, level, startPos, initCamRot, startRot, ClientCutsceneManager.camera.getPlayerCamRot(), ClientCutsceneManager.dt()).add(startRot);
            }
        } else {
            if (progress > progressLengthA) {
                return initCamRot;
            } else if (cutscene.rotationProvider == null) {
                return startRot;
            } else {
                double cutsceneProgress = (cutscene.length.length() - lengthA + lengthA * (progress / progressLengthA)) / cutscene.length.length();
                return cutscene.getTransformedRotation(cutsceneProgress, level, startPos, initCamRot, startRot, ClientCutsceneManager.camera.getPlayerCamRot(), ClientCutsceneManager.dt()).add(startRot);
            }
        }
    }

    @Override
    public void onStart(CutsceneType cutscene) {
        this.config = new FadeToColorOverlayConfiguration(colors.bottomLeft, colors.topLeft, colors.topRight, colors.bottomRight, 0);
        CutsceneOverlayManager.addOverlay(FadeToColorOverlay.INSTANCE, this.config);
    }

    @Override
    public void onFrame(double progress, CutsceneType cutscene) {
        if (progress < progressLengthA) {
            this.config.setAlpha((float)easeIn.get(progress / progressLengthA));
        } else {
            this.config.setAlpha((float)easeOut.get(Mth.clamp(1 - progress, 0, 1) / progressLengthB));
        }
        this.config.setProgress(progress);
    }

    @Override
    public void onEnd(CutsceneType cutscene) {
        CutsceneOverlayManager.removeOverlay(FadeToColorOverlay.INSTANCE, this.config);
        this.config.setAlpha(0);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        colors.bottomLeft.toNetwork(buf);
        colors.topLeft.toNetwork(buf);
        colors.topRight.toNetwork(buf);
        colors.bottomRight.toNetwork(buf);
        buf.writeDouble(lengthA);
        buf.writeDouble(lengthB);
        Easing.toNetwork(easeIn, buf);
        Easing.toNetwork(easeOut, buf);
        buf.writeBoolean(isStart);
    }

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.FADE;
    }

    public static FadeToColorTransition fromNetwork(FriendlyByteBuf buf) {
        DynamicColor colorBottomLeft = DynamicColor.fromNetwork(buf);
        DynamicColor colorTopLeft = DynamicColor.fromNetwork(buf);
        DynamicColor colorTopRight = DynamicColor.fromNetwork(buf);
        DynamicColor colorBottomRight = DynamicColor.fromNetwork(buf);
        double lengthA = buf.readDouble();
        double lengthB = buf.readDouble();
        Easing easeIn = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easeOut = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        boolean isStart = buf.readBoolean();
        return new FadeToColorTransition(colorBottomLeft, colorTopLeft, colorTopRight, colorBottomRight, lengthA, lengthB, easeIn, easeOut, isStart);
    }

    public static FadeToColorTransition fromJSON(JsonObject json, LoadingContext context) {
        double lengthA = JsonHelper.getAsDouble(json, "length_a", context);
        double lengthB = GsonHelper.getAsDouble(json, "length_b", lengthA);
        Easing easeIn = Easing.loadWrapped(json, "ease_in", context, SimpleEasing.LINEAR);
        Easing easeOut = Easing.loadWrapped(json, "ease_out", context, SimpleEasing.LINEAR);
        boolean isStart = JsonHelper.getAsBoolean(json, "is_start", context);
        String colorType = GsonHelper.getAsString(json, "color_definition", "single_color");
        switch (colorType) {
            case "per_angle" -> {
                DynamicColor colorBottomLeft = DynamicColor.loadWrapped(json, "bottom_left", context);
                DynamicColor colorTopLeft = DynamicColor.loadWrapped(json, "top_left", context);
                DynamicColor colorTopRight = DynamicColor.loadWrapped(json, "top_right", context);
                DynamicColor colorBottomRight = DynamicColor.loadWrapped(json, "bottom_right", context);
                return new FadeToColorTransition(colorBottomLeft, colorTopLeft, colorTopRight, colorBottomRight, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "horizontal_gradient" -> {
                DynamicColor color1 = DynamicColor.loadWrapped(json, "color1", context);
                DynamicColor color2 = DynamicColor.loadWrapped(json, "color2", context);
                return new FadeToColorTransition(color1, color1, color2, color2, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "vertical_gradient" -> {
                DynamicColor color1 = DynamicColor.loadWrapped(json, "color1", context);
                DynamicColor color2 = DynamicColor.loadWrapped(json, "color2", context);
                return new FadeToColorTransition(color1, color2, color2, color1, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "four_angles" -> {
                ColorConfig colors = legacyFourAngles(json, context, lengthA, lengthB);
                return new FadeToColorTransition(colors.bottomLeft, colors.topLeft, colors.topRight, colors.bottomRight, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "two_colors" -> {
                DynamicColor color = legacyTwoColors(json, context, lengthA, lengthB);
                return new FadeToColorTransition(color, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            default -> {
                DynamicColor color = DynamicColor.loadWrapped(json, "color", context);
                return new FadeToColorTransition(color, lengthA, lengthB, easeIn, easeOut, isStart);
            }
        }
    }

    private static ColorConfig legacyFourAngles(JsonObject json, LoadingContext context, double lengthA, double lengthB) {
        DynamicColor startColorBottomLeft = DynamicColor.loadWrapped(json, "start_color_bottom_left", context);
        DynamicColor startColorTopLeft = DynamicColor.loadWrapped(json, "start_color_top_left", context);
        DynamicColor startColorTopRight = DynamicColor.loadWrapped(json, "start_color_top_right", context);
        DynamicColor startColorBottomRight = DynamicColor.loadWrapped(json, "start_color_bottom_right", context);
        DynamicColor endColorBottomLeft = DynamicColor.loadWrapped(json, "end_color_bottom_left", context);
        DynamicColor endColorTopLeft = DynamicColor.loadWrapped(json, "end_color_top_left", context);
        DynamicColor endColorTopRight = DynamicColor.loadWrapped(json, "end_color_top_right", context);
        DynamicColor endColorBottomRight = DynamicColor.loadWrapped(json, "end_color_bottom_right", context);

        double gradientTimeA = GsonHelper.getAsDouble(json, "gradient_time_a", lengthA);
        double gradientTimeB = GsonHelper.getAsDouble(json, "gradient_time_b", lengthA);
        Easing colorEase = Easing.loadWrapped(json, "color_ease", context, SimpleEasing.LINEAR);

        return legacyFourAngles(lengthA, lengthB, gradientTimeA, gradientTimeB, startColorBottomLeft, endColorBottomLeft, colorEase, startColorTopLeft, endColorTopLeft, startColorTopRight, endColorTopRight, startColorBottomRight, endColorBottomRight);
    }

    private static ColorConfig legacyFourAngles(double lengthA, double lengthB, double gradientTimeA, double gradientTimeB, DynamicColor startColorBottomLeft, DynamicColor endColorBottomLeft, Easing colorEase, DynamicColor startColorTopLeft, DynamicColor endColorTopLeft, DynamicColor startColorTopRight, DynamicColor endColorTopRight, DynamicColor startColorBottomRight, DynamicColor endColorBottomRight) {
        if (lengthB == -1) lengthB = lengthA;
        double progressGA = gradientTimeA / (lengthA + lengthB);
        double progressGB = gradientTimeB / (lengthA + lengthB);
        DynamicColor bottomLeft = new DynamicColor(
            createCompound(progressGA, progressGB, startColorBottomLeft.r(), endColorBottomLeft.r(), colorEase),
            createCompound(progressGA, progressGB, startColorBottomLeft.g(), endColorBottomLeft.g(), colorEase),
            createCompound(progressGA, progressGB, startColorBottomLeft.b(), endColorBottomLeft.b(), colorEase),
            createCompound(progressGA, progressGB, startColorBottomLeft.a(), endColorBottomLeft.a(), colorEase)
        );
        DynamicColor topLeft = new DynamicColor(
            createCompound(progressGA, progressGB, startColorTopLeft.r(), endColorTopLeft.r(), colorEase),
            createCompound(progressGA, progressGB, startColorTopLeft.g(), endColorTopLeft.g(), colorEase),
            createCompound(progressGA, progressGB, startColorTopLeft.b(), endColorTopLeft.b(), colorEase),
            createCompound(progressGA, progressGB, startColorTopLeft.a(), endColorTopLeft.a(), colorEase)
        );
        DynamicColor topRight = new DynamicColor(
            createCompound(progressGA, progressGB, startColorTopRight.r(), endColorTopRight.r(), colorEase),
            createCompound(progressGA, progressGB, startColorTopRight.g(), endColorTopRight.g(), colorEase),
            createCompound(progressGA, progressGB, startColorTopRight.b(), endColorTopRight.b(), colorEase),
            createCompound(progressGA, progressGB, startColorTopRight.a(), endColorTopRight.a(), colorEase)
        );
        DynamicColor bottomRight = new DynamicColor(
            createCompound(progressGA, progressGB, startColorBottomRight.r(), endColorBottomRight.r(), colorEase),
            createCompound(progressGA, progressGB, startColorBottomRight.g(), endColorBottomRight.g(), colorEase),
            createCompound(progressGA, progressGB, startColorBottomRight.b(), endColorBottomRight.b(), colorEase),
            createCompound(progressGA, progressGB, startColorBottomRight.a(), endColorBottomRight.a(), colorEase)
        );
        return new ColorConfig(bottomLeft, topLeft, topRight, bottomRight);
    }

    private static DynamicColor legacyTwoColors(JsonObject json, LoadingContext context, double lengthA, double lengthB) {
        DynamicColor color1 = DynamicColor.loadWrapped(json, "color1", context);
        DynamicColor color2 = DynamicColor.loadWrapped(json, "color2", context);

        double gradientTimeA = GsonHelper.getAsDouble(json, "gradient_time_a", lengthA);
        double gradientTimeB = GsonHelper.getAsDouble(json, "gradient_time_b", lengthA);
        Easing colorEase = Easing.loadWrapped(json, "color_ease", context, SimpleEasing.LINEAR);

        return legacyTwoColors(lengthA, lengthB, gradientTimeA, gradientTimeB, color1, color2, colorEase);
    }

    private static DynamicColor legacyTwoColors(double lengthA, double lengthB, double gradientTimeA, double gradientTimeB, DynamicColor color1, DynamicColor color2, Easing colorEase) {
        double progressGA = gradientTimeA / (lengthA + lengthB);
        double progressGB = gradientTimeB / (lengthA + lengthB);
        return new DynamicColor(
                createCompound(progressGA, progressGB, color1.r(), color2.r(), colorEase),
                createCompound(progressGA, progressGB, color1.g(), color2.g(), colorEase),
                createCompound(progressGA, progressGB, color1.b(), color2.b(), colorEase),
                createCompound(progressGA, progressGB, color1.a(), color2.a(), colorEase)
        );
    }

    private static ColorConfig legacyTwoColorsCfg(double lengthA, double lengthB, double gradientTimeA, double gradientTimeB, DynamicColor color1, DynamicColor color2, Easing colorEase) {
        DynamicColor color = legacyTwoColors(lengthA, lengthB, gradientTimeA, gradientTimeB, color1, color2, colorEase);
        return new ColorConfig(color, color, color, color);
    }

    private static CompoundEasing createCompound(double progressGA, double progressGB, Easing color1, Easing color2, Easing colorDelta) {
        ColorEasing colorEasing = new ColorEasing(colorDelta, color1, color2);
        return new CompoundEasing(List.of(
            new CompoundEasing.TimedEasingEntry(0, new CompoundEasing.RangeAppliedEasing(0, 1, new ConstantEasing(colorEasing.get(0)))),
            new CompoundEasing.TimedEasingEntry(progressGA, new CompoundEasing.RangeAppliedEasing(0, 1, colorEasing)),
            new CompoundEasing.TimedEasingEntry(progressGB, new CompoundEasing.RangeAppliedEasing(0, 1, new ConstantEasing(colorEasing.get(1))))
        ));
    }

    private record ColorConfig(DynamicColor bottomLeft, DynamicColor topLeft, DynamicColor topRight, DynamicColor bottomRight) {
        private ColorConfig(DynamicColor[] colors) {
            this(colors[0], colors[1], colors[2], colors[3]);
        }

        private static final MapCodec<ColorConfig> PER_ANGLE = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DynamicColor.CODEC.fieldOf("bottom_left").forGetter(ColorConfig::bottomLeft),
                DynamicColor.CODEC.fieldOf("top_left").forGetter(ColorConfig::topLeft),
                DynamicColor.CODEC.fieldOf("top_right").forGetter(ColorConfig::topRight),
                DynamicColor.CODEC.fieldOf("bottom_right").forGetter(ColorConfig::bottomRight)
        ).apply(instance, ColorConfig::new));

        private static final MapCodec<ColorConfig> HORIZONTAL_GRADIENT = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DynamicColor.CODEC.fieldOf("color1").forGetter(ColorConfig::bottomLeft),
                DynamicColor.CODEC.fieldOf("color2").forGetter(ColorConfig::bottomRight)
        ).apply(instance, (color, color2) -> new ColorConfig(color, color, color2, color2)));

        private static final MapCodec<ColorConfig> VERTICAL_GRADIENT = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DynamicColor.CODEC.fieldOf("color1").forGetter(ColorConfig::bottomLeft),
                DynamicColor.CODEC.fieldOf("color2").forGetter(ColorConfig::topLeft)
        ).apply(instance, (color, color2) -> new ColorConfig(color, color2, color2, color)));

        private static final MapCodec<ColorConfig> SINGLE_COLOR = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DynamicColor.CODEC.fieldOf("color").forGetter(ColorConfig::bottomLeft)
        ).apply(instance, color -> new ColorConfig(color, color, color, color)));

        private static final MapCodec<ColorConfig> LEGACY_FOUR_ANGLES = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("length_a").forGetter(t -> -1d),
                Codec.DOUBLE.fieldOf("length_b").forGetter(t -> -1d),
                Codec.DOUBLE.fieldOf("gradient_time_a").forGetter(t -> -1d),
                Codec.DOUBLE.fieldOf("gradient_time_b").forGetter(t -> -1d),
                DynamicColor.CODEC.fieldOf("start_color_bottom_left").forGetter(ColorConfig::bottomLeft),
                DynamicColor.CODEC.fieldOf("end_color_bottom_left").forGetter(ColorConfig::bottomLeft),
                Easing.CODEC.fieldOf("color_ease").orElse(SimpleEasing.LINEAR).forGetter(t -> null),
                DynamicColor.CODEC.fieldOf("start_color_top_left").forGetter(ColorConfig::topLeft),
                DynamicColor.CODEC.fieldOf("end_color_top_left").forGetter(ColorConfig::topLeft),
                DynamicColor.CODEC.fieldOf("start_color_bottom_right").forGetter(ColorConfig::bottomRight),
                DynamicColor.CODEC.fieldOf("end_color_top_right").forGetter(ColorConfig::topRight),
                DynamicColor.CODEC.fieldOf("start_color_top_right").forGetter(ColorConfig::topRight),
                DynamicColor.CODEC.fieldOf("end_color_bottom_right").forGetter(ColorConfig::bottomRight)
        ).apply(instance, FadeToColorTransition::legacyFourAngles));

        private static final MapCodec<ColorConfig> LEGACY_TWO_COLORS = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("length_a").forGetter(t -> -1d),
                Codec.DOUBLE.fieldOf("length_b").forGetter(t -> -1d),
                Codec.DOUBLE.fieldOf("gradient_time_a").forGetter(t -> -1d),
                Codec.DOUBLE.fieldOf("gradient_time_b").forGetter(t -> -1d),
                DynamicColor.CODEC.fieldOf("color1").forGetter(ColorConfig::topRight),
                DynamicColor.CODEC.fieldOf("color2").forGetter(ColorConfig::topRight),
                Easing.CODEC.fieldOf("color_ease").orElse(SimpleEasing.LINEAR).forGetter(t -> null)
        ).apply(instance, FadeToColorTransition::legacyTwoColorsCfg));

        private static final Codec<ColorConfig> CODEC = Codec.STRING.dispatch(colorConfig -> "per_angle", s -> switch (s) {
            case "per_angle" -> PER_ANGLE;
            case "vertical_gradient" -> HORIZONTAL_GRADIENT;
            case "horizontal_gradient" -> VERTICAL_GRADIENT;
            case "four_angles" -> LEGACY_FOUR_ANGLES;
            case "two_colors" -> LEGACY_TWO_COLORS;
            default -> SINGLE_COLOR;
        });
    }
}
