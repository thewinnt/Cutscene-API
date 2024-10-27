package net.thewinnt.cutscenes.platform;

import net.minecraft.server.level.ServerPlayer;

public interface AbstractServerboundPacket extends AbstractPacket {
    void execute(ServerPlayer player);
}
