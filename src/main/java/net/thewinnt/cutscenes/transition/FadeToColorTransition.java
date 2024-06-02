package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;
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
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.util.DynamicColor;
import net.thewinnt.cutscenes.util.JsonHelper;

import java.util.List;
import java.util.Objects;

public class FadeToColorTransition implements Transition {
    private final DynamicColor colorBottomLeft;
    private final DynamicColor colorTopLeft;
    private final DynamicColor colorTopRight;
    private final DynamicColor colorBottomRight;
    private final int lengthA;
    private final int lengthB;
    private final double progressLengthA;
    private final double progressLengthB;
    private final Easing easeIn;
    private final Easing easeOut;
    private final boolean isStart;
    private FadeToColorOverlayConfiguration config;

    public FadeToColorTransition(DynamicColor colorBottomLeft, DynamicColor colorTopLeft, DynamicColor colorTopRight, DynamicColor colorBottomRight, int lengthA, int lengthB, Easing easeIn, Easing easeOut, boolean isStart) {
        this.colorBottomLeft = colorBottomLeft;
        this.colorTopLeft = colorTopLeft;
        this.colorTopRight = colorTopRight;
        this.colorBottomRight = colorBottomRight;
        this.lengthA = lengthA;
        this.lengthB = lengthB;
        this.progressLengthA = (double)lengthA / (lengthA + lengthB);
        this.progressLengthB = (double)lengthB / (lengthA + lengthB);
        this.easeIn = easeIn;
        this.easeOut = easeOut;
        this.isStart = isStart;
    }

    public FadeToColorTransition(DynamicColor color, int lengthA, int lengthB, Easing easeIn, Easing easeOut, boolean isStart) {
        this(color, color, color, color, lengthA, lengthB, easeIn, easeOut, isStart);
    }

    public FadeToColorTransition(DynamicColor color, int length, Easing easeIn, Easing easeOut, boolean isStart) {
        this(color, length, length, easeIn, easeOut, isStart);
    }

    @Override
    public int getLength() {
        return lengthA + lengthB;
    }

    @Override
    public int getOffCutsceneTime() {
        return isStart ? lengthA : lengthB;
    }

    @Override
    public int getOnCutsceneTime() {
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
                double cutsceneProgress = (progress - progressLengthA) / progressLengthA * lengthA / cutscene.length;
                return cutscene.getPathPoint(cutsceneProgress, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
            }
        } else {
            if (progress > progressLengthA) {
                return initCamPos;
            } else if (cutscene.path == null) {
                return startPos;
            } else {
                double cutsceneProgress = (cutscene.length - lengthA + lengthA * (progress / progressLengthA)) / cutscene.length;
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
                double cutsceneProgress = (progress - progressLengthA) / progressLengthA * lengthA / cutscene.length;
                return cutscene.getRotationAt(cutsceneProgress, level, startPos).add(startRot);
            }
        } else {
            if (progress > progressLengthA) {
                return initCamRot;
            } else if (cutscene.rotationProvider == null) {
                return startRot;
            } else {
                double cutsceneProgress = (cutscene.length - lengthA + lengthA * (progress / progressLengthA)) / cutscene.length;
                return cutscene.getRotationAt(cutsceneProgress, level, startPos).add(startRot);
            }
        }
    }

    @Override
    public void onStart(CutsceneType cutscene) {
        this.config = new FadeToColorOverlayConfiguration(colorBottomLeft, colorTopLeft, colorTopRight, colorBottomRight, 0);
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
        colorBottomLeft.toNetwork(buf);
        colorTopLeft.toNetwork(buf);
        colorTopRight.toNetwork(buf);
        colorBottomRight.toNetwork(buf);
        buf.writeInt(lengthA);
        buf.writeInt(lengthB);
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
        int lengthA = buf.readInt();
        int lengthB = buf.readInt();
        Easing easeIn = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easeOut = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        boolean isStart = buf.readBoolean();
        return new FadeToColorTransition(colorBottomLeft, colorTopLeft, colorTopRight, colorBottomRight, lengthA, lengthB, easeIn, easeOut, isStart);
    }

    public static FadeToColorTransition fromJSON(JsonObject json) {
        int lengthA = GsonHelper.getAsInt(json, "length_a");
        int lengthB = GsonHelper.getAsInt(json, "length_b", lengthA);
        Easing easeIn = Easing.fromJSON(json.get("ease_in"), SimpleEasing.LINEAR);
        Easing easeOut = Easing.fromJSON(json.get("ease_out"), SimpleEasing.LINEAR);
        boolean isStart = GsonHelper.getAsBoolean(json, "is_start");
        String colorType = GsonHelper.getAsString(json, "color_definition", "single_color");
        switch (colorType) {
            case "per_angle" -> {
                DynamicColor colorBottomLeft = DynamicColor.fromJSON(json.get("bottom_left"));
                DynamicColor colorTopLeft = DynamicColor.fromJSON(json.get("top_left"));
                DynamicColor colorTopRight = DynamicColor.fromJSON(json.get("top_right"));
                DynamicColor colorBottomRight = DynamicColor.fromJSON(json.get("bottom_right"));
                return new FadeToColorTransition(colorBottomLeft, colorTopLeft, colorTopRight, colorBottomRight, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "horizontal_gradient" -> {
                DynamicColor color1 = DynamicColor.fromJSON(json.get("color1"));
                DynamicColor color2 = DynamicColor.fromJSON(json.get("color2"));
                return new FadeToColorTransition(color1, color1, color2, color2, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "vertical_gradient" -> {
                DynamicColor color1 = DynamicColor.fromJSON(json.get("color1"));
                DynamicColor color2 = DynamicColor.fromJSON(json.get("color2"));
                return new FadeToColorTransition(color1, color2, color2, color1, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "four_angles" -> {
                DynamicColor[] colors = legacyFourAngles(json, lengthA, lengthB);
                return new FadeToColorTransition(colors[0], colors[1], colors[2], colors[3], lengthA, lengthB, easeIn, easeOut, isStart);
            }
            case "two_colors" -> {
                DynamicColor color = legacyTwoColors(json, lengthA, lengthB);
                return new FadeToColorTransition(color, lengthA, lengthB, easeIn, easeOut, isStart);
            }
            default -> {
                DynamicColor color = DynamicColor.fromJSON(json.get("color"));
                return new FadeToColorTransition(color, lengthA, lengthB, easeIn, easeOut, isStart);
            }
        }
    }

    private static DynamicColor[] legacyFourAngles(JsonObject json, int lengthA, int lengthB) {
        DynamicColor startColorBottomLeft = DynamicColor.fromJSON(json.get("start_color_bottom_left"));
        DynamicColor startColorTopLeft = DynamicColor.fromJSON(json.get("start_color_top_left"));
        DynamicColor startColorTopRight = DynamicColor.fromJSON(json.get("start_color_top_right"));
        DynamicColor startColorBottomRight = DynamicColor.fromJSON(json.get("start_color_bottom_right"));
        DynamicColor endColorBottomLeft = DynamicColor.fromJSON(json.get("end_color_bottom_left"));
        DynamicColor endColorTopLeft = DynamicColor.fromJSON(json.get("end_color_top_left"));
        DynamicColor endColorTopRight = DynamicColor.fromJSON(json.get("end_color_top_right"));
        DynamicColor endColorBottomRight = DynamicColor.fromJSON(json.get("end_color_bottom_right"));

        int gradientTimeA = GsonHelper.getAsInt(json, "gradient_time_a", lengthA);
        int gradientTimeB = GsonHelper.getAsInt(json, "gradient_time_b", lengthA);
        Easing colorEase = Easing.fromJSON(json.get("color_ease"), SimpleEasing.LINEAR);

        double progressGA = (double) gradientTimeA / (lengthA + lengthB);
        double progressGB = (double) gradientTimeB / (lengthA + lengthB);
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
        return new DynamicColor[]{bottomLeft, topLeft, topRight, bottomRight};
    }

    private static DynamicColor legacyTwoColors(JsonObject json, int lengthA, int lengthB) {
        DynamicColor color1 = DynamicColor.fromJSON(json.get("color1"));
        DynamicColor color2 = DynamicColor.fromJSON(json.get("color2"));

        int gradientTimeA = GsonHelper.getAsInt(json, "gradient_time_a", lengthA);
        int gradientTimeB = GsonHelper.getAsInt(json, "gradient_time_b", lengthA);
        Easing colorEase = Easing.fromJSON(json.get("color_ease"), SimpleEasing.LINEAR);

        double progressGA = (double) gradientTimeA / (lengthA + lengthB);
        double progressGB = (double) gradientTimeB / (lengthA + lengthB);
        return new DynamicColor(
            createCompound(progressGA, progressGB, color1.r(), color2.r(), colorEase),
            createCompound(progressGA, progressGB, color1.g(), color2.g(), colorEase),
            createCompound(progressGA, progressGB, color1.b(), color2.b(), colorEase),
            createCompound(progressGA, progressGB, color1.a(), color2.a(), colorEase)
        );
    }

    private static CompoundEasing createCompound(double progressGA, double progressGB, Easing color1, Easing color2, Easing colorDelta) {
        ColorEasing colorEasing = new ColorEasing(colorDelta, color1, color2);
        return new CompoundEasing(List.of(
            new CompoundEasing.TimedEasingEntry(0, new CompoundEasing.RangeAppliedEasing(0, 1, new ConstantEasing(colorEasing.get(0)))),
            new CompoundEasing.TimedEasingEntry(progressGA, new CompoundEasing.RangeAppliedEasing(0, 1, colorEasing)),
            new CompoundEasing.TimedEasingEntry(progressGB, new CompoundEasing.RangeAppliedEasing(0, 1, new ConstantEasing(colorEasing.get(1))))
        ));
    }
}
