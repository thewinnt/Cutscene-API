package net.thewinnt.cutscenes.fabric.mixin;

import net.minecraft.client.Camera;
import net.thewinnt.cutscenes.fabric.util.duck.CameraExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraExt {
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Unique
    public void csapi$setAngles(float pitch, float yaw) {
        this.xRot = yaw;
        this.yRot = pitch;
    }
}
