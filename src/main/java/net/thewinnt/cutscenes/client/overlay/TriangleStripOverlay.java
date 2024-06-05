package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.mixin.RenderStateShardAccessor;
import net.thewinnt.cutscenes.util.DynamicVertex;
import net.thewinnt.cutscenes.util.TimeProvider;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class TriangleStripOverlay implements Overlay {
    public static final RenderType TRIANGLE_STRIP = RenderType.create(
        "cutscenes:triangle_strip",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLE_STRIP,
        1536,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShardAccessor.sc$getGuiShader())
            .setTransparencyState(RenderStateShardAccessor.sc$getTranslucentTransparency())
            .setDepthTestState(RenderStateShardAccessor.sc$getLEqualDepthTest())
            .setCullState(RenderStateShardAccessor.sc$getNoCull())
            .createCompositeState(false)
    );
    private final TriangleStripConfiguration config;

    public TriangleStripOverlay(TriangleStripConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config) {
        TimeProvider time = (TimeProvider) config;
        VertexConsumer consumer = graphics.bufferSource().getBuffer(TRIANGLE_STRIP);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();
        double t = time.getProgress();
        for (DynamicVertex i : this.config.vertices()) {
            float x = i.x().get(t, width);
            float y = i.y().get(t, height);
//            float[] colors = i.color().sample(t);
//            CutsceneAPI.LOGGER.debug("vertex {} / {} ({}, {}, {}, {})", x, y, (int)(colors[0] * 255), (int)(colors[1] * 255), (int)(colors[2] * 255), (int)(colors[3] * 255));
            consumer.vertex(matrix4f, x, y, 0).color(i.color().toARGB(t)).endVertex();
        }
        stack.popPose();
    }
}
