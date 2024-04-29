package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

public class StopCutscenePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes:stop_cutscene");

    public void handle(PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            ClientCutsceneManager.stopCutscene();
        });
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {}

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
