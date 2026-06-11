package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.platform.AbstractClientboundPacket;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public record StopCutscenePacket(EndingReason reason) implements AbstractClientboundPacket {
    public static final Type<StopCutscenePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("cutscenes", "stop_cutscene"));

    @Override
    public void execute() {
        ClientCutsceneManager.stopCutsceneImmediate(reason);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(reason);
    }

    public static StopCutscenePacket read(FriendlyByteBuf buf) {
        return new StopCutscenePacket(buf.readEnum(EndingReason.class));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
