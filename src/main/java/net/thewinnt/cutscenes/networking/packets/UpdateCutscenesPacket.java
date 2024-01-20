package net.thewinnt.cutscenes.networking.packets;

import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

public class UpdateCutscenesPacket {
    private final Map<ResourceLocation, CutsceneType> registry;
    
    public UpdateCutscenesPacket(Map<ResourceLocation, CutsceneType> registry) {
        this.registry = registry;
    }

    public static UpdateCutscenesPacket read(FriendlyByteBuf buf) {
        return new UpdateCutscenesPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, CutsceneType::fromNetwork));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeMap(registry, (b, id) -> b.writeResourceLocation(id), (b, cs) -> cs.toNetwork(b));
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
           ClientCutsceneManager.updateRegistry(registry);
        });
        context.setPacketHandled(true);
    }
}
