package net.thewinnt.cutscenes.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public record PacketType<T extends AbstractPacket>(CustomPacketPayload.Type<T> type, AbstractPacket.PacketReader<T> reader, Consumer<T> handler) {
    public StreamCodec<FriendlyByteBuf, T> codec() {
        return StreamCodec.ofMember(AbstractPacket::write, reader::read);
    }
}
