package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.client.ClientCutsceneManager.CutsceneStatus;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "Lnet/minecraft/world/entity/Entity;getPosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    public void getPosition(float partialTick, CallbackInfoReturnable<Vec3> callback) {
        if (((Entity)(Object)this) instanceof CutsceneCameraEntity camera) {
            callback.setReturnValue(camera.getProperPosition(partialTick));
        }
    }

    @Inject(method = "Lnet/minecraft/world/entity/Entity;shouldRender(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void shouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> callback) {
        if (ClientCutsceneManager.cutsceneStatus != CutsceneStatus.NONE && ((Entity)(Object)this).equals(Minecraft.getInstance().player)) {
            callback.setReturnValue(false);
        }
    }
}
