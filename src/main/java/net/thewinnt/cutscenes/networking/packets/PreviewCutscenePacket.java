package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;

public class PreviewCutscenePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes:preview_cutscene");
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
        Vec3 startPos = CutsceneNetworkHandler.readVec3(buf);
        float pathYaw = buf.readFloat();
        float pathPitch = buf.readFloat();
        float pathRoll = buf.readFloat();
        return new PreviewCutscenePacket(type, startPos, pathYaw, pathPitch, pathRoll);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNullable(type, FriendlyByteBuf::writeResourceLocation);
        CutsceneNetworkHandler.writeVec3(buf, startPos);
        buf.writeFloat(pathYaw);
        buf.writeFloat(pathPitch);
        buf.writeFloat(pathRoll);
    }

    public void handle(final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
           ClientCutsceneManager.setPreviewedCutscene(ClientCutsceneManager.CLIENT_REGISTRY.get(type), startPos, pathYaw, pathPitch, pathRoll);
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
