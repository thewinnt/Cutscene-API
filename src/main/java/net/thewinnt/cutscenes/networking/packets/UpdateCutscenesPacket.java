package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

import java.util.Map;

public class UpdateCutscenesPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes:update_cutscenes");
    private final Map<ResourceLocation, CutsceneType> registry;
    
    public UpdateCutscenesPacket(Map<ResourceLocation, CutsceneType> registry) {
        this.registry = registry;
    }

    public static UpdateCutscenesPacket read(FriendlyByteBuf buf) {
        return new UpdateCutscenesPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, CutsceneType::fromNetwork));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeMap(registry, FriendlyByteBuf::writeResourceLocation, (b, cs) -> cs.toNetwork(b));
    }

    public void handle(PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
           ClientCutsceneManager.updateRegistry(registry);
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
