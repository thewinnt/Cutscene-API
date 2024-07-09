package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

@Mixin(Gui.class)
public class GuiMixin {
    // show the real player's hud
    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void getCameraPlayer(CallbackInfoReturnable<Player> cir) {
        if (ClientCutsceneManager.isCutsceneRunning()) {
            Minecraft minecraft = Minecraft.getInstance(); // prevent warning
            cir.setReturnValue(minecraft.player);
        }
    }
}
