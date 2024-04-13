package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.client.ClientCutsceneManager.CutsceneStatus;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    // don't interact with blocks
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void interactBlock(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> callback) {
        if (ClientCutsceneManager.actionToggles().disableBlockInteractions()) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }

    // don't interact with entities
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void interactEntity(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        if (ClientCutsceneManager.actionToggles().disableEntityInteractions()) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }

    // don't interact with entities
    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    public void interactEntityAtLocation(Player player, Entity target, EntityHitResult ray, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        if (ClientCutsceneManager.actionToggles().disableEntityInteractions()) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }

    // don't attack self
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attackEntity(Player player, Entity target, CallbackInfo callback) {
        if (ClientCutsceneManager.actionToggles().disableAttacking() || target.equals(player)) {
            callback.cancel();
        }
    }
}
