package net.thewinnt.cutscenes.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.packets.*;
import net.thewinnt.cutscenes.path.point.PointProvider;

public class CutsceneNetworkHandler {
    public static final String PROTOCOL_VERSION = "1.2-pre";
    public static SimpleChannel INSTANCE;
    private static int id_counter = 0;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("cutscenes:networking"),
            () -> PROTOCOL_VERSION,
            i -> i.equals(PROTOCOL_VERSION),
            i -> i.equals(PROTOCOL_VERSION)
        );
        INSTANCE.messageBuilder(StartCutscenePacket.class, id_counter++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(StartCutscenePacket::write)
            .decoder(StartCutscenePacket::read)
            .consumerMainThread(StartCutscenePacket::handle)
            .add();

        INSTANCE.messageBuilder(UpdateCutscenesPacket.class, id_counter++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(UpdateCutscenesPacket::write)
            .decoder(UpdateCutscenesPacket::read)
            .consumerMainThread(UpdateCutscenesPacket::handle)
            .add();

        INSTANCE.messageBuilder(PreviewCutscenePacket.class, id_counter++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PreviewCutscenePacket::write)
            .decoder(PreviewCutscenePacket::read)
            .consumerMainThread(PreviewCutscenePacket::handle)
            .add();

        INSTANCE.messageBuilder(StopCutscenePacket.class, id_counter++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((t, u) -> {})
            .decoder(buf -> new StopCutscenePacket())
            .consumerMainThread(StopCutscenePacket::handle)
            .add();
    }

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            return new Vec3(x, y, z);
        } else {
            return null;
        }
    }

    public static PointProvider readPointProvider(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            ResourceLocation id = buf.readResourceLocation();
            return CutsceneManager.getPointType(id).fromNetwork(buf);
        } else {
            return null;
        }
    }

    public static void writeVec3(FriendlyByteBuf buf, Vec3 vec) {
        if (vec == null) {
            buf.writeBoolean(false); // is present
            return;
        } else {
            buf.writeBoolean(true);
        }
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public static void writePointProvider(FriendlyByteBuf buf, PointProvider point) {
        if (point == null) {
            buf.writeBoolean(false); // is present
            return;
        } else {
            buf.writeBoolean(true);
        }
        buf.writeResourceLocation(CutsceneManager.getPointTypeId(point.getSerializer()));
        point.toNetwork(buf);
    }
}
