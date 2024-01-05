package net.thewinnt.cutscenes.networking.packets;

import java.util.function.Supplier;

import net.minecraftforge.network.NetworkEvent;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

public class StopCutscenePacket {
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ClientCutsceneManager.stopCutsceneImmediate();
        });
        supplier.get().setPacketHandled(true);
    }
}
