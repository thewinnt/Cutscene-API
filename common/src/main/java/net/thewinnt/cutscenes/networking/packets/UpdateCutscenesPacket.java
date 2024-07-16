package net.thewinnt.cutscenes.networking.packets;

import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public class UpdateCutscenesPacket implements AbstractPacket {
    public static final String ID = "cutscenes:update_cutscenes";
    private final Map<ResourceLocation, CutsceneType> registry;
    
    public UpdateCutscenesPacket(Map<ResourceLocation, CutsceneType> registry) {
        this.registry = registry;
    }

    public static UpdateCutscenesPacket read(FriendlyByteBuf buf) {
        return new UpdateCutscenesPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, CutsceneType::fromNetwork));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(registry, FriendlyByteBuf::writeResourceLocation, (b, cs) -> cs.toNetwork(b));
    }

    @Override
    public void execute() {
       ClientCutsceneManager.updateRegistry(registry);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CustomPacketPayload.createType(ID);
    }
}
