package net.thewinnt.cutscenes.rotation.handler;

import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.rotation.RotationHandler;
import net.thewinnt.cutscenes.rotation.RotationSerializer;

public class PlayerRotation implements RotationHandler {
    public static final PlayerRotation INSTANCE = new PlayerRotation();

    private PlayerRotation() {}

    @Override
    public Vec3 apply(Vec3 initCamRot, Vec3 startRot, Vec3 playerRot, Vec3 cutsceneRot, double dt) {
        return playerRot;
    }

    @Override
    public RotationSerializer<?> serializer() {
        return RotationSerializer.PLAYER;
    }
}
