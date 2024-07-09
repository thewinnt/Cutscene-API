package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.util.DynamicVertex;
import net.thewinnt.cutscenes.util.TimeProvider;
import org.joml.Matrix4f;

public class TriangleStripOverlay implements Overlay {
    public static final RenderType TRIANGLE_STRIP = CutsceneAPI.clientPlatform().getTriangleStrip();
    private final TriangleStripConfiguration config;

    public TriangleStripOverlay(TriangleStripConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config) {
        minecraft.getProfiler().push("cutscenes:triangle_strip");
        TimeProvider time = (TimeProvider) config;
        VertexConsumer consumer = graphics.bufferSource().getBuffer(TRIANGLE_STRIP);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();
        double t = time.getProgress();
        for (DynamicVertex i : this.config.vertices()) {
            float x = i.x().get(t, width);
            float y = i.y().get(t, height);
            consumer.vertex(matrix4f, x, y, 0).color(i.color().toARGB(t)).endVertex();
        }
        stack.popPose();
        minecraft.getProfiler().pop();
    }
}
