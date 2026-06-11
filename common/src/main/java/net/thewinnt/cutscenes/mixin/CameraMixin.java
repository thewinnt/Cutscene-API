package net.thewinnt.cutscenes.mixin;

import org.jspecify.annotations.Nullable;
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
    @Shadow private float eyeHeightOld;
    @Shadow private float eyeHeight;

    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    // update camera height instantly
    @Inject(method = "setEntity", at = @At("RETURN"))
    public void setEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof CutsceneCameraEntity) {
            this.eyeHeightOld = entity.getEyeHeight();
            this.eyeHeight = entity.getEyeHeight();
        }
    }

    @Inject(method = "alignWithEntity", at = @At("RETURN"))
    private void alignWithEntity(float partialTicks, CallbackInfo ci) {
        if (this.entity instanceof CutsceneCameraEntity camera) {
            this.setPosition(camera.getProperPosition(partialTicks));
        }
    }
}
