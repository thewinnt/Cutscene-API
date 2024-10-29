package net.thewinnt.cutscenes.rotation.handler;

import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.rotation.RotationHandler;
import net.thewinnt.cutscenes.rotation.RotationSerializer;

public class CutsceneRotation implements RotationHandler {
    public static final CutsceneRotation INSTANCE = new CutsceneRotation();

    private CutsceneRotation() {}

    @Override
    public Vec3 apply(Vec3 initCamRot, Vec3 startRot, Vec3 playerRot, Vec3 cutsceneRot, double dt) {
        return startRot.add(cutsceneRot);
    }

    @Override
    public RotationSerializer<?> serializer() {
        return RotationSerializer.CUTSCENE;
    }
}
