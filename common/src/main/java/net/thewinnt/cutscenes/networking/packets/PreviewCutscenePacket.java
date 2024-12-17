package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.platform.AbstractClientboundPacket;

public record PreviewCutscenePacket(ResourceLocation type, Vec3 startPos, float pathYaw,
                                    float pathPitch,
                                    float pathRoll) implements AbstractClientboundPacket {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes", "preview_cutscene");

    public static PreviewCutscenePacket read(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        Vec3 startPos = CutsceneNetworkHandler.readVec3(buf);
        float pathYaw = buf.readFloat();
        float pathPitch = buf.readFloat();
        float pathRoll = buf.readFloat();
        return new PreviewCutscenePacket(type, startPos, pathYaw, pathPitch, pathRoll);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNullable(type, FriendlyByteBuf::writeResourceLocation);
        CutsceneNetworkHandler.writeVec3(buf, startPos);
        buf.writeFloat(pathYaw);
        buf.writeFloat(pathPitch);
        buf.writeFloat(pathRoll);
    }

    public void execute() {
        ClientCutsceneManager.setPreviewedCutscene(ClientCutsceneManager.CLIENT_REGISTRY.get(type), startPos, pathYaw, pathPitch, pathRoll);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
