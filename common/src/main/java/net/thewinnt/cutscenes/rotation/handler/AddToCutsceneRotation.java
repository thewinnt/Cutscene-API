package net.thewinnt.cutscenes.rotation.handler;

import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.rotation.RotationHandler;
import net.thewinnt.cutscenes.rotation.RotationSerializer;

public class AddToCutsceneRotation implements RotationHandler {
    public static final AddToCutsceneRotation INSTANCE = new AddToCutsceneRotation();

    private AddToCutsceneRotation() {}

    @Override
    public Vec3 apply(Vec3 initCamRot, Vec3 startRot, Vec3 playerRot, Vec3 cutsceneRot, double dt) {
        return (startRot.add(cutsceneRot)).add((playerRot.subtract(initCamRot)));
    }

    @Override
    public RotationSerializer<?> serializer() {
        return RotationSerializer.ADD;
    }
}
