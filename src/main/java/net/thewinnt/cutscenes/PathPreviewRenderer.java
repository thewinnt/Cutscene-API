package net.thewinnt.cutscenes;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PathPreviewRenderer {
    private static final Vector3f COLOR_POINT = new Vector3f(1, 0.5f, 1);
    private static final Vector3f COLOR_CONTROL_LINE = new Vector3f(0.5f, 0.5f, 1);
    private static final Vector3f COLOR_START = new Vector3f(0.25f, 0.5f, 1);

    @SuppressWarnings("resource")
    public static void beforeDebugRender(PoseStack stack, VertexConsumer consumer) {
        if (ClientCutsceneManager.previewedCutscene == null) return;
        CutsceneType type = ClientCutsceneManager.previewedCutscene;
        Path path = type.path;
        float yRot = (float)Math.toRadians(ClientCutsceneManager.previewPathYaw);
        float zRot = (float)Math.toRadians(ClientCutsceneManager.previewPathPitch);
        float xRot = (float)Math.toRadians(ClientCutsceneManager.previewPathRoll);
        Vec3 s = ClientCutsceneManager.getOffset();
        Level l = Minecraft.getInstance().level;
        for (int i = 0; i < path.size(); i++) {
            PathLike segment = path.getSegment(i);
            Vec3 offset = ClientCutsceneManager.getOffset();
            Vec3 start = segment.getStart(l, s).getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(offset);
            Vec3 end = segment.getEnd(l, s).getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(offset);
            drawPoint(stack, consumer, start, 0.3F, COLOR_POINT);
            drawPoint(stack, consumer, end, 0.3F, COLOR_POINT);
            float ticksPerWeight = type.length * 3 / path.getWeightSum(); // roughly one line per frame at 60 fps
            int thisLength = (int)(ticksPerWeight * segment.getWeight());
            for (int j = 0; j < thisLength; j++) {
                Vec3 a = segment.getPoint(j / (double)thisLength, l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(offset);
                Vec3 b = segment.getPoint((j + 1) / (double)thisLength, l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(offset);
                drawLineGlobal(stack, consumer, a, b, getColorAtPoint(i + j / (float)thisLength));
            }
        }
        drawPoint(stack, consumer, ClientCutsceneManager.getOffset(), 0.25f, COLOR_START);
    }

    /** Draws a line relative to the camera */
    private static void drawLineLocal(PoseStack stack, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, Vector3f color) {
        Matrix4f matrix4f = stack.last().pose();
        Matrix3f matrix3f = stack.last().normal();
        // i have no idea what normals do - seems to work fine without them
        consumer.vertex(matrix4f, x1, y1, z1).color(color.x(), color.y(), color.z(), 1f).normal(matrix3f, 0, 0, 0).endVertex();
        consumer.vertex(matrix4f, x2, y2, z2).color(color.x(), color.y(), color.z(), 1f).normal(matrix3f, 0, 0, 0).endVertex();
    }

    /** Draws a line relative to the world center */
    @SuppressWarnings("resource")
    private static void drawLineGlobal(PoseStack stack, VertexConsumer comsumer, double x1, double y1, double z1, double x2, double y2, double z2, Vector3f color) {
        Vec3 cam_pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        x1 -= cam_pos.x;
        y1 -= cam_pos.y;
        z1 -= cam_pos.z;
        x2 -= cam_pos.x;
        y2 -= cam_pos.y;
        z2 -= cam_pos.z;
        drawLineLocal(stack, comsumer, (float)x1, (float)y1, (float)z1, (float)x2, (float)y2, (float)z2, color);
    }

    private static void drawLineGlobal(PoseStack stack, VertexConsumer consumer, Vec3 a, Vec3 b, Vector3f color) {
        drawLineGlobal(stack, consumer, a.x, a.y, a.z, b.x, b.y, b.z, color);
    }

    private static Vector3f getColorAtPoint(float point) {
        point /= (ClientCutsceneManager.previewedCutscene.path.size()); // to get a value [0-1]
        float red, green, blue;
        if (point < 0.333f) {
            red = 1 - point * 3;
            green = point * 3;
            blue = 0;
        } else if (point < 0.667f) {
            red = 0;
            green = 1 - (point - 0.333f) * 3;
            blue = (point - 0.333f) * 3;
        } else {
            red = (point - 0.667f) * 3;
            green = 0;
            blue = 1 - (point - 0.667f) * 3;
        }
        red = Mth.sqrt(red);
        green = Mth.sqrt(green);
        blue = Mth.sqrt(blue);
        return new Vector3f(red, green, blue); // allows me to pack three values into one
    }

    private static void drawPoint(PoseStack stack, VertexConsumer consumer, Vec3 pos, float size, Vector3f color) {
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        drawLineGlobal(stack, consumer, x-size, y, z, x+size, y, z, color);
        drawLineGlobal(stack, consumer, x, y-size, z, x, y+size, z, color);
        drawLineGlobal(stack, consumer, x, y, z-size, x, y, z+size, color);
    }
}
