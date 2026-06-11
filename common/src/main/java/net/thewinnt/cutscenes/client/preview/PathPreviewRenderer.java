package net.thewinnt.cutscenes.client.preview;

import java.util.List;

import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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

    public static void emitGizmos() {
        CutsceneType type = ClientCutsceneManager.getPreviewedCutscene();
        if (type == null) return;
        Path path = type.path;
        if (path == null) return;
        float yRot = (float)Math.toRadians(ClientCutsceneManager.previewPathYaw);
        float zRot = (float)Math.toRadians(ClientCutsceneManager.previewPathPitch);
        float xRot = (float)Math.toRadians(ClientCutsceneManager.previewPathRoll);
        Vec3 s = ClientCutsceneManager.getOffset();
        Level l = Minecraft.getInstance().level;
        for (int i = 0; i < path.size(); i++) {
            PathLike segment = path.getSegment(i);
            Vec3 start = PointProvider.getPoint(segment.getStart(l, s), l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
            Vec3 end = PointProvider.getPoint(segment.getEnd(l, s), l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
            drawPoint(start, 0.3F, POINT_COLORS.getFirst());
            drawPoint(end, 0.3F, POINT_COLORS.getFirst());
            double ticksPerWeight = type.length.length() * type.length.manager().ticksPerUnit() * 3 / path.getWeightSum(); // roughly one line per frame at 60 fps
            int thisLength = (int)(ticksPerWeight * segment.weight());
            for (int j = 0; j < thisLength; j++) {
                Vec3 a = segment.getPoint(j / (double)thisLength, l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
                Vec3 b = segment.getPoint((j + 1) / (double)thisLength, l, s).yRot(yRot).zRot(zRot).xRot(xRot).add(s);
                drawLineGlobal(a, b, getColorAtPoint(i + j / (float)thisLength));
            }
        }
        for (Line i : path.getUtilityPoints(l, s, 0)) {
            Line line;
            if (i.isPoint()) {
                line = new Line(new StaticPointProvider(PointProvider.getPoint(i.start, l, s).yRot(yRot).zRot(zRot).xRot(xRot)), null, i.level);
            } else {
                line = new Line(
                    new StaticPointProvider(PointProvider.getPoint(i.start, l, s).yRot(yRot).zRot(zRot).xRot(xRot)),
                    new StaticPointProvider(PointProvider.getPoint(i.end, l, s).yRot(yRot).zRot(zRot).xRot(xRot)),
                    i.level
                );
            }
            if (line.isPoint()) {
                drawPoint(PointProvider.getPoint(line.start, l, s).add(s), 0.2f, POINT_COLORS.get(line.level % POINT_COLORS.size()));
            } else {
                drawLineGlobal(line, l, s);
                drawPoint(PointProvider.getPoint(line.start, l, s).add(s), 0.2f, POINT_COLORS.get(line.level % POINT_COLORS.size()));
                drawPoint(PointProvider.getPoint(line.end, l, s).add(s), 0.2f, POINT_COLORS.get(line.level % POINT_COLORS.size()));
            }
        }
        drawPoint(ClientCutsceneManager.getOffset(), 0.25f, COLOR_START);
    }

    /** Draws a line relative to the world center */
    private static void drawLineGlobal(double x1, double y1, double z1, double x2, double y2, double z2, Vector3f color) {
        drawLineGlobal(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2), color);
    }

    private static void drawLineGlobal(Vec3 a, Vec3 b, Vector3f color) {
        Gizmos.line(a, b, ARGB.colorFromFloat(1, color.x, color.y, color.z));
    }

    private static void drawLineGlobal(Line line, Level l, Vec3 s) {
        drawLineGlobal(PointProvider.getPoint(line.start, l, s).add(s), PointProvider.getPoint(line.end, l, s).add(s), POINT_COLORS.get(line.level % POINT_COLORS.size()));
    }

    private static Vector3f getColorAtPoint(float point) {
        point /= (ClientCutsceneManager.getPreviewedCutscene().path.size()); // to get a value [0-1]
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

    private static void drawPoint(Vec3 pos, float size, Vector3f color) {
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        drawLineGlobal(x-size, y, z, x+size, y, z, color);
        drawLineGlobal(x, y-size, z, x, y+size, z, color);
        drawLineGlobal(x, y, z-size, x, y, z+size, color);
    }

    public record Line(PointProvider start, @Nullable PointProvider end, int level) {
        public boolean isPoint() {
            return end == null;
        }
    }
}
