package net.thewinnt.cutscenes.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface AbstractPacket {
    ResourceLocation id();
    void write(FriendlyByteBuf buf);

    @FunctionalInterface
    interface PacketReader<T extends AbstractPacket> {
        T read(FriendlyByteBuf buf);
    }
}
