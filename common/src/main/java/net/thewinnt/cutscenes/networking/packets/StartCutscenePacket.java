package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public record StartCutscenePacket(ResourceLocation cutscene, Vec3 startPos, float cameraYaw, float cameraPitch,
                                  float cameraRoll, float pathYaw, float pathPitch,
                                  float pathRoll) implements AbstractPacket {
    public static final Type<StartCutscenePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("cutscenes", "start_cutscene"));

    public static StartCutscenePacket read(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        Vec3 startPos = buf.readVec3();
        float cameraYaw = buf.readFloat();
        float cameraPitch = buf.readFloat();
        float cameraRoll = buf.readFloat();
        float pathYaw = buf.readFloat();
        float pathPitch = buf.readFloat();
        float pathRoll = buf.readFloat();
        return new StartCutscenePacket(type, startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNullable(cutscene, FriendlyByteBuf::writeResourceLocation);
        buf.writeVec3(startPos);
        buf.writeFloat(cameraYaw);
        buf.writeFloat(cameraPitch);
        buf.writeFloat(cameraRoll);
        buf.writeFloat(pathYaw);
        buf.writeFloat(pathPitch);
        buf.writeFloat(pathRoll);
    }

    @Override
    public void execute() {
        ClientCutsceneManager.startCutscene(ClientCutsceneManager.CLIENT_REGISTRY.get(cutscene), startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
