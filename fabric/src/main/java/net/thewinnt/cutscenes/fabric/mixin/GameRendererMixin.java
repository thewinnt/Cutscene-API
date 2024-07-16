package net.thewinnt.cutscenes.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.thewinnt.cutscenes.fabric.CameraAngleSetterImpl;
import net.thewinnt.cutscenes.fabric.client.CutsceneAPIFabricClient;
import net.thewinnt.cutscenes.fabric.util.duck.CameraExt;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera mainCamera;
    @Unique private CameraAngleSetterImpl impl = new CameraAngleSetterImpl(0, 0, 0);

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void cameraAnglesEvent(float partialTick, long nanoTime, CallbackInfo ci) {
        this.impl = new CameraAngleSetterImpl(mainCamera.getXRot(), mainCamera.getYRot(), 0);
        CutsceneAPIFabricClient.CLIENT_PLATFORM.angleSetters.forEach(consumer -> consumer.accept(impl));
        ((CameraExt)mainCamera).csapi$setAngles(impl.getPitch(), impl.getYaw());
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.BEFORE))
    private void applyRoll(float partialTick, long nanoTime, CallbackInfo ci, @Local(ordinal = 1) LocalRef<Matrix4f> matrix4f) {
        matrix4f.set(new Matrix4f().rotationZ(impl.getRoll() * 0.017453292F).mul(matrix4f.get()));
    }
}
