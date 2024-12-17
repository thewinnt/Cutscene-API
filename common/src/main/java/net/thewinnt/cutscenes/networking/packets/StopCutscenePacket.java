package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.platform.AbstractClientboundPacket;

public record StopCutscenePacket(EndingReason reason) implements AbstractClientboundPacket {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes", "stop_cutscene");

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
    public ResourceLocation id() {
        return ID;
    }
}
