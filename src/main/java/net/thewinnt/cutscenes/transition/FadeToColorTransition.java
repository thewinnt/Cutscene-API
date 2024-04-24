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
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.util.JsonHelper;

public class FadeToColorTransition implements Transition {
    private final float[] startColorBottomLeft;
    private final float[] startColorTopLeft;
    private final float[] startColorTopRight;
    private final float[] startColorBottomRight;
    private final float[] endColorBottomLeft;
    private final float[] endColorTopLeft;
    private final float[] endColorTopRight;
    private final float[] endColorBottomRight;
    private final int lengthA;
    private final int lengthB;
    private final double progressLengthA;
    private final double progressLengthB;
    private final int gradientTimeA;
    private final int gradientTimeB;
    private final Easing easeIn;
    private final Easing easeOut;
    private final Easing colorEase;
    private final boolean isStart;
    private FadeToColorOverlayConfiguration config;

    public FadeToColorTransition(float[] startColorBottomLeft, float[] startColorTopLeft, float[] startColorTopRight, float[] startColorBottomRight, float[] endColorBottomLeft, float[] endColorTopLeft, float[] endColorTopRight, float[] endColorBottomRight, int lengthA, int lengthB, int gradientTimeA, int gradientTimeB, Easing easeIn, Easing easeOut, Easing colorEase, boolean isStart) {
        this.startColorBottomLeft = startColorBottomLeft;
        this.startColorTopLeft = startColorTopLeft;
        this.startColorTopRight = startColorTopRight;
        this.startColorBottomRight = startColorBottomRight;
        this.endColorBottomLeft = endColorBottomLeft;
        this.endColorTopLeft = endColorTopLeft;
        this.endColorTopRight = endColorTopRight;
        this.endColorBottomRight = endColorBottomRight;
        this.lengthA = lengthA;
        this.lengthB = lengthB;
        this.progressLengthA = (double)lengthA / (lengthA + lengthB);
        this.progressLengthB = (double)lengthB / (lengthA + lengthB);
        this.gradientTimeA = gradientTimeA;
        this.gradientTimeB = gradientTimeB;
        this.easeIn = easeIn;
        this.easeOut = easeOut;
        this.colorEase = colorEase;
        this.isStart = isStart;
    }

    public FadeToColorTransition(float[] color1, float[] color2, int lengthA, int lengthB, int gradientTimeA, int gradientTimeB, Easing easeIn, Easing easeOut, Easing colorEase, boolean isStart) {
        this(color1, color1, color1, color1, color2, color2, color2, color2, lengthA, lengthB, gradientTimeA, gradientTimeB, easeIn, easeOut, colorEase, isStart);
    }

    public FadeToColorTransition(float[] color, int lengthA, int lengthB, Easing easeIn, Easing easeOut, boolean isStart) {
        this(color, color, lengthA, lengthB, 0, lengthA, easeIn, easeOut, SimpleEasing.LINEAR, isStart);
    }

    public FadeToColorTransition(float[] color, int length, Easing easeIn, Easing easeOut, boolean isStart) {
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
        this.config = new FadeToColorOverlayConfiguration(startColorBottomLeft, startColorTopLeft, startColorTopRight, startColorBottomRight, 0);
        CutsceneOverlayManager.addOverlay(FadeToColorOverlay.INSTANCE, this.config);
    }

    @Override
    public void onFrame(double progress, CutsceneType cutscene) {
        if (progress < progressLengthA) {
            this.config.setAlpha((float)easeIn.get(progress / progressLengthA));
        } else {
            this.config.setAlpha((float)easeOut.get((1 - progress + progressLengthA) / progressLengthB));
        }
        double colorLerpStartProgress = (double)gradientTimeA / (lengthA + lengthB);
        double colorLerpEndProgress = (double)gradientTimeB / (lengthA + lengthB);
        if (progress < colorLerpStartProgress) {
            this.config.setColors(startColorBottomLeft, startColorTopLeft, startColorTopRight, startColorBottomRight);
        } else if (progress > colorLerpEndProgress) {
            this.config.setColors(endColorBottomLeft, endColorTopLeft, endColorTopRight, endColorBottomRight);
        } else {
            float lerpProgress = (float)colorEase.get((progress - colorLerpStartProgress) / (colorLerpEndProgress - colorLerpStartProgress));
            this.config.setColorBottomLeft(lerpColor(startColorBottomLeft, endColorBottomLeft, lerpProgress));
            this.config.setColorTopLeft(lerpColor(startColorTopLeft, endColorTopLeft, lerpProgress));
            this.config.setColorTopRight(lerpColor(startColorTopRight, endColorTopRight, lerpProgress));
            this.config.setColorBottomRight(lerpColor(startColorBottomRight, endColorBottomRight, lerpProgress));
        }
    }

    @Override
    public void onEnd(CutsceneType cutscene) {
        CutsceneOverlayManager.removeOverlay(FadeToColorOverlay.INSTANCE, this.config);
        this.config.setAlpha(0);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        CutsceneNetworkHandler.writeColorRGBA(buf, startColorBottomLeft);
        CutsceneNetworkHandler.writeColorRGBA(buf, startColorTopLeft);
        CutsceneNetworkHandler.writeColorRGBA(buf, startColorTopRight);
        CutsceneNetworkHandler.writeColorRGBA(buf, startColorBottomRight);
        CutsceneNetworkHandler.writeColorRGBA(buf, endColorBottomLeft);
        CutsceneNetworkHandler.writeColorRGBA(buf, endColorTopLeft);
        CutsceneNetworkHandler.writeColorRGBA(buf, endColorTopRight);
        CutsceneNetworkHandler.writeColorRGBA(buf, endColorBottomRight);
        buf.writeInt(lengthA);
        buf.writeInt(lengthB);
        buf.writeInt(gradientTimeA);
        buf.writeInt(gradientTimeB);
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easeIn.getSerializer()));
        easeIn.toNetwork(buf);
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(easeOut.getSerializer()));
        easeOut.toNetwork(buf);
        buf.writeInt(CutsceneAPI.EASING_SERIALIZERS.getId(colorEase.getSerializer()));
        colorEase.toNetwork(buf);
        buf.writeBoolean(isStart);
    }

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.FADE;
    }

    public static FadeToColorTransition fromNetwork(FriendlyByteBuf buf) {
        float[] startColorBottomLeft = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] startColorTopLeft = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] startColorTopRight = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] startColorBottomRight = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] endColorBottomLeft = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] endColorTopLeft = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] endColorTopRight = CutsceneNetworkHandler.readColorRGBA(buf);
        float[] endColorBottomRight = CutsceneNetworkHandler.readColorRGBA(buf);
        int lengthA = buf.readInt();
        int lengthB = buf.readInt();
        int gradientTimeA = buf.readInt();
        int gradientTimeB = buf.readInt();
        Easing easeIn = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing easeOut = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        Easing colorEase = CutsceneAPI.EASING_SERIALIZERS.byId(buf.readInt()).fromNetwork(buf);
        boolean isStart = buf.readBoolean();
        return new FadeToColorTransition(startColorBottomLeft, startColorTopLeft, startColorTopRight, startColorBottomRight, endColorBottomLeft, endColorTopLeft, endColorTopRight, endColorBottomRight, lengthA, lengthB, gradientTimeA, gradientTimeB, easeIn, easeOut, colorEase, isStart);
    }

    public static FadeToColorTransition fromJSON(JsonObject json) {
        int lengthA = GsonHelper.getAsInt(json, "length_a");
        int lengthB = GsonHelper.getAsInt(json, "length_b", lengthA);
        int gradientTimeA = GsonHelper.getAsInt(json, "gradient_time_a", lengthA);
        int gradientTimeB = GsonHelper.getAsInt(json, "gradient_time_b", lengthA);
        Easing easeIn = Easing.fromJSON(json.get("ease_in"), SimpleEasing.LINEAR);
        Easing easeOut = Easing.fromJSON(json.get("ease_out"), SimpleEasing.LINEAR);
        Easing colorEase = Easing.fromJSON(json.get("color_ease"), SimpleEasing.LINEAR);
        boolean isStart = GsonHelper.getAsBoolean(json, "is_start");
        String colorType = GsonHelper.getAsString(json, "color_definition", "single_color");
        if ("four_angles".equals(colorType)) {
            float[] startColorBottomLeft = JsonHelper.getColor(json, "start_color_bottom_left", 1);
            float[] startColorTopLeft = JsonHelper.getColor(json, "start_color_top_left", 1);
            float[] startColorTopRight = JsonHelper.getColor(json, "start_color_top_right", 1);
            float[] startColorBottomRight = JsonHelper.getColor(json, "start_color_bottom_right", 1);
            float[] endColorBottomLeft = JsonHelper.getColor(json, "end_color_bottom_left", 1);
            float[] endColorTopLeft = JsonHelper.getColor(json, "end_color_top_left", 1);
            float[] endColorTopRight = JsonHelper.getColor(json, "end_color_top_right", 1);
            float[] endColorBottomRight = JsonHelper.getColor(json, "end_color_bottom_right", 1);
            return new FadeToColorTransition(startColorBottomLeft, startColorTopLeft, startColorTopRight, startColorBottomRight, endColorBottomLeft, endColorTopLeft, endColorTopRight, endColorBottomRight, lengthA, lengthB, gradientTimeA, gradientTimeB, easeIn, easeOut, colorEase, isStart);
        } else if ("two_colors".equals(colorType)) {
            float[] color1 = JsonHelper.getColor(json, "color1", 1);
            float[] color2 = JsonHelper.getColor(json, "color1", 1);
            return new FadeToColorTransition(color1, color2, lengthA, lengthB, gradientTimeA, gradientTimeB, easeIn, easeOut, colorEase, isStart);
        } else {
            float[] color = JsonHelper.getColor(json, "color", 1);
            return new FadeToColorTransition(color, lengthA, lengthB, easeIn, easeOut, isStart);
        }
    }

    public static float[] lerpColor(float[] from, float[] to, float progress) {
        from = new float[]{from[0] * from[0], from[1] * from[1], from[2] * from[2], from[3]};
        to = new float[]{to[0] * to[0], to[1] * to[1], to[2] * to[2], to[3]};
        float[] output = new float[]{Mth.lerp(progress, from[0], to[0]), Mth.lerp(progress, from[1], to[1]), Mth.lerp(progress, from[2], to[2]), Mth.lerp(progress, from[3], to[3])};
        return new float[]{Mth.sqrt(output[0]), Mth.sqrt(output[1]), Mth.sqrt(output[2]), output[3]};
    }
}
