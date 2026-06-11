package net.thewinnt.cutscenes.networking.packets;

import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractClientboundPacket;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public class UpdateCutscenesPacket implements AbstractClientboundPacket {
    public static final Type<UpdateCutscenesPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("cutscenes", "update_cutscenes"));
    private final Map<Identifier, CutsceneType> registry;
    
    public UpdateCutscenesPacket(Map<Identifier, CutsceneType> registry) {
        this.registry = registry;
    }

    public static UpdateCutscenesPacket read(FriendlyByteBuf buf) {
        return new UpdateCutscenesPacket(buf.readMap(FriendlyByteBuf::readIdentifier, CutsceneType::fromNetwork));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(registry, FriendlyByteBuf::writeIdentifier, (b, cs) -> cs.toNetwork(b));
    }

    @Override
    public void execute() {
       ClientCutsceneManager.updateRegistry(registry);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
