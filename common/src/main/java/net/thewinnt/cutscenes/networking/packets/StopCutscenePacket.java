package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public class StopCutscenePacket implements AbstractPacket {
    public static final String ID = "cutscenes:stop_cutscene";

    @Override
    public void execute() {
        ClientCutsceneManager.stopCutsceneImmediate();
    }

    @Override
    public void write(FriendlyByteBuf FriendlyByteBuf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CustomPacketPayload.createType(ID);
    }
}
