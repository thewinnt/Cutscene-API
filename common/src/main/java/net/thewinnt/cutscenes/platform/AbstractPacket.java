package net.thewinnt.cutscenes.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface AbstractPacket extends CustomPacketPayload {
    void write(FriendlyByteBuf buf);
    void execute();

    @FunctionalInterface
    interface PacketReader {
        AbstractPacket read(FriendlyByteBuf buf);
    }
}
