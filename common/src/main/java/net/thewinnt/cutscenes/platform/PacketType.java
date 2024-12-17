package net.thewinnt.cutscenes.platform;

public record PacketType<T extends AbstractPacket>(Class<T> type, AbstractPacket.PacketReader<T> reader) {
}
