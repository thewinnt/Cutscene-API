package net.thewinnt.cutscenes.networking.packets;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

public class StopCutscenePacket {
    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ClientCutsceneManager.stopCutsceneImmediate();
        });
        context.setPacketHandled(true);
    }
}
