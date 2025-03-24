package net.thewinnt.cutscenes.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.thewinnt.cutscenes.effect.HideChunksEffect;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void renderLevel(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo callback) {
        if (HideChunksEffect.active()) {
            callback.cancel();
        }
    }
}
