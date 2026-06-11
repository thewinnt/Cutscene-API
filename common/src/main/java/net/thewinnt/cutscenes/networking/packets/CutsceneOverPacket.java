package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.platform.AbstractServerboundPacket;
import net.thewinnt.cutscenes.util.PlayerExt;

public class CutsceneOverPacket implements AbstractServerboundPacket {
    public static final Type<CutsceneOverPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("cutscenes", "cutscene_over"));

    @Override
    public void execute(ServerPlayer player) {
        ((PlayerExt) player).csapi$finishCutscene(EndingReason.FINISH);
    }

    @Override
    public void write(FriendlyByteBuf FriendlyByteBuf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
