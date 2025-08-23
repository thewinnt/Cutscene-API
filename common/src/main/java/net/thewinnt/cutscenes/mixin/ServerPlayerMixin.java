package net.thewinnt.cutscenes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.event.EndingReason;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    @Unique private String cutscenes$startReason = "";
    @Unique private EndingReason cutscenes$endReason;

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
    public void csapi$setRunningCutscene(CutsceneType type, String startingReason) {
        this.cutscenes$running = type;
        this.cutscenes$startReason = startingReason;
    }

    @Override
    public void csapi$finishCutscene(EndingReason reason) {
        if (cutscenes$running != null) {
            this.cutscenes$endReason = reason;
            CutsceneEvents.CUTSCENE_OVER_SERVER.invoke(listener -> listener.accept(cutscenes$running, CutsceneManager.REGISTRY.inverse().get(cutscenes$running), ((ServerPlayer) (Object) this), reason));
            this.cutscenes$running = null;
            this.cutscenes$ticksRemaining = 0;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if (cutscenes$ticksRemaining > 0) {
            cutscenes$ticksRemaining--;
            if (cutscenes$ticksRemaining == 0) {
                this.csapi$finishCutscene(EndingReason.FINISH);
            }
        } else if (cutscenes$ticksRemaining == 0) {
            cutscenes$startReason = "";
            cutscenes$endReason = null;
            cutscenes$ticksRemaining--;
        }
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isSpectator(CallbackInfoReturnable<Boolean> callback) {
        if (cutscenes$ticksRemaining > 0 && cutscenes$running != null && cutscenes$running.actionToggles.considerSpectator()) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void hurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        if (cutscenes$running != null && cutscenes$running.actionToggles.disableDamage()) {
            callback.setReturnValue(false);
        }
    }

    @Override
    public boolean csapi$isWatchingCutscene() {
        return cutscenes$ticksRemaining > 0;
    }

    @Override
    public String csapi$getStartReason() {
        return cutscenes$startReason;
    }

    @Override
    public EndingReason csapi$getEndReason() {
        return cutscenes$endReason;
    }
}
