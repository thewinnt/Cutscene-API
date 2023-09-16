package net.thewinnt.cutscenes.networking.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;

public class StartCutscenePacket {
    public final ResourceLocation type;
    public final Vec3 startPos;
    public final float cameraYaw;
    public final float cameraPitch;
    public final float cameraRoll;
    public final float pathYaw;
    public final float pathPitch;
    public final float pathRoll;

    public StartCutscenePacket(ResourceLocation type, Vec3 startPos, float cameraYaw, float cameraPitch, float cameraRoll, float pathYaw, float pathPitch, float pathRoll) {
        this.type = type;
        this.startPos = startPos;
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
        this.cameraRoll = cameraRoll;
        this.pathYaw = pathYaw;
        this.pathPitch = pathPitch;
        this.pathRoll = pathRoll;
    }

    public static StartCutscenePacket read(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        Vec3 startPos = CutsceneNetworkHandler.readVec3(buf);
        float cameraYaw = buf.readFloat();
        float cameraPitch = buf.readFloat();
        float cameraRoll = buf.readFloat();
        float pathYaw = buf.readFloat();
        float pathPitch = buf.readFloat();
        float pathRoll = buf.readFloat();
        return new StartCutscenePacket(type, startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNullable(type, FriendlyByteBuf::writeResourceLocation);
        CutsceneNetworkHandler.writeVec3(buf, startPos);
        buf.writeFloat(cameraYaw);
        buf.writeFloat(cameraPitch);
        buf.writeFloat(cameraRoll);
        buf.writeFloat(pathYaw);
        buf.writeFloat(pathPitch);
        buf.writeFloat(pathRoll);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
           ClientCutsceneManager.startCutscene(ClientCutsceneManager.CLIENT_REGISTRY.get(type), startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll);
        });
        supplier.get().setPacketHandled(true);
    }
}
