package net.thewinnt.cutscenes.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.thewinnt.cutscenes.networking.packets.*;

public class CutsceneNetworkHandler {
    private static int id_counter = 0;

    public static SimpleChannel INSTANCE;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("cutscenes:networking"),
            () -> "0.1",
            i -> true,
            i -> true // there's really only one version you can be using
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

        INSTANCE.messageBuilder(AddCutscenePacket.class, id_counter++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AddCutscenePacket::write)
            .decoder(AddCutscenePacket::read)
            .consumerMainThread(AddCutscenePacket::handle)
            .add();

        INSTANCE.messageBuilder(PreviewCutscenePacket.class, id_counter++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PreviewCutscenePacket::write)
            .decoder(PreviewCutscenePacket::read)
            .consumerMainThread(PreviewCutscenePacket::handle)
            .add();
    }

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        if (Double.isNaN(x) && Double.isNaN(y) && Double.isNaN(z)) {
            return null;
        }
        return new Vec3(x, y, z);
    }

    public static void writeVec3(FriendlyByteBuf buf, Vec3 vec) {
        if (vec == null) {
            vec = new Vec3(Double.NaN, Double.NaN, Double.NaN);
        }
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }
}
