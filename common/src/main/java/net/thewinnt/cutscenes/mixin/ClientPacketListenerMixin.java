package net.thewinnt.cutscenes.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void handleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        if (ClientCutsceneManager.isCutsceneRunning()) {
            ClientCutsceneManager.runningCutscene.getTimeManager().syncGameTime(packet.getGameTime());
        }
    }

    @Inject(method = "handleTickingState", at = @At("RETURN"))
    private void handleTickingState(ClientboundTickingStatePacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().level != null && ClientCutsceneManager.isCutsceneRunning()) {
            ClientCutsceneManager.runningCutscene.getTimeManager().setGameTickRate(packet.tickRate());
        }
    }
}
