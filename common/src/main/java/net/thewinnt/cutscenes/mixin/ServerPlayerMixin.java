package net.thewinnt.cutscenes.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.event.EndingReason;
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

    @Override
    public void csapi$finishCutscene(EndingReason reason) {
        if (cutscenes$running != null) {
            CutsceneEvents.CUTSCENE_OVER_SERVER.invoke(listener -> listener.accept(cutscenes$running, CutsceneManager.REGISTRY.inverse().get(cutscenes$running), ((ServerPlayer) (Object) this), reason));
            cutscenes$running = null;
            cutscenes$ticksRemaining = 0;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if (cutscenes$ticksRemaining > 0) {
            cutscenes$ticksRemaining--;
            if (cutscenes$ticksRemaining == 0) {
                this.csapi$finishCutscene(EndingReason.FINISH);
            }
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
