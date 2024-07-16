package net.thewinnt.cutscenes.neoforge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final protected Minecraft minecraft;

    @Redirect(method = "renderHealthLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getCameraPlayer()Lnet/minecraft/world/entity/player/Player;"))
    public Player csapi$renderHealth(Gui instance) {
        if (ClientCutsceneManager.isCutsceneRunning()) {
            return minecraft.player;
        }
        return (minecraft.getCameraEntity() instanceof Player player) ? player : null;
    }
}
