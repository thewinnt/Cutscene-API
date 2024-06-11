package net.thewinnt.cutscenes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    @Inject(
        method = "render",
        at = @At(value = "JUMP"),
        slice = @Slice(
            from = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/GuiGraphics;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0)
        )
    )
    private void cs$render(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci, @Local GuiGraphics guigraphics) {
        // this method gets called several times, but we only want the first one
        if (!ClientCutsceneManager.renderedOverlaysThisFrame) {
            this.minecraft.getProfiler().push("cutscene_overlay");
            if (ClientCutsceneManager.isCutsceneRunning()) {
                CutsceneOverlayManager.render(minecraft, guigraphics, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
            } else {
                CutsceneOverlayManager.clearOverlays();
            }
            this.minecraft.getProfiler().pop();
            ClientCutsceneManager.renderedOverlaysThisFrame = true;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void cs$markNewFrame(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci) {
        ClientCutsceneManager.renderedOverlaysThisFrame = false;
    }
}
