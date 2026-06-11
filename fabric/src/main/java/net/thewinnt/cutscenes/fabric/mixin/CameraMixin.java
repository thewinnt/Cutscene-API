package net.thewinnt.cutscenes.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.fabric.CameraAngleSetterImpl;
import net.thewinnt.cutscenes.fabric.client.CutsceneAPIFabricClient;
import net.thewinnt.cutscenes.fabric.util.duck.CameraExt;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraExt {
    @Shadow @Final private static Vector3f FORWARDS;
    @Shadow @Final private static Vector3f UP;
    @Shadow @Final private static Vector3f LEFT;
    @Shadow @Final private Quaternionf rotation;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;

    @Shadow
    private int matrixPropertiesDirty;

    @WrapOperation(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void setup(Camera instance, float yRot, float xRot, Operation<Void> original) {
        if (!ClientCutsceneManager.isCutsceneRunning()) {
            original.call(instance, yRot, xRot); // keep it non-intrusive
        } else {
            CameraAngleSetterImpl event = new CameraAngleSetterImpl(yRot, xRot, 0.0F);
            CutsceneAPIFabricClient.CLIENT_PLATFORM.angleSetters.forEach(consumer -> consumer.accept(event));
            this.setRotation(event.getYaw(), event.getPitch(), event.getRoll());
        }
    }

    @Unique
    private void setRotation(float yRot, float xRot, float roll) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation.rotationYXZ((float) Math.PI - yRot * (float) (Math.PI / 180.0), -xRot * (float) (Math.PI / 180.0), -roll * (float) (Math.PI / 180.0));
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
        this.matrixPropertiesDirty |= 3;
    }
}
