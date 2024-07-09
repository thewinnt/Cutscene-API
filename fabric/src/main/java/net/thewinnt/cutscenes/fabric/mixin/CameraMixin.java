package net.thewinnt.cutscenes.fabric.mixin;

import net.minecraft.client.Camera;
import net.thewinnt.cutscenes.fabric.util.duck.CameraExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Camera.class)
public class CameraMixin implements CameraExt {
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Override
    public void csapi$setAngles(float pitch, float yaw) {
        this.xRot = pitch;
        this.yRot = yaw;
    }
}
