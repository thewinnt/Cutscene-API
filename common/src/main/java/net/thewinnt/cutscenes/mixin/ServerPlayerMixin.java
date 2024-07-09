package net.thewinnt.cutscenes.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.thewinnt.cutscenes.CutsceneType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.thewinnt.cutscenes.util.ServerPlayerExt;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerExt {
    @Unique private CutsceneType cutscenes$running;
    @Unique private int cutscenes$ticksRemaining;

    @Override
    public int csapi$getCutsceneTicks() {
        return cutscenes$ticksRemaining;
    }

    @Override
    public void csapi$setCutsceneTicks(int value) {
        cutscenes$ticksRemaining = value;
    }

    @Nullable
    @Override
    public CutsceneType csapi$getRunningCutscene() {
        return cutscenes$running;
    }

    @Override
    public void csapi$setRunningCutscene(CutsceneType type) {
        cutscenes$running = type;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if (cutscenes$ticksRemaining > 0) {
            cutscenes$ticksRemaining--;
        } else {
            cutscenes$running = null;
        }
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isSpectator(CallbackInfoReturnable<Boolean> callback) {
        if (cutscenes$ticksRemaining > 0 && cutscenes$running != null && cutscenes$running.actionToggles.considerSpectator()) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        if (cutscenes$running != null && cutscenes$running.actionToggles.disableDamage()) {
            callback.setReturnValue(false);
        }
    }
}
