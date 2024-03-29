package net.thewinnt.cutscenes.client.preview;

import java.util.List;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.path.point.PointProvider;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;

@OnlyIn(Dist.CLIENT)
public class PathPreviewRenderer {
    private static final List<Vector3f> POINT_COLORS = List.of(
        new Vector3f(1, 0.5f, 1),
        new Vector3f(0.75f, 0.5f, 1),
        new Vector3f(0.5f, 0.5f, 1),
        new Vector3f(0.5f, 0.75f, 1),
        new Vector3f(0.75f, 0.75f, 1),
        new Vector3f(1, 0.75f, 1)
    );
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
            Vec3 start = segment.getStart(l, s).getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
            Vec3 end = segment.getEnd(l, s).getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
            drawPoint(stack, consumer, start, 0.3F, POINT_COLORS.get(0));
            drawPoint(stack, consumer, end, 0.3F, POINT_COLORS.get(0));
            float ticksPerWeight = type.length * 3 / path.getWeightSum(); // roughly one line per frame at 60 fps
            int thisLength = (int)(ticksPerWeight * segment.getWeight());
            for (int j = 0; j < thisLength; j++) {
                Vec3 a = segment.getPoint(j / (double)thisLength, l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
                Vec3 b = segment.getPoint((j + 1) / (double)thisLength, l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
                drawLineGlobal(stack, consumer, a, b, getColorAtPoint(i + j / (float)thisLength));
            }
        }
        for (Line i : path.getUtilityPoints(l, s, 0)) {
            Line line;
            if (i.isPoint()) {
                line = new Line(new StaticPointProvider(i.start.getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot)), null, i.level);
            } else {
                line = new Line(
                    new StaticPointProvider(i.start.getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot)),
                    new StaticPointProvider(i.end.getPoint(l, s).yRot(yRot).zRot(zRot).xRot(xRot)),
                    i.level
                );
            }
            if (line.isPoint()) {
                drawPoint(stack, consumer, line.start.getPoint(l, s).add(s), 0.2f, POINT_COLORS.get(line.level % POINT_COLORS.size()));
            } else {
                drawLineGlobal(stack, consumer, line, l, s);
                drawPoint(stack, consumer, line.start.getPoint(l, s).add(s), 0.2f, POINT_COLORS.get(line.level % POINT_COLORS.size()));
                drawPoint(stack, consumer, line.end.getPoint(l, s).add(s), 0.2f, POINT_COLORS.get(line.level % POINT_COLORS.size()));
            }
        }
        drawPoint(stack, consumer, ClientCutsceneManager.getOffset(), 0.25f, COLOR_START);
    }

    /** Draws a line relative to the camera */
    private static void drawLineLocal(PoseStack stack, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, Vector3f color) {
        Matrix4f matrix4f = stack.last().pose();
        Matrix3f matrix3f = stack.last().normal();
        consumer.vertex(matrix4f, x1, y1, z1).color(color.x(), color.y(), color.z(), 1f).normal(matrix3f, 0, 0, 0).endVertex();
        consumer.vertex(matrix4f, x2, y2, z2).color(color.x(), color.y(), color.z(), 1f).normal(matrix3f, 0, 0, 0).endVertex();
    }

    /** Draws a line relative to the world center */
    @SuppressWarnings("resource")
    private static void drawLineGlobal(PoseStack stack, VertexConsumer consumer, double x1, double y1, double z1, double x2, double y2, double z2, Vector3f color) {
        Vec3 cam_pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        x1 -= cam_pos.x;
        y1 -= cam_pos.y;
        z1 -= cam_pos.z;
        x2 -= cam_pos.x;
        y2 -= cam_pos.y;
        z2 -= cam_pos.z;
        drawLineLocal(stack, consumer, (float)x1, (float)y1, (float)z1, (float)x2, (float)y2, (float)z2, color);
    }

    private static void drawLineGlobal(PoseStack stack, VertexConsumer consumer, Vec3 a, Vec3 b, Vector3f color) {
        drawLineGlobal(stack, consumer, a.x, a.y, a.z, b.x, b.y, b.z, color);
    }

    public static void drawLineGlobal(PoseStack stack, VertexConsumer consumer, Line line, Level l, Vec3 s) {
        drawLineGlobal(stack, consumer, line.start.getPoint(l, s).add(s), line.end.getPoint(l, s).add(s), POINT_COLORS.get(line.level % POINT_COLORS.size()));
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

    public static record Line(PointProvider start, @Nullable PointProvider end, int level) {
        private boolean isPoint() {
            return end == null;
        }
    };
}
