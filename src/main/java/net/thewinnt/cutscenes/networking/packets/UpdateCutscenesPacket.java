package net.thewinnt.cutscenes.networking.packets;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.thewinnt.cutscenes.ClientCutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;

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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
           ClientCutsceneManager.updateRegistry(registry);
        });
        supplier.get().setPacketHandled(true);
    }
}
