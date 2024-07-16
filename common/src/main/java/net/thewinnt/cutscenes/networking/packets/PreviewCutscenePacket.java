package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractPacket;

public class PreviewCutscenePacket implements AbstractPacket {
    public static final String ID = "cutscenes:preview_cutscene";
    public final ResourceLocation type;
    public final Vec3 startPos;
    public final float pathYaw;
    public final float pathPitch;
    public final float pathRoll;

    public PreviewCutscenePacket(ResourceLocation type, Vec3 startPos, float pathYaw, float pathPitch, float pathRoll) {
        this.type = type;
        this.startPos = startPos;
        this.pathYaw = pathYaw;
        this.pathPitch = pathPitch;
        this.pathRoll = pathRoll;
    }

    public static PreviewCutscenePacket read(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        Vec3 startPos = buf.readVec3();
        float pathYaw = buf.readFloat();
        float pathPitch = buf.readFloat();
        float pathRoll = buf.readFloat();
        return new PreviewCutscenePacket(type, startPos, pathYaw, pathPitch, pathRoll);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNullable(type, FriendlyByteBuf::writeResourceLocation);
        buf.writeVec3(startPos);
        buf.writeFloat(pathYaw);
        buf.writeFloat(pathPitch);
        buf.writeFloat(pathRoll);
    }

    public void execute() {
       ClientCutsceneManager.setPreviewedCutscene(ClientCutsceneManager.CLIENT_REGISTRY.get(type), startPos, pathYaw, pathPitch, pathRoll);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CustomPacketPayload.createType(ID);
    }
}
