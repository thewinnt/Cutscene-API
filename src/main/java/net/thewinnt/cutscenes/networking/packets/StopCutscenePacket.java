package net.thewinnt.cutscenes.networking.packets;

import java.util.function.Supplier;

import net.minecraftforge.network.NetworkEvent;
import net.thewinnt.cutscenes.ClientCutsceneManager;

public class StopCutscenePacket {
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ClientCutsceneManager.stopCutscene();
        });
        supplier.get().setPacketHandled(true);
    }
}
