package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow private Entity entity;
    @Shadow private float eyeHeightOld;
    @Shadow private float eyeHeight;

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
}
