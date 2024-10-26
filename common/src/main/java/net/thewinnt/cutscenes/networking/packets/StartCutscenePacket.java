package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.platform.AbstractPacket;

import java.util.Objects;

public final class StartCutscenePacket implements AbstractPacket {
    public static final Type<StartCutscenePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("cutscenes", "start_cutscene"));
    private final ResourceLocation cutscene;
    private final Vec3 startPos;
    private final float cameraYaw;
    private final float cameraPitch;
    private final float cameraRoll;
    private final float pathYaw;
    private final float pathPitch;
    private final float pathRoll;
    private final long startGameTime;

    public StartCutscenePacket(ResourceLocation cutscene, Vec3 startPos, float cameraYaw, float cameraPitch, float cameraRoll, float pathYaw, float pathPitch, float pathRoll) {
        this(cutscene, startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll, CutsceneAPI.platform().getServer().overworld().getGameTime());
    }

    private StartCutscenePacket(ResourceLocation cutscene, Vec3 startPos, float cameraYaw, float cameraPitch, float cameraRoll, float pathYaw, float pathPitch, float pathRoll, long startGameTime) {
        this.cutscene = cutscene;
        this.startPos = startPos;
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
        this.cameraRoll = cameraRoll;
        this.pathYaw = pathYaw;
        this.pathPitch = pathPitch;
        this.pathRoll = pathRoll;
        this.startGameTime = startGameTime;
    }

    public static StartCutscenePacket read(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        Vec3 startPos = buf.readVec3();
        float cameraYaw = buf.readFloat();
        float cameraPitch = buf.readFloat();
        float cameraRoll = buf.readFloat();
        float pathYaw = buf.readFloat();
        float pathPitch = buf.readFloat();
        float pathRoll = buf.readFloat();
        long startGameTime = buf.readVarLong();
        return new StartCutscenePacket(type, startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll, startGameTime);
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
        buf.writeVarLong(startGameTime);
    }

    @Override
    public void execute() {
        ClientCutsceneManager.startCutscene(ClientCutsceneManager.CLIENT_REGISTRY.get(cutscene), startPos, cameraYaw, cameraPitch, cameraRoll, pathYaw, pathPitch, pathRoll, startGameTime);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
