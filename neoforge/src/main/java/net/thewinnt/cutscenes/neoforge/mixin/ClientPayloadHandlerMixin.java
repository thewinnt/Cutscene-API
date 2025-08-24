package net.thewinnt.cutscenes.neoforge.mixin;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(remap = false, targets = "net.neoforged.neoforge.client.network.ClientPayloadHandler")
public class ClientPayloadHandlerMixin {
    @Inject(method = "handle(Lnet/neoforged/neoforge/network/payload/ClientboundCustomSetTimePayload;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V", at = @At("RETURN"))
    private static void handleSetTime(ClientboundCustomSetTimePayload payload, IPayloadContext context, CallbackInfo ci) {
        if (ClientCutsceneManager.isCutsceneRunning()) {
            ClientCutsceneManager.runningCutscene.getTimeManager().syncGameTime(payload.gameTime());
        }
    }
}
