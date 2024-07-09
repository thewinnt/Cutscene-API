package net.thewinnt.cutscenes.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.thewinnt.cutscenes.fabric.CutsceneAPIFabric;
import net.thewinnt.cutscenes.fabric.util.duck.MinecraftExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin implements MinecraftExt {
    @Unique private float realPartialTick;
    @Shadow private volatile boolean pause;
    @Shadow private float pausePartialTick;
    @Shadow @Final private Timer timer;

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", shift = At.Shift.BEFORE))
    private void setRealPartialTick(CallbackInfo info) {
        this.realPartialTick = this.pause ? this.pausePartialTick : this.timer.partialTick;
    }

    @Override
    public float csapi$getPartialTick() {
        return realPartialTick;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void onLogout(CallbackInfo info) {
        // we don't really care about the exact moment here, we just need a good moment to stop it all
        CutsceneAPIFabric.PLATFORM.onLogout.forEach(Runnable::run);
    }
}
