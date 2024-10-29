package net.thewinnt.cutscenes.rotation.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.rotation.RotationHandler;
import net.thewinnt.cutscenes.rotation.RotationSerializer;
import net.thewinnt.cutscenes.rotation.serializer.EaseBackSerializer;

public class EaseBackRotation implements RotationHandler {
    public final double decay;
    private Vec3 prevPlayerRot;
    private Vec3 prevOutput;

    public EaseBackRotation(double decay) {
        this.decay = decay;
    }

    @Override
    public Vec3 apply(Vec3 initCamRot, Vec3 startRot, Vec3 playerRot, Vec3 cutsceneRot, double dt) {
        if (dt <= 0) {
            prevPlayerRot = initCamRot;
            prevOutput = cutsceneRot;
            return cutsceneRot;
        }
        Vec3 a = prevOutput.add(playerRot.subtract(prevPlayerRot)); // get current rotation (last + player added)
        Vec3 b = startRot.add(cutsceneRot); // get cutscene rotation
        LocalPlayer player = Minecraft.getInstance().player;
        player.setYRot(((float) prevPlayerRot.x));
        player.setXRot(((float) prevPlayerRot.y));
//        prevPlayerRot = playerRot;
        prevOutput = b.add((a.subtract(b).scale(Math.exp(-decay * dt)))); // lerp towards cutscene rotation
        return prevOutput;
    }

    @Override
    public RotationSerializer<?> serializer() {
        return EaseBackSerializer.INSTANCE;
    }
}
