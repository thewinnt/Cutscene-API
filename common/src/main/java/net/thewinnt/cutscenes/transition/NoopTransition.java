package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.util.LoadingContext;

public class NoopTransition implements Transition {
    public static final NoopTransition INSTANCE = new NoopTransition();
    public static final MapCodec<NoopTransition> CODEC = MapCodec.unit(INSTANCE);

    private NoopTransition() {}

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene) {
        if (cutscene.path == null) return startPos;
        return cutscene.getPathPoint(0, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
    }

    @Override
    public Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene) {
        if (cutscene.rotationProvider == null) return startRot;
        return cutscene.getRotationAt(0, level, startPos);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.NO_OP;
    }
    
    @Override
    public double getOffCutsceneTime() {
        return 0;
    }

    @Override
    public double getOnCutsceneTime() {
        return 0;
    }

    public static NoopTransition fromNetwork(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static NoopTransition fromJSON(JsonObject json, LoadingContext context) {
        return INSTANCE;
    }
}
