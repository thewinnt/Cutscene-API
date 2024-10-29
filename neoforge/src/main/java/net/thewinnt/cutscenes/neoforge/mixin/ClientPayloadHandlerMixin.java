package net.thewinnt.cutscenes.neoforge.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPayloadHandler.class, remap = false)
public class ClientPayloadHandlerMixin {
    @Inject(method = "handle(Lnet/neoforged/neoforge/network/payload/ClientboundCustomSetTimePayload;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V", at = @At("RETURN"))
    private static void handleSetTime(ClientboundCustomSetTimePayload payload, IPayloadContext context, CallbackInfo ci) {
        if (ClientCutsceneManager.isCutsceneRunning()) {
            ClientCutsceneManager.runningCutscene.getTimeManager().syncGameTime(payload.gameTime());
        }
    }
}
