package net.thewinnt.cutscenes.mixin;

import net.thewinnt.cutscenes.CutsceneType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.entity.CutsceneCameraEntity;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getPosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    public void getPosition(float partialTick, CallbackInfoReturnable<Vec3> callback) {
        CutsceneType cutscene = ClientCutsceneManager.runningCutscene;
        if (cutscene == null) return;
        if (((Entity)(Object)this) instanceof CutsceneCameraEntity camera) {
            Vec3 pos = camera.getProperPosition(partialTick);
            if (pos == null) return;
            callback.setReturnValue(pos);
        }
    }

    @Inject(method = "shouldRender(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void shouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> callback) {
        if (ClientCutsceneManager.isCutsceneRunning() && this.equals(Minecraft.getInstance().player)) {
            callback.setReturnValue(false);
        }
    }
}
