package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    // don't attack
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void startAttack(CallbackInfoReturnable<Boolean> callback) {
        if (ClientCutsceneManager.actionToggles().disableAttacking()) {
            callback.setReturnValue(false);
        }
    }

    // don't pick blocks
    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    public void pickBlock(CallbackInfo callback) {
        if (ClientCutsceneManager.actionToggles().disablePickingBlocks()) {
            callback.cancel();
        }
    }

    // don't break blocks
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    public void continueAttack(CallbackInfo callback) {
        if (ClientCutsceneManager.actionToggles().disableBreakingBlocks()) {
            callback.cancel();
        }
    }

    // don't use items
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    public void startUseItem(CallbackInfo callback) {
        if (ClientCutsceneManager.actionToggles().disableUsingItems()) {
            callback.cancel();
        }
    }
}
