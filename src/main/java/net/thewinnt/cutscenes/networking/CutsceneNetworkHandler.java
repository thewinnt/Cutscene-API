package net.thewinnt.cutscenes.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StopCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.path.point.PointProvider;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class CutsceneNetworkHandler {
    public static final int PROTOCOL_VERSION = 3;
    private static int id_counter = 0;

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("cutscenes").versioned("1.2.2");
        registrar.play(
            PreviewCutscenePacket.ID,
            PreviewCutscenePacket::read,
            handler -> handler.client(PreviewCutscenePacket::handle)
        );

        registrar.play(
            StartCutscenePacket.ID,
            StartCutscenePacket::read,
            handler -> handler.client(StartCutscenePacket::handle)
        );

        registrar.play(
            StopCutscenePacket.ID,
            buf -> new StopCutscenePacket(),
            handler -> handler.client(StopCutscenePacket::handle)
        );

        registrar.play(
            UpdateCutscenesPacket.ID,
            UpdateCutscenesPacket::read,
            handler -> handler.client(UpdateCutscenesPacket::handle)
        );
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

    public static float[] readColorRGBA(FriendlyByteBuf buf) {
        float r = buf.readFloat();
        float g = buf.readFloat();
        float b = buf.readFloat();
        float a = buf.readFloat();
        return new float[]{r, g, b, a};
    }

    public static void writeColorRGBA(FriendlyByteBuf buf, float[] color) {
        buf.writeFloat(color[0]);
        buf.writeFloat(color[1]);
        buf.writeFloat(color[2]);
        buf.writeFloat(color[3]);
    }
}
