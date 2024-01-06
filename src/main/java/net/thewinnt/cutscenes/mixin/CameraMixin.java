package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Entity entity;
    @Shadow private float eyeHeightOld;
    @Shadow private float eyeHeight;
    @Shadow protected abstract void setPosition(Vec3 pos);

    // update camera height instantly
    @Inject(method = "setup", at = @At("HEAD"))
    public void setup(BlockGetter level, Entity newEntity, boolean thirdPerson, boolean inverseView, float partialTick, CallbackInfo callback) {
        if (newEntity == null || this.entity == null || newEntity.equals(this.entity)) {
            return;
        }

        if (newEntity instanceof CutsceneCameraEntity || this.entity instanceof CutsceneCameraEntity) {
            this.eyeHeightOld = newEntity.getEyeHeight();
            this.eyeHeight = newEntity.getEyeHeight();
        }
    }

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = Shift.AFTER))
    public void setPositionCorrect(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo callback) {
        if (pEntity instanceof CutsceneCameraEntity camera) {
            this.setPosition(camera.getPosition(pPartialTick));
        }
    }
}
