package net.thewinnt.cutscenes.mixin;

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
        if (!ClientCutsceneManager.isCutsceneRunning()) return;
        if (((Entity)(Object)this) instanceof CutsceneCameraEntity camera) {
            Minecraft.getInstance().getProfiler().push("cutscene_position");
            Vec3 pos = camera.getProperPosition(partialTick);
            Minecraft.getInstance().getProfiler().pop();
            if (pos == null) return;
            callback.setReturnValue(pos);
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    @Inject(method = "shouldRender(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void shouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> callback) {
        if (ClientCutsceneManager.isCutsceneRunning() && this.equals(Minecraft.getInstance().player) && ClientCutsceneManager.actionToggles().hideSelf()) {
            callback.setReturnValue(false);
        }
    }
}
