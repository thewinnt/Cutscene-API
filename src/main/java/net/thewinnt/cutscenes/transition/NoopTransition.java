package net.thewinnt.cutscenes.transition;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;

public class NoopTransition implements Transition {
    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Vec3 getPos(double progress, Level level, Vec3 startPos, Vec3 pathRot, Vec3 initCamPos, CutsceneType cutscene) {
        return cutscene.getPathPoint(0, level, startPos).yRot((float)pathRot.y).zRot((float)pathRot.z).xRot((float)pathRot.x).add(startPos);
    }

    @Override
    public Vec3 getRot(double progress, Level level, Vec3 startPos, Vec3 startRot, Vec3 initCamRot, CutsceneType cutscene) {
        return cutscene.getRotationAt(0, level, startPos);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}

    @Override
    public TransitionSerializer<?> getSerializer() {
        return CutsceneManager.NO_OP;
    }

    @Override
    public boolean countTowardsCutsceneTime() {
        return false;
    }

    public static NoopTransition fromNetwork(FriendlyByteBuf buf) {
        return new NoopTransition();
    }

    public static NoopTransition fromJSON(JsonObject json) {
        return new NoopTransition();
    }
}
