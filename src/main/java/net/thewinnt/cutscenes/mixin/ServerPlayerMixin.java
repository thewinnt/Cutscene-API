package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.thewinnt.cutscenes.util.ServerPlayerExt;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerExt {
    public int cutsceneTicksRemaining;

    @Override
    public int getCutsceneTicks() {
        return cutsceneTicksRemaining;
    }

    @Override
    public void setCutsceneTicks(int value) {
        cutsceneTicksRemaining = value;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if (cutsceneTicksRemaining > 0) {
            cutsceneTicksRemaining--;
        }
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isSpectator(CallbackInfoReturnable<Boolean> callback) {
        if (cutsceneTicksRemaining > 0) {
            callback.setReturnValue(true);
        }
    }
}
