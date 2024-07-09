package net.thewinnt.cutscenes.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.thewinnt.cutscenes.fabric.CameraAngleSetterImpl;
import net.thewinnt.cutscenes.fabric.CutsceneAPIFabric;
import net.thewinnt.cutscenes.fabric.util.duck.CameraExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void cameraAnglesEvent(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        CameraAngleSetterImpl impl = new CameraAngleSetterImpl(mainCamera.getXRot(), mainCamera.getYRot(), 0);
        CutsceneAPIFabric.PLATFORM.angleSetters.forEach(consumer -> consumer.accept(impl));
        ((CameraExt)mainCamera).csapi$setAngles(impl.getPitch(), impl.getYaw());
        poseStack.mulPose(Axis.ZP.rotationDegrees(impl.getRoll()));
    }
}
