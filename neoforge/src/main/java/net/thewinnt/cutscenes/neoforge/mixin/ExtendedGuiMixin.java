package net.thewinnt.cutscenes.neoforge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExtendedGui.class)
public class ExtendedGuiMixin {
    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    public Entity csapi$renderHealth(Minecraft instance) {
        if (ClientCutsceneManager.isCutsceneRunning()) {
            return instance.player;
        }
        return instance.getCameraEntity();
    }
}
