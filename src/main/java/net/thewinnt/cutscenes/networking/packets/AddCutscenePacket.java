package net.thewinnt.cutscenes.networking.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.thewinnt.cutscenes.ClientCutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;

public class AddCutscenePacket {
    private final ResourceLocation id;
    private final CutsceneType type;
    
    public AddCutscenePacket(ResourceLocation id, CutsceneType type) {
        this.id = id;
        this.type = type;
    }

    public static AddCutscenePacket read(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        CutsceneType type = CutsceneType.fromNetwork(buf);
        return new AddCutscenePacket(id, type);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        type.toNetwork(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
           ClientCutsceneManager.registerCutscene(id, type);
        });
        supplier.get().setPacketHandled(true);
    }
}
