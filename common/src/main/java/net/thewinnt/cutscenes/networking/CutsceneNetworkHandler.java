package net.thewinnt.cutscenes.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.path.point.PointProvider;

import java.util.function.IntFunction;

public class CutsceneNetworkHandler {
    /** @deprecated use {@link FriendlyByteBuf#readVec3()} instead */
    @Deprecated(forRemoval = true)
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

    /** @deprecated use {@link FriendlyByteBuf#writeVec3(Vec3)} instead */
    @Deprecated(forRemoval = true)
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

    public static <T> T[] readArray(FriendlyByteBuf buf, IntFunction<T[]> generator, FriendlyByteBuf.Reader<T> reader) {
        T[] array = generator.apply(buf.readInt());
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.apply(buf);
        }
        return array;
    }

    public static <T> void writeArray(FriendlyByteBuf buf, T[] array, FriendlyByteBuf.Writer<T> writer) {
        buf.writeInt(array.length);
        for (T i : array) {
            writer.accept(buf, i);
        }
    }
}
