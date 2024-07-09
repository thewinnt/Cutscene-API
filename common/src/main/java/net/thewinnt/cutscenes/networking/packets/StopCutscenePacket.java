package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public class StopCutscenePacket implements AbstractPacket {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes:stop_cutscene");

    @Override
    public void execute() {
        ClientCutsceneManager.stopCutsceneImmediate();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {}

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
